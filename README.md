<img src="https://travis-ci.org/FabianTerhorst/Iron.svg?branch=master"></img>
[![Join the chat at https://gitter.im/FabianTerhorst/Iron](https://badges.gitter.im/FabianTerhorst/Iron.svg)](https://gitter.im/FabianTerhorst/Iron?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Iron-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/2990)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/c0703e14011042bf9233a2604af85636)](https://www.codacy.com/app/fabian-terhorst/Iron)
<a href="http://www.methodscount.com/?lib=io.fabianterhorst%3Airon%3A0.6.4"><img src="https://img.shields.io/badge/Size-28 KB-e91e63.svg"></img></a>
<img src="https://img.shields.io/bintray/v/fabianterhorst/maven/iron.svg?label=Core"></img>
<img src="https://img.shields.io/bintray/v/fabianterhorst/maven/iron-compiler.svg?label=Compiler"></img>
<img src="https://img.shields.io/bintray/v/fabianterhorst/maven/iron-retrofit.svg?label=Retrofit"></img>
<img src="https://img.shields.io/bintray/v/fabianterhorst/maven/iron-annotations.svg?label=Annotations"></img>
<img src="https://img.shields.io/bintray/v/fabianterhorst/maven/iron-encryption.svg?label=Encryption"></img>
<img src="https://img.shields.io/github/license/fabianterhorst/iron.svg"></img>

# Iron

Fast and easy to use NoSQL data storage with RxJava and Kotlin support

Android sdk version 8 support

#### Add dependency

Add apt plugin in your top level gradle build file

```groovy
classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
```

Apply apt plugin in your application gradle build file.

```groovy
apply plugin: 'com.neenbedankt.android-apt'
```

Add dependencies to your application gradle build file

The Core
```groovy
compile 'io.fabianterhorst:iron:0.8.0'
```
The Extensions
```groovy
compile 'io.fabianterhorst:iron-retrofit:0.8.0'
compile 'io.fabianterhorst:iron-encryption:0.8.0'
//is only required for using the compiler
compile 'io.fabianterhorst:iron-annotations:0.8.0'
apt 'io.fabianterhorst:iron-compiler:0.8.0'
```

Initiate Iron instance with application context

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Iron.init(getApplicationContext());
        Iron.setCache(Cache.MEMORY);//default is NONE
        //Optional if iron-retrofit is included
        Iron.setLoader(new IronRetrofit());
        //Optional if iron-encryption is included
        Iron.setEncryption(new IronEncryption());
    }
}
```

Use the @Store annotation on any plain old Java object.

```java
@Store
public class Main {
    @DefaultObject
    @Name(value = "contributor_list", transaction = true, listener = true, loader = true, async = true)
    ArrayList<Contributor> contributors;

    String userName;

    @DefaultLong(120)
    Long myLong;
}
```
Now you can access the generated Methods from your Main + "Store" file.

```java
 MainStore.setContributors(contributors);
```

Get value synced

```java
 MainStore.getContributors(contributors);
```

Get value asynced

```java
MainStore.getContributors(new Chest.ReadCallback<ArrayList<Contributor>>() {
    @Override
    public void onResult(ArrayList<Contributor> contributors) {
    }
});
```

Remove value

```java
 MainStore.removeContributors();
