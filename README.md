# Iron
Fast and easy to use NoSQL data storage

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Iron.init(getApplicationContext());
    }
}
```

```java
@Store
public class Main {
    @Name("contributors")
    ArrayList<Contributor> contributors;
}
```

```java
 MainStore.setContributors(contributors); // set contributors
```

```java
 MainStore.getContributors(contributors); // get contributors
```

```java
 MainStore.removeContributors(); // remove contributors
```

```java
 Paper.book().addOnDataChangeListener(new DataChangeCallback(this) {
    @Override
    public void onDataChange(String key, Object value) {
        if(key.equals(MainStore.Keys.CONTRIBUTORS.toString())){
            Log.d(TAG, MainStore.getContributors().toString()); // get contributors
        }
    }
});
```

```java
MainStore.getContributors(new Chest.ReadCallback<ArrayList<Contributor>>() {
    @Override
    public void onResult(ArrayList<Contributor> contributors) {
    }
});
```

```java
 MainStore.executeContributorsTransaction(new Book.Transaction<ArrayList<Contributor>>() {
    @Override
    public void execute(ArrayList<Contributor> contributors) {
    	Contributor contributor = new Contributor();
    	contributor.setName("fabianterhorst");
        contributors.add(contributor);
    }
});
```

```java
MainStore.addOnContributorsDataChangeListener(new DataChangeCallback<ArrayList<Contributor>>(this) {
    @Override
    public void onDataChange(ArrayList<Contributor> value) {
    }
});
```

```java
MainStore.addOnDataChangeListener(new DataChangeCallback(this) {
    @Override
    public void onDataChange(String key, Object value) {
        if(key.equals(MainStore.Keys.CONTRIBUTORS.toString()))
            //contributors changed
    }
});
```

```java
MainStore.getContributorsForField("login", "fabianterhorst", new Book.ReadCallback<Contributor>() {
    @Override
    public void onResult(Contributor contributor) {
        if(contributor != null)
            Log.d(TAG, contributor.toString());
    }
});
```

```java
@Override
protected void onDestroy() {
    super.onDestroy();
    MainStore.removeListener(this);
    //Iron.chest().removeListener(this);
}
```

Retrofit support
```java
Call<List<Contributor>> userCall = service.contributors("fabianterhorst", "iron");
MainStore.loadContributors(new IronRetrofit(), userCall);
```

with Cache:

Running [Benchmark](https://github.com/fabianterhorst/Iron/blob/master/iron/src/androidTest/java/io/fabianterhorst/iron/benchmark/Benchmark.java) on Nexus 6p, in ms:

| Benchmark                 | Iron    | [Hawk](https://github.com/orhanobut/hawk) | [sqlite](http://developer.android.com/reference/android/database/sqlite/package-summary.html) |
|---------------------------|----------|----------|----------|
| Read/write 500 contacts   | 29      | 142      |          |
| Write 500 contacts        | 27      | 60      |          |
| Read 500 contacts         | 0       | 63      |          |

Running [Benchmark](https://github.com/fabianterhorst/Iron/master/iron/src/androidTest/java/io/fabianterhorst/iron/benchmark/Benchmark.java) on Emulator, in ms:

| Benchmark                 | Iron    | [Hawk](https://github.com/orhanobut/hawk) | [sqlite](http://developer.android.com/reference/android/database/sqlite/package-summary.html) |
|---------------------------|----------|----------|----------|
| Read/write 500 contacts   | 12      | 54      |          |
| Write 500 contacts        | 11      | 25      |          |
| Read 500 contacts         | 0       | 24      |          |