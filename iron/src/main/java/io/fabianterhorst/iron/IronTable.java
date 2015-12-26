package io.fabianterhorst.iron;

class IronTable<T> {

    @SuppressWarnings("UnusedDeclaration")
    IronTable() {
    }

    IronTable(T content) {
        mContent = content;
    }

    // Serialized content
    T mContent;
}
