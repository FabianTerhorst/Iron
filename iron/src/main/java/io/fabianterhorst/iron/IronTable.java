package io.fabianterhorst.iron;

class IronTable<T> {

    private T mContent;

    @SuppressWarnings("UnusedDeclaration")
    IronTable() {
    }

    IronTable(T content) {
        mContent = content;
    }

    public T getContent() {
        return mContent;
    }
}
