package io.fabianterhorst.iron;


import android.util.Log;

public abstract class DataChangeCallback<T> {
    public static final String TAG = DataChangeCallback.class.getName();

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

    public Class<?> getType(){
        return DAOUtil.getTypeArguments(DataChangeCallback.class, this.getClass()).get(0);
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
        Log.d(TAG, "onDataChange(" + key + ", " + value + ")");
    }

    public void onDataChange(T value) {
        Log.d(TAG, "onDataChange(" + value + ")");
    }

    public void onDataChange(Class clazz, T value) {
        Log.d(TAG, "onDataChange(" + clazz.getSimpleName() + ", " + value + ")");
    }
}
