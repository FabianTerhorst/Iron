package io.fabianterhorst.iron;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Chest {

    public interface Transaction<T> {
        void execute(T value);
    }

    public interface ReadCallback<T> {
        void onResult(T value);
    }

    private final Storage mStorage;

    protected transient ArrayList<DataChangeCallback> mCallbacks;

    protected Chest(Context context, String dbName) {
        mStorage = new DbStoragePlainFile(context.getApplicationContext(), dbName);
    }

    /**
     * Destroys all data saved in Chest.
     */
    public void destroy() {
        mStorage.destroy();
    }

    /**
     * Saves any types of POJOs or collections in Chest storage.
     *
     * @param key   object key is used as part of object's file name
     * @param value object to save, must have no-arg constructor, can't be null.
     * @param <T>   object type
     * @return this Chest instance
     */
    public <T> Chest write(String key, T value) {
        if (value == null) {
            throw new IronException("Iron doesn't support writing null root values");
        } else {
            mStorage.insert(key, value);
            callCallbacks(key, value);
        }
        return this;
    }

    public <T> Chest write(Class clazz, T value) {
        write(clazz.getName(), value);
        return this;
    }

    /**
     * Saves any types of POJOs or collections in Chest storage async
     *
     * @param key   object key is used as part of object's file name
     * @param value object to save, must have no-arg constructor, can't be null.
     * @param <T>   object type
     * @return this Chest instance
     */
    public <T> Chest put(String key, T value) {
        AsyncTask<Object, Void, Void> task = new AsyncTask<Object, Void, Void>() {

            @SuppressWarnings("unchecked")
            @Override
            protected Void doInBackground(Object... objects) {
                String key = (String) objects[0];
                T value = (T) objects[1];
                write(key, value);
                return null;
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, key, value);
        return this;
    }

    /**
     * Instantiates saved object using original object class (e.g. LinkedList). Support limited
     * backward and forward compatibility: removed fields are ignored, new fields have their
     * default values.
     * <p/>
     * All instantiated objects must have no-arg constructors.
     *
     * @param key          object key to read
     * @param readCallback callback that return the readed object
     */
    public void get(String key, ReadCallback readCallback) {
        AsyncTask<Object, Void, Object> task = new AsyncTask<Object, Void, Object>() {

            protected ReadCallback mReadCallback;

            @Override
            protected Object doInBackground(Object... objects) {
                String key = (String) objects[0];
                mReadCallback = (ReadCallback) objects[1];
                return read(key);
            }

            @Override
            protected void onPostExecute(Object o) {
                mReadCallback.onResult(o);
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, key, readCallback);
    }

    public void execute(String key, Transaction transaction, Object defaultObject) {
        AsyncTask<Object, Void, Void> task = new AsyncTask<Object, Void, Void>() {

            @Override
            protected Void doInBackground(Object... objects) {
                String key = (String) objects[0];
                Transaction transaction = (Transaction) objects[1];
                Object defaultObject = objects[2];
                Object value = read(key);
                if (value == null)
                    value = defaultObject;
                transaction.execute(value);
                if (value != null)
                    write(key, value);
                return null;
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, key, transaction, defaultObject);
    }

    public void remove(String key) {
        AsyncTask<Object, Void, Void> task = new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... objects) {
                String key = (String) objects[0];
                delete(key);
                return null;
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, key);
    }

    public void removeAll() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                deleteAll();
                return null;
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void execute(String key, Transaction transaction) {
        execute(key, transaction, null);
    }

    /**
     * Instantiates saved object using original object class (e.g. LinkedList). Support limited
     * backward and forward compatibility: removed fields are ignored, new fields have their
     * default values.
     * <p/>
     * All instantiated objects must have no-arg constructors.
     *
     * @param key object key to read
     * @return the saved object instance or null
     */
    public <T> T read(String key) {
        return read(key, null);
    }

    public <T> T read(Class clazz) {
        return read(clazz.getName(), null);
    }

    public <T> T read(Class clazz, T defaultValue) {
        return read(clazz.getName(), defaultValue);
    }

    /**
     * Instantiates saved object using original object class (e.g. LinkedList). Support limited
     * backward and forward compatibility: removed fields are ignored, new fields have their
     * default values.
     * <p/>
     * All instantiated objects must have no-arg constructors.
     *
     * @param key          object key to read
     * @param defaultValue will be returned if key doesn't exist
     * @return the saved object instance or null
     */
    public <T> T read(String key, T defaultValue) {
        T value = mStorage.select(key);
        return value == null ? defaultValue : value;
    }


    /**
     * Check if an object with the given key is saved in Chest storage.
     *
     * @param key object key
     * @return true if object with given key exists in Chest storage, false otherwise
     */
    public boolean exist(String key) {
        return mStorage.exist(key);
    }

    /**
     * Delete saved object for given key if it is exist.
     *
     * @param key object key
     */
    public void delete(String key) {
        mStorage.deleteIfExists(key);
    }

    public void deleteAll() {
        for (String key : getAllKeys())
            delete(key);
    }

    /**
     * Returns all keys for objects in chest.
     *
     * @return all keys
     */
    public List<String> getAllKeys() {
        return mStorage.getAllKeys();
    }

    /**
     * add data change callback to callbacks
     *
     * @param callback data change callback
     */
    public void addOnDataChangeListener(DataChangeCallback callback) {
        if (mCallbacks == null)
            mCallbacks = new ArrayList<>();
        mCallbacks.add(callback);
    }

    /**
     * remove all listener from this object
     *
     * @param object Object with listeners
     */
    public void removeListener(Object object) {
        Iterator<DataChangeCallback> i = mCallbacks.iterator();
        while (i.hasNext()) {
            DataChangeCallback callback = i.next();
            if (callback.getClassName().equals(object.getClass().getName()))
                i.remove();
        }
    }

    /**
     * call all data change callbacks
     */
    @SuppressWarnings("unchecked")
    public <T> void callCallbacks(String key, T value) {
        if (mCallbacks != null) {
            for (DataChangeCallback callback : mCallbacks) {
                if (callback.getType() != null && callback.getType().isInstance(value)) {
                    Class clazz = null;
                    if (callback.getType().equals(List.class)) {
                        List<T> values = (List) value;
                        if (values.size() > 0)
                            clazz = values.get(0).getClass();
                    }
                    if (callback.getKey() != null) {
                        if (callback.getKey().equals(key)) {
                            callback.onDataChange(value);
                            callback.onDataChange(key, value);
                        }
                    } else if (callback.getValues() != null) {
                        for (Enum enumValue : callback.getValues()) {
                            if (enumValue.toString().equals(key)) {
                                callback.onDataChange(key, value);
                                callback.onDataChange(value);
                            }
                        }
                    } else {
                        callback.onDataChange(key, value);
                        callback.onDataChange(value);
                        if (clazz != null)
                            callback.onDataChange(clazz, value);
                    }
                } else if (callback.getType() == null) {
                    if (callback.getKey() != null) {
                        if (callback.getKey().equals(key)) {
                            callback.onDataChange(value);
                            callback.onDataChange(key, value);
                        }
                    } else {
                        callback.onDataChange(key, value);
                        callback.onDataChange(value);
                    }
                }
            }
        }
    }

    /**
     * load objects from loader extension
     *
     * @param ironLoadExtension extension
     * @param call               extension call
     * @param key                key to save
     */
    public <T> void load(IronLoadExtension ironLoadExtension, T call, String key) {
        ironLoadExtension.load(call, key);
    }

    public <T> void load(IronLoadExtension ironLoadExtension, T call, Class clazz) {
        load(ironLoadExtension, call, clazz.getName());
    }
}
