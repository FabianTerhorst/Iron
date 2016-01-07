package io.fabianterhorst.iron.sample;

import android.app.Application;

import io.fabianterhorst.iron.Iron;
import io.fabianterhorst.iron.encryption.IronEncryption;
import io.fabianterhorst.iron.retrofit.IronRetrofit;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Iron.init(getApplicationContext());
        Iron.setLoaderExtension(new IronRetrofit());
        Iron.setEncryptionExtension(new IronEncryption());
    }
}
