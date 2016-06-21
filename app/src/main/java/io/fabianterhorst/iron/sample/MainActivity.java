package io.fabianterhorst.iron.sample;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import io.fabianterhorst.iron.Chest;
import io.fabianterhorst.iron.DataChangeCallback;
import io.fabianterhorst.iron.Iron;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import rx.Observable;
import rx.Subscriber;


public class MainActivity extends RxAppCompatActivity {

    private final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainStore.getContributors(new Chest.ReadCallback<ArrayList<Contributor>>() {
            @Override
            public void onResult(ArrayList<Contributor> contributors) {
                Log.d("size", contributors.size() + "");
            }
        });

        ArrayList<Contributor> contributors = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Contributor contributor = new Contributor();
            contributor.setName("name" + i);
            contributors.add(contributor);
        }

        MainStore.addContributors(contributors);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        GitHubService service = retrofit.create(GitHubService.class);

        Iron.chest().addOnDataChangeListener(new DataChangeCallback<List<Repo>>(this, Repo.class) {
            @Override
            public void onDataChange(List<Repo> repos) {
                for (Repo repo : repos) {
                    Log.d(TAG, repo.getName());
                }
            }
        });

        Call<List<Repo>> reposCall = service.listRepos("fabianterhorst");

        Iron.chest().load(reposCall, Repo.class);

        Iron.chest().addOnDataChangeListener(new DataChangeCallback(this) {
            @Override
            public void onDataChange(String key, Object value) {
                if (key.equals(MainStore.Keys.CONTRIBUTORS.toString())) {
                    Log.d(TAG, ((ArrayList<Contributor>) value).toString()); // get contributors
                }
            }
        });

        MainStore.addOnDataChangeListener(new DataChangeCallback(this) {
            @Override
            public void onDataChange(String key, Object value) {
                if (key.equals(MainStore.Keys.CONTRIBUTORS.toString()))
                    Log.d(TAG, "contributors change listener mainstore all");
            }
        });

        MainStore.getContributorsForField("login", "fabianterhorst", new Chest.ReadCallback<Contributor>() {
            @Override
            public void onResult(Contributor contributor) {
                if (contributor != null)
                    Log.d(TAG, contributor.toString() + " single");
            }
        });

        MainStore.addOnContributorsDataChangeListener(new DataChangeCallback<ArrayList<Contributor>>(this) {
            @Override
            public void onDataChange(ArrayList<Contributor> value) {
                Log.d(TAG, "contributors change listener mainstore");
            }
        });

        Contributor contributor = new Contributor();
        contributor.setName("test");
        MainStore.addContributor(contributor);

        MainStore.executeContributorsTransaction(new Chest.Transaction<ArrayList<Contributor>>() {
            @Override
            public void execute(ArrayList<Contributor> contributors) {
                Contributor contributor = new Contributor();
                contributor.setName("fabianterhorst");
                contributors.add(contributor);
            }
        });

        Iron.chest().addOnDataChangeListener(new DataChangeCallback<List<Contributor>>(this, Contributor.class) {
            @Override
            public void onDataChange(List<Contributor> contributors) {
                for (Contributor contributor : contributors) {
                    if (contributor.getName() != null)
                        Log.d(TAG, contributor.getName());
                }
            }
        });

        Iron.chest().set("rxjava", "bla");

        Iron.chest().<String>get("rxjava").compose(this.<String>bindToLifecycle()).subscribe(new Subscriber<String>() {

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(String s) {
                Log.d("change", s);
            }

            @Override
            public void onCompleted() {

            }
        });

        Iron.chest().set("rxjava", "bla2");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Iron.chest().set("rxjava", "bla3");
            }
        }, 2000);


        ArrayList<String> names = new ArrayList<>();
        names.add("Name1");

        Iron.chest().set("names", names);

        Iron.chest().<ArrayList<String>>get("names").compose(this.<ArrayList<String>>bindToLifecycle()).subscribe(new Subscriber<ArrayList<String>>() {

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(ArrayList<String> s) {
                Log.d("change list", s.size() + "");
            }

            @Override
            public void onCompleted() {

            }
        });

        names.add("Name2");

        Iron.chest().set("names", names);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> names = new ArrayList<>();
                names.add("Name3");
                Iron.chest().set("names", names);
            }
        }, 2000);

        Observable<List<Repo>> reposCallObservable = service.listReposRxJava("fabianterhorst");

        Iron.chest().load(reposCallObservable, Repo.class);

        Iron.chest().addOnDataChangeListener(new DataChangeCallback<List<Repo>>(this, Repo.class) {
            @Override
            public void onDataChange(List<Repo> repos) {
                Log.d("repos size", repos.size() + "");
            }
        });

        Iron.chest().<ArrayList<String>>execute("names").compose(this.<ArrayList<String>>bindToLifecycle()).subscribe(new Subscriber<ArrayList<String>>() {

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onCompleted() {
                Log.d("names size", Iron.chest().<ArrayList<String>>read("names").size() + "");
            }

            @Override
            public void onNext(ArrayList<String> strings) {
                Log.d("names size", strings.size() + "");
                strings.add("Name4");
            }
        });

        //Call<List<Contributor>> userCall = service.contributors("fabianterhorst", "iron");
        //MainStore.loadContributors(userCall);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Iron.chest().load(new Retrofit.Builder()
                    .baseUrl("https://api.github.com")
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
                    .create(GitHubService.class)
                    .listRepos("fabianterhorst"), Repo.class);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainStore.removeListener(this);
        //Iron.chest().removeListener(this);
    }
}
