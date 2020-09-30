/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.conf;

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
