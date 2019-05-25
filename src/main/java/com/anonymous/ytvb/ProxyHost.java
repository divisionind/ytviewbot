/*
 * ytviewbot - just a YouTube view bot
 * Copyright (C) 2019 Anonymous
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
