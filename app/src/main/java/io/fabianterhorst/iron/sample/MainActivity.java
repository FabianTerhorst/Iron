package io.fabianterhorst.iron.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import io.fabianterhorst.iron.Chest;
import io.fabianterhorst.iron.DataChangeCallback;
import io.fabianterhorst.iron.Iron;
import retrofit2.Call;
import retrofit2.MoshiConverterFactory;
import retrofit2.Retrofit;


public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainStore.setContributors(new ArrayList<Contributor>());

        MainStore.getContributors(new Chest.ReadCallback<ArrayList<Contributor>>() {
            @Override
            public void onResult(ArrayList<Contributor> contributors) {
                if(contributors != null)
                    Log.d("size", contributors.size() + "");
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(MoshiConverterFactory.create())
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
                    Log.d(TAG, ((ArrayList<Contributor>)value).toString()); // get contributors
                }
            }
        });

        MainStore.addOnDataChangeListener(new DataChangeCallback(this) {
            @Override
            public void onDataChange(String key, Object value) {
                if(key.equals(MainStore.Keys.CONTRIBUTORS.toString()))
                    Log.d(TAG, "contributors change listener mainstore all");
            }
        });

        MainStore.getContributorsForField("login", "fabianterhorst", new Chest.ReadCallback<Contributor>() {
            @Override
            public void onResult(Contributor contributor) {
                if(contributor != null)
                    Log.d(TAG, contributor.toString() + " single");
            }
        });

        MainStore.addOnContributorsDataChangeListener(new DataChangeCallback<ArrayList<Contributor>>(this) {
            @Override
            public void onDataChange(ArrayList<Contributor> value) {
                Log.d(TAG, "contributors change listener mainstore");
            }
        });

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
                    Log.d(TAG, contributor.getName());
                }
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
