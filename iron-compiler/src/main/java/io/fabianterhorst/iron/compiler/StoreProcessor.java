package io.fabianterhorst.iron.compiler;

import org.apache.commons.io.IOUtils;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
import io.fabianterhorst.iron.annotations.Name;

@SupportedAnnotationTypes("io.fabianterhorst.iron.Store")
public class StoreProcessor extends AbstractProcessor {
    private static final String SUFFIX_PREF_WRAPPER = "Store";

    private Configuration mFreemarkerConfiguration;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private Configuration getFreemarkerConfiguration() {
        if (mFreemarkerConfiguration == null) {
            mFreemarkerConfiguration = new Configuration(new Version(2, 3, 22));
            mFreemarkerConfiguration.setClassForTemplateLoading(getClass(), "");
        }
        return mFreemarkerConfiguration;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement te : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(te)) {
                TypeElement classElement = (TypeElement) element;
                PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();

                //String classComment = processingEnv.getElementUtils().getDocComment(classElement);

                List<StoreEntry> prefList = new ArrayList<StoreEntry>();
                // Iterate over the fields of the class
                for (VariableElement variableElement : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
                    if (variableElement.getModifiers().contains(Modifier.STATIC)) {
                        // Ignore constants
                        continue;
                    }

                    TypeMirror fieldType = variableElement.asType();
                    String fieldName = variableElement.getSimpleName().toString();

                    Name fieldNameAnnot = variableElement.getAnnotation(Name.class);
                    String keyName = getKeyName(fieldName, fieldNameAnnot);
                    prefList.add(new StoreEntry(keyName, fieldType.toString(), fieldNameAnnot.transaction(),
                                                fieldNameAnnot.listener(), fieldNameAnnot.loader(), fieldNameAnnot.async()));
                }

                Map<String, Object> args = new HashMap<String, Object>();


                JavaFileObject javaFileObject = null;
                try {
                    // StoreWrapper
                    javaFileObject = processingEnv.getFiler().createSourceFile(classElement.getQualifiedName() + SUFFIX_PREF_WRAPPER);
                    Template template = getFreemarkerConfiguration().getTemplate("storewrapper.ftl");
                    args.put("package", packageElement.getQualifiedName());
                    args.put("keyWrapperClassName", classElement.getSimpleName() + SUFFIX_PREF_WRAPPER);
                    args.put("keyList", prefList);
                    Writer writer = javaFileObject.openWriter();
                    template.process(args, writer);
                    IOUtils.closeQuietly(writer);

                } catch (Exception e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "En error occurred while generating Prefs code " + e.getClass() + e.getMessage(), element);
                    e.printStackTrace();
                    // Problem detected: halt
                    return true;
                }
            }
        }
        return true;
    }
    private static String getKeyName(String fieldName, Name fieldNameAnnot) {
        if (fieldNameAnnot != null) {
            return fieldNameAnnot.value();
        }
        return fieldName;
    }
}