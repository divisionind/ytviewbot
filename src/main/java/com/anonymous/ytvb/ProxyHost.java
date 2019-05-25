package com.anonymous.ytvb;

public class ProxyHost {

    private String host;
    private int port;
    private boolean socks;

    public ProxyHost(String host, int port, boolean socks) {
        this.host = host;
        this.port = port;
        this.socks = socks;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isSocks() {
        return socks;
    }
}
