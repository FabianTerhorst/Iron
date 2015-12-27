<img src="https://img.shields.io/badge/Methods and size-core: 198 | deps: 9494 | 21 KB-e91e63.svg"></img>
<img src="https://travis-ci.org/FabianTerhorst/Iron.svg?branch=master"></img>
[![Join the chat at https://gitter.im/FabianTerhorst/Iron](https://badges.gitter.im/FabianTerhorst/Iron.svg)](https://gitter.im/FabianTerhorst/Iron?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Iron

Fast and easy to use NoSQL data storage

#### Add dependency
```groovy
classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
...
apply plugin: 'com.neenbedankt.android-apt'
...
compile 'io.fabianterhorst:iron:0.1'
compile 'io.fabianterhorst:iron-retrofit:0.1'
apt 'io.fabianterhorst:iron-compiler:0.1'
```

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Iron.init(getApplicationContext());
    }
}
```

Use the @Store annotation on any plain old Java object.

```java
@Store
public class Main {
    @Name("contributors")
    ArrayList<Contributor> contributors;
}
```
Now you can access the generated Methods from your Main + "Store" file.

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
 Iron.chest().addOnDataChangeListener(new DataChangeCallback(this) {
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
 MainStore.executeContributorsTransaction(new Chest.Transaction<ArrayList<Contributor>>() {
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
MainStore.getContributorsForField("login", "fabianterhorst", new Chest.ReadCallback<Contributor>() {
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

### License
    Copyright 2015 Fabian Terhorst

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.