package io.fabianterhorst.iron.retrofit;

import io.fabianterhorst.iron.Iron;
import io.fabianterhorst.iron.IronLoadExtension;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class IronRetrofit implements IronLoadExtension {
    @SuppressWarnings("unchecked")
    public <T> void load(T call, final String key) {
        if (call instanceof Call) {
            ((Call<T>) call).enqueue(new Callback<T>() {
                @Override
                public void onResponse(Response<T> response, Retrofit retrofit) {
                    if (response.body() != null)
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
