package io.fabianterhorst.iron.sample;

import android.app.Application;

import io.fabianterhorst.iron.Cache;
import io.fabianterhorst.iron.Iron;
import io.fabianterhorst.iron.retrofit.IronRetrofit;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //Todo : Iron builder
        Iron.init(getApplicationContext());
        Iron.setCache(Cache.MEMORY);
        Iron.setLoader(new IronRetrofit());
        //Iron.setEncryption(new IronEncryption());
    }
}
