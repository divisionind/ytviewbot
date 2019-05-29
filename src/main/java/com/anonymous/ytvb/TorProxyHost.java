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

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class TorProxyHost extends ProxyHost {

    // TODO add tor Process info here for starting/stopping
    protected final AtomicInteger viewsGenerated;
    protected final AtomicInteger refreshProxyAt;

    // make constructor a config info
    public TorProxyHost(String host, int port, boolean socks) {
        super(host, port, socks);
        this.viewsGenerated = new AtomicInteger();
        this.refreshProxyAt = new AtomicInteger();
    }

    public void reset(int refreshPoint) {
        this.viewsGenerated.set(0);
        this.refreshProxyAt.set(refreshPoint);

        // TODO restart tor process
    }

    public void waitIfResetting() {

    }

    public static int calculateRefreshPoint(Random randy, int proxyRefreshInterval, int proxyRefreshIntervalVariation) {
        return (proxyRefreshInterval + proxyRefreshIntervalVariation) - randy.nextInt(proxyRefreshIntervalVariation * 2);
    }
}
