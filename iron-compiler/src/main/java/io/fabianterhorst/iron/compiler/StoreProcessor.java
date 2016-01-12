package io.fabianterhorst.iron.compiler;

import org.apache.commons.io.IOUtils;

import java.io.Writer;
import java.lang.annotation.Annotation;
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
import io.fabianterhorst.iron.annotations.DefaultBoolean;
import io.fabianterhorst.iron.annotations.DefaultFloat;
import io.fabianterhorst.iron.annotations.DefaultInt;
import io.fabianterhorst.iron.annotations.DefaultLong;
import io.fabianterhorst.iron.annotations.DefaultObject;
import io.fabianterhorst.iron.annotations.DefaultString;
import io.fabianterhorst.iron.annotations.DefaultStringSet;
import io.fabianterhorst.iron.annotations.Name;
import io.fabianterhorst.iron.annotations.Store;

@SupportedAnnotationTypes("io.fabianterhorst.iron.annotations.Store")
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

                    String fieldDefaultValue = getDefaultValue(variableElement, fieldType);
                    /*if (fieldDefaultValue == null) {
                        // Problem detected: halt
                        return true;
                    }*/

                    String fieldName = variableElement.getSimpleName().toString();

                    boolean transaction = false;
                    boolean async = false;
                    boolean listener = false;
                    boolean loader = false;

                    Name fieldNameAnnot = variableElement.getAnnotation(Name.class);
                    String keyName = getKeyName(fieldName, fieldNameAnnot);
                    if(fieldNameAnnot != null) {
                        transaction = fieldNameAnnot.transaction();
                        listener = fieldNameAnnot.listener();
                        loader = fieldNameAnnot.loader();
                        async = fieldNameAnnot.async();
                    }
                    prefList.add(new StoreEntry(fieldName, keyName, fieldType.toString(), transaction,
                            listener, loader, async, fieldDefaultValue));
                }

                Map<String, Object> args = new HashMap<>();


                JavaFileObject javaFileObject;
                try {
                    Store fieldStoreAnnot = element.getAnnotation(Store.class);
                    // StoreWrapper
                    javaFileObject = processingEnv.getFiler().createSourceFile(classElement.getQualifiedName() + SUFFIX_PREF_WRAPPER);
                    Template template = getFreemarkerConfiguration().getTemplate("storewrapper.ftl");
                    args.put("package", fieldStoreAnnot.value().length() > 0 ? fieldStoreAnnot.value() : packageElement.getQualifiedName());
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

    private String getDefaultValue(VariableElement variableElement, TypeMirror fieldType) {
        Class<? extends Annotation> annotClass = DefaultBoolean.class;
        ObjectType compatiblePrefType = ObjectType.BOOLEAN;
        DefaultBoolean defaultBooleanAnnot = (DefaultBoolean) variableElement.getAnnotation(annotClass);
        if (defaultBooleanAnnot != null) {
            if (!ensureCompatibleAnnotation(compatiblePrefType, fieldType, annotClass, variableElement)) return null;
            return String.valueOf(defaultBooleanAnnot.value());
        }

        annotClass = DefaultFloat.class;
        compatiblePrefType = ObjectType.FLOAT;
        DefaultFloat defaultFloatAnnot = (DefaultFloat) variableElement.getAnnotation(annotClass);
        if (defaultFloatAnnot != null) {
            if (!ensureCompatibleAnnotation(compatiblePrefType, fieldType, annotClass, variableElement)) return null;
            return String.valueOf(defaultFloatAnnot.value()) + "f";
        }

        annotClass = DefaultInt.class;
        compatiblePrefType = ObjectType.INTEGER;
        DefaultInt defaultIntAnnot = (DefaultInt) variableElement.getAnnotation(annotClass);
        if (defaultIntAnnot != null) {
            if (!ensureCompatibleAnnotation(compatiblePrefType, fieldType, annotClass, variableElement)) return null;
            return String.valueOf(defaultIntAnnot.value());
        }

        annotClass = DefaultLong.class;
        compatiblePrefType = ObjectType.LONG;
        DefaultLong defaultLongAnnot = (DefaultLong) variableElement.getAnnotation(annotClass);
        if (defaultLongAnnot != null) {
            if (!ensureCompatibleAnnotation(compatiblePrefType, fieldType, annotClass, variableElement)) return null;
            return String.valueOf(defaultLongAnnot.value()) + "L";
        }

        annotClass = DefaultString.class;
        compatiblePrefType = ObjectType.STRING;
        DefaultString defaultStringAnnot = (DefaultString) variableElement.getAnnotation(annotClass);
        if (defaultStringAnnot != null) {
            if (!ensureCompatibleAnnotation(compatiblePrefType, fieldType, annotClass, variableElement)) return null;
            return "\"" + defaultStringAnnot.value() + "\"";
        }

        annotClass = DefaultStringSet.class;
        compatiblePrefType = ObjectType.STRING_SET;
        DefaultStringSet defaultStringSetAnnot = (DefaultStringSet) variableElement.getAnnotation(annotClass);
        if (defaultStringSetAnnot != null) {
            if (!ensureCompatibleAnnotation(compatiblePrefType, fieldType, annotClass, variableElement)) return null;
            StringBuilder res = new StringBuilder("new HashSet<String>(Arrays.asList(");
            int i = 0;
            for (String s : defaultStringSetAnnot.value()) {
                if (i > 0) res.append(", ");
                res.append("\"");
                res.append(s);
                res.append("\"");
                i++;
            }
            res.append("))");
            return res.toString();
        }

        annotClass = DefaultObject.class;
        DefaultObject defaultObjectAnnot = (DefaultObject) variableElement.getAnnotation(annotClass);
        if (defaultObjectAnnot != null) {
            return "new " + variableElement.asType().toString() + "()";
        }

        // Default default value :)
        return "null";
    }

    private boolean ensureCompatibleAnnotation(ObjectType objectType, TypeMirror fieldType, Class<?> annotClass, VariableElement variableElement) {
        if (!objectType.isCompatible(fieldType)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    annotClass.getSimpleName() + " annotation is only allowed on " + objectType.getSimpleName() + " fields", variableElement);
            return false;
        }
        return true;
    }

    private static String getKeyName(String fieldName, Name fieldNameAnnot) {
        if (fieldNameAnnot != null && fieldNameAnnot.value() != null) {
            return fieldNameAnnot.value();
        }
        return fieldName;
    }
}