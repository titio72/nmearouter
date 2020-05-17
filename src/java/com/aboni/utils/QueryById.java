package com.aboni.utils;

public class QueryById implements Query {

    private final int id;

    public QueryById(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
