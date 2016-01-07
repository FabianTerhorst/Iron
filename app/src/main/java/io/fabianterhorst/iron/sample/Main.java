package io.fabianterhorst.iron.sample;

import java.util.ArrayList;

import io.fabianterhorst.iron.annotations.Name;
import io.fabianterhorst.iron.annotations.Store;

@Store
public class Main {
    @Name(value = "contributors", transaction = true, listener = true, loader = true, async = true)
    ArrayList<Contributor> contributors;

    @Name("repos")
    ArrayList<Contributor> repos;

    @Name("username")
    String userName;
}