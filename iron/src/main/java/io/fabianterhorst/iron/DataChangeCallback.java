package io.fabianterhorst.iron;


public abstract class DataChangeCallback<T> {

    protected String mKey;
    protected String mClassName;
    protected Enum[] mValues;
    protected String mIdentifier;

    public DataChangeCallback(Object object, String key) {
        this.mKey = key;
        this.mClassName = object.getClass().getName();
    }

    public DataChangeCallback(Object object, Class clazz) {
        this.mKey = clazz.getName();
        this.mClassName = object.getClass().getName();
    }

    public Class<?> getType() {
        return DAOUtil.getTypeArguments(DataChangeCallback.class, this.getClass()).get(0);
    }

    public DataChangeCallback(Object object) {
        this.mClassName = object.getClass().getName();
    }

    public DataChangeCallback(String identifier) {
        this.mIdentifier = this.mKey = identifier;
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        this.mKey = key;
    }

    public String getClassName() {
        return mClassName;
    }

    public void setValues(Enum[] values) {
        this.mValues = values;
    }

    public Enum[] getValues() {
        return mValues;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    /**
     * Called when data is changed with the data key and the value
     *
     * @param key   the key
     * @param value the value
     */
    public void onDataChange(String key, T value) {
        //Should be override when listener is set
    }

    /**
     * Called when data is changed with the value (should be used with a Typed DataChangeListener)
     *
     * @param value the value
     */
    public void onDataChange(T value) {
        //Should be override when listener is set
    }

    /**
     * Called when data is changed with the data class key and the value
     *
     * @param clazz the class key
     * @param value the value
     */
    public void onDataChange(Class clazz, T value) {
        //Should be override when listener is set
    }
}
