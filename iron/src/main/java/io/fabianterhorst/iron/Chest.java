package io.fabianterhorst.iron;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Chest {
    private final Storage mStorage;
    private final ObjectStorage mObjectStorage;

    private final transient ArrayList<DataChangeCallback> mCallbacks = new ArrayList<>();

    private final Loader mLoader;

    public interface Transaction<T> {
        void execute(T value);
    }

    public interface ReadCallback<T> {
        void onResult(T value);
    }

    protected Chest(Context context, String dbName, Loader loader, Encryption encryption, int cache, int storage) {
        mStorage = new DbStoragePlainFile(context.getApplicationContext(), dbName, encryption, cache);
        if(storage == Storage.OBJECT)
            mObjectStorage = new ObjectStorage(mStorage);
        else
            mObjectStorage = null;
        mLoader = loader;
    }

    public void add(String key, Object object){
        if(mObjectStorage == null)
            throw new IronException("You have to set Iron.setStorage(Storage.Object) in your Application create()");
        mObjectStorage.add(key, object);
    }

    public <T> T get(String key){
        if(mObjectStorage == null)
            throw new IronException("You have to set Iron.setStorage(Storage.Object) in your Application create()");
        return mObjectStorage.get(key);
    }

    public void save(){
        if(mObjectStorage == null)
            throw new IronException("You have to set Iron.setStorage(Storage.Object) in your Application create()");
        mObjectStorage.save();
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
        if (Build.VERSION.SDK_INT > 10) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, key, value);
        } else {
            task.execute(key, value);
        }
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
    public <T> void get(String key, ReadCallback<T> readCallback) {
        get(key, readCallback, null);
    }

    /**
     * Instantiates saved object using original object class (e.g. LinkedList). Support limited
     * backward and forward compatibility: removed fields are ignored, new fields have their
     * default values.
     * <p/>
     * All instantiated objects must have no-arg constructors.
     *
     * @param key           object key to read
     * @param readCallback  callback that return the readed object
     * @param defaultObject return the defaultObject if readed object is null
     */
    public <T> void get(String key, ReadCallback<T> readCallback, Object defaultObject) {
        AsyncTask<Object, Void, T> task = new AsyncTask<Object, Void, T>() {

            protected ReadCallback<T> mReadCallback;

            @Override
            protected T doInBackground(Object... objects) {
                String key = (String) objects[0];
                mReadCallback = (ReadCallback<T>) objects[1];
                T defaultObject = null;
                if (objects.length > 2)
                    defaultObject = (T) objects[2];
                return read(key, defaultObject);
            }

            @Override
            protected void onPostExecute(T value) {
                mReadCallback.onResult(value);
            }
        };
        if (Build.VERSION.SDK_INT > 10) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, key, readCallback, defaultObject);
        } else {
            task.execute(key, readCallback, defaultObject);
        }
    }

    /**
     * Execute a transaction in this you can modify the data from the parameter with a data saving after modifying is finished
     *
     * @param key           data key
     * @param transaction   transaction
     * @param defaultObject default object if value is null
     */
    public <T> void execute(String key, Transaction<T> transaction, Object defaultObject) {
        AsyncTask<Object, Void, Void> task = new AsyncTask<Object, Void, Void>() {

            @Override
            protected Void doInBackground(Object... objects) {
                String key = (String) objects[0];
                Transaction<T> transaction = (Transaction<T>) objects[1];
                T defaultObject = (T) objects[2];
                T value = read(key);
                if (value == null)
                    value = defaultObject;
                transaction.execute(value);
                if (value != null)
                    write(key, value);
                return null;
            }
        };
        if (Build.VERSION.SDK_INT > 10) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, key, transaction, defaultObject);
        } else {
            task.execute(key, transaction, defaultObject);
        }
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
        if (Build.VERSION.SDK_INT > 10) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, key);
        } else {
            task.execute(key);
        }
    }

    public void removeAll() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                deleteAll();
                return null;
            }
        };
        if (Build.VERSION.SDK_INT > 10) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
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
        mCallbacks.add(callback);
    }

    /**
     * remove all listener from this object
     *
     * @param object Object with listeners
     */
    synchronized public void removeListener(Object object) {
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
            synchronized (mCallbacks) {
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
    }

    /**
     * load objects from loader
     *
     * @param call extension call
     * @param key  key to save
     */
    public <T> void load(T call, String key) {
        if (mLoader == null)
            throw new IronException("To use load() you have to set the loader in your application onCreate() with Iron.setLoader(new IronRetrofit())");
        mLoader.load(call, key);
    }

    /**
     * load objects from loader
     *
     * @param call  call
     * @param clazz classname to save
     */
    public <T> void load(T call, Class clazz) {
        load(call, clazz.getName());
    }

    /**
     * Clears cache for given key.
     *
     * @param key object key
     */
    public void invalidateCache(String key) {
        mStorage.invalidateCache(key);
    }

    /**
     * Clears cache.
     */
    public void invalidateCache() {
        mStorage.invalidateCache();
    }
}
