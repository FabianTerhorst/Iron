package ${package};

import java.lang.reflect.Field;

import io.fabianterhorst.iron.Chest;
import io.fabianterhorst.iron.DataChangeCallback;
import io.fabianterhorst.iron.Iron;
import io.fabianterhorst.iron.IronLoadExtension;

public class ${keyWrapperClassName} {

public enum Keys {
    <#list keyList as key>
    ${key.key?upper_case}("${key.key}"),
    </#list>
    ;

    private final String text;

    private Keys(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

    public static void addOnDataChangeListener(DataChangeCallback  dataChangeCallback){
        dataChangeCallback.setValues(Keys.values());
        Iron.chest().addOnDataChangeListener(dataChangeCallback);
    }

    public static void removeListener(Object object){
        Iron.chest().removeListener(object);
    }

<#list keyList as key>
    public static void set${key.key?cap_first}(${key.className} ${key.key}) {
        Iron.chest().write("${key.key}", ${key.key});
    }

    public static void put${key.key?cap_first}(${key.className} ${key.key}) {
        Iron.chest().put("${key.key}", ${key.key});
    }

    public static <T> void load${key.key?cap_first}(IronLoadExtension ironLoadExtension, T call) {
        Iron.chest().load(ironLoadExtension, call, "${key.key}");
    }

    public static ${key.className} get${key.key?cap_first}() {
        return Iron.chest().read("${key.key}", new ${key.className}());
    }

    public static void get${key.key?cap_first}(Chest.ReadCallback readCallback) {
        Iron.chest().get("${key.key}", readCallback);
    }

<#if key.className?contains('java.util.ArrayList')>
    public static <T extends ${key.className?replace('java.util.ArrayList<', '')} void get${key.key?cap_first}ForField(final String fieldName, final Object searchValue, final Chest.ReadCallback<T> readCallback){
        Iron.chest().get("${key.key}", new Chest.ReadCallback<${key.className}>() {
            @Override
            public void onResult(${key.className} ${key.key}) {
                if(${key.key} != null){
                    for(${key.className?replace('java.util.ArrayList<', '')?replace('>', '')} object : ${key.key}) {
                        try {
                            Field field = object.getClass().getDeclaredField(fieldName);
                            field.setAccessible(true);
                            Object value = field.get(object);
                            if(value != null){
                                if (value.equals(searchValue)){
                                    readCallback.onResult((T)object);
                                    break;
                                }
                            }
                            readCallback.onResult(null);
                        }catch(NoSuchFieldException nsf){
                            nsf.printStackTrace();
                        }catch(IllegalAccessException iae){
                            iae.printStackTrace();
                        }
                    }
                }
            }
        });
    }
</#if>
    public static void remove${key.key?cap_first}() {
        Iron.chest().delete("${key.key}");
    }
<#if key.transaction>
    public static <T extends ${key.className}> void execute${key.key?cap_first}Transaction(Chest.Transaction<T> transaction){
        Iron.chest().execute("${key.key}", transaction, new ${key.className}());
    }
</#if>
<#if key.listener>
    public static <T extends ${key.className}> void addOn${key.key?cap_first}DataChangeListener(DataChangeCallback<T>  dataChangeCallback){
        dataChangeCallback.setKey("${key.key}");
        Iron.chest().addOnDataChangeListener(dataChangeCallback);
    }
</#if>
</#list>
}