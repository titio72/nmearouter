package com.aboni.nmea.router.conf.net;

public class NetConf {
    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isTx() {
        return tx;
    }

    public void setTx(boolean tx) {
        this.tx = tx;
    }

    public boolean isRx() {
        return rx;
    }

    public void setRx(boolean rx) {
        this.rx = rx;
    }

    public NetConf(String server, int port, boolean rx, boolean tx) {
        this.server = server;
        this.port = port;
        this.rx = rx;
        this.tx = tx;
    }

    private String server;
    private int port;
    private boolean tx;
    private boolean rx;
}
