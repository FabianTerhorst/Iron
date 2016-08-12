package io.fabianterhorst.iron;

import android.app.Application;
import android.content.Context;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Fast NoSQL data storage with auto-upgrade support to save any types of Plain Old Java Objects or
 * collections using Kryo serialization.
 * <p/>
 * Every custom class must have no-arg constructor. Common classes supported out of the box.
 * <p/>
 * Auto upgrade works in a way that removed object's fields are ignored on read and new fields
 * have their default values on create class instance.
 * <p/>
 * Each object is saved in separate Iron file with name like object_key.pt.
 * All Iron files are created in the /files/io.iron dir in app's private storage.
 */
public class Iron {
    public static final String DEFAULT_DB_NAME = "io.iron";

    private static Context mContext;

    private static Loader mLoader;

    private static Encryption mEncryption;

    private static int mCache = Cache.NONE;

    private static final ConcurrentHashMap<String, Chest> mChestMap = new ConcurrentHashMap<>();

    /**
     * Lightweight method to init Iron instance. Should be executed in {@link Application#onCreate()}
     *
     * @param context context, used to get application context
     */
    public static void init(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Set the loader to the Iron instance
     *
     * @param loader loader extension for the iron instance
     */
    public static void setLoader(Loader loader) {
        mLoader = loader;
    }

    /**
     * Set the encryption to the Iron instance
     *
     * @param encryption encryption extension for the iron instance
     */
    public static void setEncryption(Encryption encryption) {
        mEncryption = encryption;
    }

    /**
     * Set the cache strategy to store the written objects in memory
     *
     * @param cache the cache strategy Cache#Memory or Cache#NONE
     */
    public static void setCache(int cache) {
        mCache = cache;
    }

    /**
     * Returns Iron chest instance with the given name
     *
     * @param name name of new database
     * @return Iron instance
     */
    public static Chest chest(String name) {
        if (name.equals(DEFAULT_DB_NAME)) throw new IronException(DEFAULT_DB_NAME +
                " name is reserved for default library name");
        return getChest(name);
    }

    /**
     * Returns default iron chest instance
     *
     * @return Chest instance
     */
    public static Chest chest() {
        return getChest(DEFAULT_DB_NAME);
    }

    private static Chest getChest(String name) {
        if (mContext == null) {
            throw new IronException("Iron.init is not called");
        }
        synchronized (mChestMap) {
            Chest chest = mChestMap.get(name);
            if (chest == null) {
                chest = new Chest(mContext, name, mLoader, mEncryption, mCache);
                mChestMap.put(name, chest);
            }
            return chest;
        }
    }

    /**
     * @deprecated use Iron.chest().write()
     */
    public static <T> Chest put(String key, T value) {
        return chest().write(key, value);
    }

    /**
     * @deprecated use Iron.chest().read()
     */
    public static <T> T get(String key) {
        return chest().read(key);
    }

    /**
     * @deprecated use Iron.chest().read()
     */
    public static <T> T get(String key, T defaultValue) {
        return chest().read(key, defaultValue);
    }

    /**
     * @deprecated use Iron.chest().exist()
     */
    public static boolean exist(String key) {
        return chest().exist(key);
    }

    /**
     * @deprecated use Iron.chest().delete()
     */
    public static void delete(String key) {
        chest().delete(key);
    }

    /**
     * @deprecated use Iron.chest().destroy(). NOTE: Iron.init() be called
     * before destroy()
     */
    public static void clear(Context context) {
        init(context);
        chest().destroy();
    }
}
