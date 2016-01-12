package io.fabianterhorst.iron.sample;

import java.util.ArrayList;

import io.fabianterhorst.iron.annotations.DefaultLong;
import io.fabianterhorst.iron.annotations.DefaultObject;
import io.fabianterhorst.iron.annotations.Name;
import io.fabianterhorst.iron.annotations.Store;

@Store
public class Main {
    @DefaultObject
    @Name(value = "contributors", transaction = true, listener = true, loader = true, async = true)
    ArrayList<Contributor> contributors;

    @DefaultObject
    @Name("repos")
    ArrayList<Contributor> repos;

    @Name("my_username")
    String userName;

    @DefaultLong(120)
    Long bla;
}