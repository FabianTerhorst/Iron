package io.fabianterhorst.iron.sample;

public class Contributor {

    private String login;

    public String getName(){
        return login;
    }

    public void setName(String name){
        this.login = name;
    }
}
