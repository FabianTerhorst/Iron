package io.fabianterhorst.iron.testdata;

public class ClassWithoutPublicNoArgConstructor {

    private String mName;

    public ClassWithoutPublicNoArgConstructor(String name) {
        this.mName = name;
    }

    public String getName() {
        return mName;
    }
}
