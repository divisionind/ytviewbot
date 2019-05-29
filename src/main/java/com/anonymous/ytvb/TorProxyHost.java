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

import java.io.*;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class TorProxyHost extends ProxyHost {

    private static final long STARTED_POLLING_RATE = 250L;
    private static final String DEFAULT_HOST = "127.0.0.1";

    // TODO add tor Process info here for starting/stopping
    protected final AtomicInteger viewsGenerated;
    protected final AtomicInteger refreshProxyAt;

    private Process process;
    private boolean starting;
    private File config;

    public TorProxyHost(int port, File config, int refreshPoint) {
        super(DEFAULT_HOST, port, true);
        this.config = config;
        this.viewsGenerated = new AtomicInteger(0);
        this.refreshProxyAt = new AtomicInteger(refreshPoint);
    }

    public void reset(int refreshPoint) throws InterruptedException, IOException {
        this.viewsGenerated.set(0);
        this.refreshProxyAt.set(refreshPoint);

        // restart tor process, gets a new public ip address
        process.destroyForcibly();
        process.waitFor();
        start();
    }

    public void waitIfResetting() throws InterruptedException {
        while (starting) Thread.sleep(STARTED_POLLING_RATE);
    }

    public void start() throws IOException {
        starting = true;
        try {
            // create config derived from given with modified port value

            File torrcDerived = new File(YTViewBot.TEMP_FOLDER, "torrc-" + getPort());

            // check if file exists first (tells us whether or not this is a reload)
            if (!torrcDerived.exists()) {
                InputStream cfin;
                if (config != null && config.exists()) {
                    cfin = new FileInputStream(config);
                } else cfin = getClass().getResourceAsStream("assets/torrc");

                // write derived file from source with mods
                try (FileWriter fw = new FileWriter(torrcDerived)) {
                    try (Scanner s = new Scanner(new InputStreamReader(cfin))) {
                        while (s.hasNext()) fw.write(s.nextLine().replaceAll("%PORT%", Integer.toString(getPort())));
                    }
                    fw.flush();
                }

                // delete file on exit
                torrcDerived.deleteOnExit();
            }

            // create process
            process = new ProcessBuilder("tor", "-f", torrcDerived.getAbsolutePath()).start();

            boolean success = false;
            try (Scanner s = new Scanner(new InputStreamReader(process.getInputStream()))) {
                while (s.hasNext()) {
                    String line = s.nextLine().toLowerCase();
                    if (line.contains("done")) {
                        // tor process has been started successfully
                        success = true;
                        break;
                    }
                }
            }

            // failed to start tor process
            if (!success) throw new IOException("Tor process failed to start. Is something already running in the port range?");

            starting = false;
        } catch (Exception e) {
            starting = false;
            throw e;
        }
    }

    public static int calculateRefreshPoint(Random randy, int proxyRefreshInterval, int proxyRefreshIntervalVariation) {
        return (proxyRefreshInterval + proxyRefreshIntervalVariation) - randy.nextInt(proxyRefreshIntervalVariation * 2);
    }
}
