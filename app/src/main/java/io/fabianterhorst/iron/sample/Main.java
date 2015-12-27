package io.fabianterhorst.iron.sample;

import java.util.ArrayList;

import io.fabianterhorst.iron.compiler.Name;
import io.fabianterhorst.iron.compiler.Store;

@Store
public class Main {
    @Name(value = "contributors", transaction = true, listener = true)
    ArrayList<Contributor> contributors;

    @Name("repos")
    ArrayList<Contributor> repos;

    @Name("username")
    String userName;
}