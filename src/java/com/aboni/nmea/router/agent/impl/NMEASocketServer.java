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

package com.aboni.nmea.router.agent.impl;

import com.aboni.log.Log;
import com.aboni.log.LogStringBuilder;
import com.aboni.nmea.nmea0183.Message2NMEA0183;
import com.aboni.nmea.nmea0183.NMEA0183Message;
import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.nmea.router.conf.NetConf;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.utils.TimestampProvider;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class NMEASocketServer extends NMEAAgentImpl {

    public static final String TCP_SERVER_CATEGORY = "TCPServer";
    public static final String CLIENT_KEY_NAME = "client";
    private int port = -1;
    private Selector selector;
    private ServerSocketChannel serverSocket;
    private final ByteBuffer writeBuffer;
    private final ByteBuffer readBuffer;
    private final Map<SocketChannel, ClientDescriptor> clients;
    private final Message2NMEA0183 converter;
    private SentenceSerializer serializer;

    public interface SentenceSerializer {
        String getOutSentence(Sentence s);
    }

    private static class ClientDescriptor {

        ClientDescriptor(String ip) {
            this.ip = ip;
        }

        final String ip;

        int errors = 0;

        @Override
        public String toString() {
            return ip;
        }
    }

    @Inject
    public NMEASocketServer(TimestampProvider tp, Log log, RouterMessageFactory messageFactory, Message2NMEA0183 converter) {
        super(log, tp, messageFactory, false, true);
        if (converter==null) throw new IllegalArgumentException("NMEA converter cannot be null");
        this.converter = converter;
        writeBuffer = ByteBuffer.allocate(16384);
        readBuffer = ByteBuffer.allocate(16384);
        clients = new HashMap<>();
    }

    public void setup(String name, QOS qos, NetConf conf, SentenceSerializer serializer) {
        if (port == -1) {
            setup(name, qos);
            setSourceTarget(conf.isRx(), conf.isTx());
            port = conf.getPort();
            getLog().info(LogStringBuilder.start(TCP_SERVER_CATEGORY).wO("setup").wV("port", port).wV("rx", conf.isRx())
                    .wV("tx", conf.isTx()).toString());
            this.serializer = serializer;
        } else {
            getLog().warning(LogStringBuilder.start(TCP_SERVER_CATEGORY).wO("setup").wV("error", "already setup").toString());
        }
    }

    public int getPort() {
        return port;
    }

    @Override
    public String getType() {
        return "TCP NMEA Server";
    }

    @Override
    public String getDescription() {
        synchronized (clients) {
            return "Port " + getPort() + " - number of clients " + clients.size();
        }
    }

    @Override
    protected boolean onActivate() {
        createServerSocket();
        startServer();
        return true;
    }

    private boolean isSelectorOpen() {
        return selector.isOpen();
    }

    private void startServer() {
        Thread t = new Thread(() -> {
            if (selector!=null && serverSocket!=null) {
                while (isSelectorOpen()) {
                    try {
                        selector.select();
                        for (SelectionKey ky: selector.selectedKeys()) { handleSelectionKey(ky); }
                        selector.selectedKeys().clear();
                    } catch (IOException e) {
                        getLog().error(LogStringBuilder.start(TCP_SERVER_CATEGORY).wO("start").toString(), e);
                    }
                }
            }
        }, "Socket server selector [" + getName() + "]");
        t.setDaemon(true);
        t.start();
    }

    private void handleSelectionKey(SelectionKey ky) {
        if (ky.isValid()) {
            if (ky.isAcceptable()) {
                handleConnection();
            } else if (ky.isReadable()) {
                handleRead(ky);
            }
        }
    }

    private void handleRead(SelectionKey ky) {
        SocketChannel client = (SocketChannel) ky.channel();
        readBuffer.clear();
        try {
            int readBytes = client.read(readBuffer);
            if (readBytes>2) {
                String sSentence = new String(readBuffer.array(), 0, readBytes).trim();
                Sentence sentence = SentenceFactory.getInstance().createParser(sSentence);
                NMEASocketServer.this.postMessage(new NMEA0183Message(sentence));
            } else if (readBytes==0) {
                handleDisconnection(client);
            }
        } catch (IOException e) {
            handleDisconnection(client);
        }
    }

    private void handleDisconnection(SocketChannel client) {
        try {
            getLog().info(LogStringBuilder.start(TCP_SERVER_CATEGORY).wO("disconnect").wV(CLIENT_KEY_NAME, clients.getOrDefault(client, null)).toString());
            synchronized (clients) {
                clients.remove(client);
            }
            client.close();
        } catch (Exception e) {
            getLog().error(LogStringBuilder.start(TCP_SERVER_CATEGORY).wO("disconnect").toString(), e);
        }
    }

    private void handleConnection() {
        try {
            SocketChannel client = serverSocket.accept();

            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            ClientDescriptor desc = new ClientDescriptor(client.getRemoteAddress().toString());
            synchronized (clients) {
                clients.put(client, desc);
            }
            getLog().info(LogStringBuilder.start(TCP_SERVER_CATEGORY).wO("connect").wV(CLIENT_KEY_NAME, desc).toString());
        } catch (Exception ee) {
            getLog().error(LogStringBuilder.start(TCP_SERVER_CATEGORY).wO("connect").toString(), ee);
        }
    }

    @Override
    protected void onDeactivate() {
        synchronized (clients) {
            for (SocketChannel c: clients.keySet()) {
                try {
                    c.close();
                } catch (Exception e) {
                    getLog().error(LogStringBuilder.start(TCP_SERVER_CATEGORY).wO("stop").wV(CLIENT_KEY_NAME, c).toString(), e);
                }
            }
            clients.clear();
            try {
                serverSocket.close();
            } catch (Exception e) {
                getLog().error(LogStringBuilder.start(TCP_SERVER_CATEGORY).wO("stop").toString(), e);
            }
            try {
                selector.close();
            } catch (Exception e) {
                getLog().error(LogStringBuilder.start(TCP_SERVER_CATEGORY).wO("stop").toString(), e);
            }
        }
    }

    @OnRouterMessage
    public void onMessage(RouterMessage rm) {
        synchronized (clients) {
            if (serializer != null && isTarget() && !clients.isEmpty()) {
                for (Sentence s: getSentenceToSend(rm)) {
                    String output = serializer.getOutSentence(s);
                    writeBuffer.clear();
                    writeBuffer.put(output.getBytes());
                    writeBuffer.put("\r\n".getBytes());
                    int p = writeBuffer.position();
                    Iterator<Entry<SocketChannel, ClientDescriptor>> iterator = clients.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Entry<SocketChannel, ClientDescriptor> itm = iterator.next();
                        SocketChannel sc = itm.getKey();
                        ClientDescriptor cd = itm.getValue();
                        if (!sendMessageToClient(output, p, sc, cd)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    private Sentence[] getSentenceToSend(RouterMessage rm) {
        return converter.convert(rm.getPayload());
    }

    private boolean sendMessageToClient(String output, int p, SocketChannel sc, ClientDescriptor cd) {
        writeBuffer.position(0);
        writeBuffer.limit(p);
        try {
            int written = sc.write(writeBuffer);
            if (written==0) {
                cd.errors++;
                getLog().warning(LogStringBuilder.start(TCP_SERVER_CATEGORY).wO("send failure").wV("message", output)
                        .wV(CLIENT_KEY_NAME, sc.getRemoteAddress()).toString());
            } else {
                cd.errors = 0;
            }
            return cd.errors < 10 /* allow a max of 10 failure, then close the channel */;
        } catch (IOException e) {
            try {
                getLog().error(LogStringBuilder.start(TCP_SERVER_CATEGORY).wO("close connection after send failure").toString(), e);
                sc.close();
            } catch (IOException e1) {
                getLog().error(LogStringBuilder.start(TCP_SERVER_CATEGORY).wO("close connection after send failure").toString(), e);
            }
        } catch (Exception e) {
            getLog().error(LogStringBuilder.start(TCP_SERVER_CATEGORY).wO("send failure").wV("message", output).toString(), e);
        }
        return false;
    }

    private void createServerSocket() {
        try {
            selector = Selector.open();
            getLog().info(LogStringBuilder.start(TCP_SERVER_CATEGORY).wO("create socket").toString());
            serverSocket = ServerSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress(getPort());
            serverSocket.bind(hostAddress);
            serverSocket.configureBlocking(false);
            int ops = serverSocket.validOps();
            serverSocket.register(selector, ops, null);
        } catch (Exception e) {
            getLog().error(LogStringBuilder.start(TCP_SERVER_CATEGORY).wO("create socket").toString(), e);
        }
    }

    @Override
    public String toString() {
        return "TCP " + port + " " + (isSource() ? "R" : "")
                + (isTarget() ? "X" : "");
    }
}
