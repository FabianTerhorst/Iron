package io.fabianterhorst.iron.retrofit;

import io.fabianterhorst.iron.Iron;
import io.fabianterhorst.iron.Loader;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IronRetrofit implements Loader {
    @SuppressWarnings("unchecked")
    public <T> void load(T call, final String key) {
        if (call instanceof Call) {
            ((Call<T>) call).enqueue(new Callback<T>() {
                @Override
                public void onResponse(Response<T> response) {
                    if (response.isSuccess())
                        Iron.chest().write(key, response.body());
                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }
}
