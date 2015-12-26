package io.fabianterhorst.iron.sample;

import java.util.ArrayList;

import io.fabianterhorst.iron.compiler.Name;
import io.fabianterhorst.iron.compiler.Store;

@Store
public class Main {
    @Name("contributors")
    ArrayList<Contributor> contributors;

    @Name("repos")
    ArrayList<Contributor> repos;

    @Name("username")
    String userName;
}