```

```java
 Iron.chest().addOnDataChangeListener(new DataChangeCallback(this) {
    @Override
    public void onDataChange(String key, Object value) {
        if(key.equals(MainStore.Keys.CONTRIBUTORS.toString())){
            Log.d(TAG, ((ArrayList<Contributor>)value).toString());
        }
    }
});
```

transactions (changes will be saved)

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
Use a internal transaction to add a object to the list and save it automatically

```java
Contributor contributor = new Contributor();
contributor.setName("test");
MainStore.addContributor(contributor);
```

Use a internal transaction to add objects to the list and save it automatically

```java
ArrayList<Contributor> contributors = new ArrayList<>();
for(int i = 0;i < 10;i++){
    Contributor contributor = new Contributor();
    contributor.setName("name" + i);
    contributors.add(contributor);
}
MainStore.addContributors(contributors);
```


Data change listener

```java
MainStore.addOnContributorsDataChangeListener(new DataChangeCallback<ArrayList<Contributor>>(this) {
    @Override
    public void onDataChange(ArrayList<Contributor> value) {
    }
});
```

Generic data change listener

```java
MainStore.addOnDataChangeListener(new DataChangeCallback(this) {
    @Override
    public void onDataChange(String key, Object value) {
        if(key.equals(MainStore.Keys.CONTRIBUTORS.toString()))
            //contributors changed
    }
});
```

Search for object with field and value

```java
MainStore.getContributorsForField("login", "fabianterhorst", new Chest.ReadCallback<Contributor>() {
    @Override
    public void onResult(Contributor contributor) {
        if(contributor != null)
            Log.d(TAG, contributor.toString());
    }
});
```

####RxJava support

Set a value asynchron with RxJava

```java
Iron.chest().set("name", "Fabian");
```

Get a value asynchron with RxJava

```java
Iron.chest().<String>get("name").compose(this.<String>bindToLifecycle()).subscribe(new Subscriber<String>() {

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(String s) {
                //called when name changed and when name was loaded asynchron
                Log.d("name", s);
            }

            @Override
            public void onCompleted() {

            }
        });
```

With RxJava the Retrofit extension isn´t needed anymore.

```java
Observable<List<Repo>> reposCallObservable = service.listReposRxJava("fabianterhorst");
Iron.chest().load(reposCallObservable, Repo.class);
```

You can also use Iron.chest()´s methods.

```java
Iron.chest().write("username", "fabian");
```

Read data objects. Iron instantiates exactly the classes which has been used in saved data. The limited backward and forward compatibility is supported.

```java
String username = Iron.chest().read("username");
```

Laod and save data with retrofit. Need loader extension to be added in application.

```java
Call<List<Repo>> reposCall = service.listRepos("fabianterhorst");
Iron.chest().load(reposCall, Repo.class);
```

Get value asynced with default value

```java
Iron.chest().get("contributors", new Chest.ReadCallback<ArrayList<Contributor>>() {
    @Override
    public void onResult(ArrayList<Contributor> contributors) {      
    }
}, new ArrayList<Contributor>());
```

Remove listener to prevent memory leaks

```java
@Override
protected void onDestroy() {
    super.onDestroy();
    MainStore.removeListener(this);
    //Iron.chest().removeListener(this);
}
```

#### Handle data structure changes
Class fields which has been removed will be ignored on restore and new fields will have their default values. For example, if you have following data class saved in Paper storage:

```java
class User {
    public String name; //Fabian
    public boolean isActive;
}
```

And then you realized you need to change the class like:

```java
class User {
    public String name; //Fabian
    // public boolean isActive; removed field
    public Location location; // New field
}
```

Then on restore the _isActive_ field will be ignored and new _location_ field will have its default value _null_.

Retrofit support
```java
Call<List<Contributor>> userCall = service.contributors("fabianterhorst", "iron");
MainStore.loadContributors(userCall);
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

with Encryption (only in Iron):

Running [Benchmark](https://github.com/fabianterhorst/Iron/master/iron/src/androidTest/java/io/fabianterhorst/iron/benchmark/Benchmark.java) on Nexus 6p, in ms:

| Benchmark                 | Iron    | [Hawk](https://github.com/orhanobut/hawk) | [sqlite](http://developer.android.com/reference/android/database/sqlite/package-summary.html) |
|---------------------------|----------|----------|----------|
| Read/write 500 contacts   | 53      | 142      |          |
| Write 500 contacts        | 28      | 61      |          |
| Read 500 contacts         | 23       | 63      |          |


#### Snapshot Builds

Add the jitpack repository in your root build.gradle at the end of repositories
```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

```groovy
//Latest commit
compile 'com.github.FabianTerhorst:Iron:-SNAPSHOT'
```

### License
    Copyright 2016 Fabian Terhorst

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.