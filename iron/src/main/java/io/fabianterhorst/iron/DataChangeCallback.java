package io.fabianterhorst.iron;


public abstract class DataChangeCallback<T> {

    public Class<?> getType(){
        return DAOUtil.getTypeArguments(DataChangeCallback.class, this.getClass()).get(0);
    }

    protected String mKey;
    protected String mClassName;
    protected Enum[] mValues;

    public DataChangeCallback(Object object, String key) {
        this.mKey = key;
        this.mClassName = object.getClass().getName();
    }

    public DataChangeCallback(Object object, Class clazz) {
        this.mKey = clazz.getName();
        this.mClassName = object.getClass().getName();
    }

    public DataChangeCallback(Object object) {
        this.mClassName = object.getClass().getName();
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String key){
        this.mKey = key;
    }

    public String getClassName() {
        return mClassName;
    }

    public void setValues(Enum[] values){
        this.mValues = values;
    }

    public Enum[] getValues(){
        return mValues;
    }

    public void onDataChange(String key, T value) {

    }

    public void onDataChange(T value) {

    }

    public void onDataChange(Class clazz, T value) {

    }
}
