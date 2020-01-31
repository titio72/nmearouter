package com.aboni.utils.db;

public class DBException extends Exception {

    public DBException(String msg) {
        super(msg);
    }

    public DBException(String msg, Throwable src) {
        super(msg, src);
    }

}
