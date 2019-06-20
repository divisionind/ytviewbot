/*
 * ytviewbot - just a YouTube view bot
 * Copyright (C) 2019 Division Industries LLC
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

package com.divisionind.ytvb;

import com.divisionind.ytvb.queuers.IdentityQueuer;
import com.divisionind.ytvb.queuers.ProxyHostQueuer;
import com.divisionind.ytvb.queuers.Queuer;
import com.divisionind.ytvb.queuers.URLQueuer;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.NonBlockingReader;
import org.openqa.selenium.firefox.FirefoxDriver;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

@CommandLine.Command(name = "ytviewbot", version = "@DivisionVersion@ (git: @DivisionGitHash@)", description = "A bot to view youtube videos (or possibly any site).", mixinStandardHelpOptions = true, usageHelpWidth = 100)
public class YTViewBot implements Callable<Void> {

    @CommandLine.Parameters(index = "0", description = "list of URL(s) to view")
    private File urls;

    @CommandLine.Option(names = {"-i", "--identities"}, description = "list user agents to screen resolutions to use (simulate different devices)")
    private File identities;

    @CommandLine.Option(names = {"-P", "--proxies"}, description = "list of proxies to use (if specified, tor is not required)")
    private File proxies;

    @CommandLine.Option(names = {"-c", "--config-tor"}, description = "location of tor config (if not specified, will use internal)")
    private File torrc;

    @CommandLine.Option(names = {"-w", "--watch"}, description = "how long to spend viewing a given URL in seconds (after load time)")
    private int watchTime = 8;

    @CommandLine.Option(names = {"-v", "--watch-variation"}, description = "how much to vary watch time by + or - in seconds")
    private int watchTimeVariation = 2;

    @CommandLine.Option(names = {"-p", "--processes"}, description = "number of process to view at the same time")
    private int processes = 2;

    @CommandLine.Option(names = {"-t", "--tor-proxies"}, description = "number of tor proxies to spawn, warning: increasing this will decrease anonymity") // Increasing this will decrease anonymity while allow you to raise the refresh interval.
    private int torProxies = 1;

    @CommandLine.Option(names = {"-r", "--refresh-int"}, description = "number of views to generate before switching proxies")
    private int proxyRefreshInterval = 20;

    @CommandLine.Option(names = {"-R", "--refresh-variation"}, description = "how much to vary the refresh interval by + or - in number of views")
    private int proxyRefreshIntervalVariation = 5;

    public static final File TEMP_FOLDER = new File("/tmp");
    private static final int TOR_START_PORT = 9051; // maybe make this pull from a list later

    private static PrintStream systemOut;
    private static OutputRedirect out;
    private static OutputRedirect err;

    static {
        systemOut = System.out;
        System.setOut(new PrintStream(out = new OutputRedirect()));
        System.setErr(new PrintStream(err = new OutputRedirect()));
    }

    public static Logger log = Logger.getLogger("YTViewBot");

    public static void main(String[] args) {

        if (!System.getProperty("os.name").toLowerCase().contains("linux")) {
            log.severe("OS not supported! You must be running Linux.");
            return;
        }

        try {
            CommandLine.call(new YTViewBot(), args);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public static void handleException(Exception ex) {
        log.severe("An error occurred running the program.");
        ex.printStackTrace();
    }

    private Queuer<String> urlQueuer;
    private Queuer<ProxyHost> proxyQueuer;
    private Queuer<Identity> identityQueuer;
    private ViewBot[] viewBots;
    private Random randy;
    private AtomicLong viewsGenerated;

    @Override
    public Void call() throws Exception {
        // used to temporarily disable tor
        if (proxies == null) {
            log.severe("Tor proxies are not yet supported. Please specify a list of socks4/5 proxies to use the program.");
            return null;
        }

        Terminal terminal = TerminalBuilder.builder()
                .name("YTViewBot")
                .jna(true)
                .system(true)
                .build();
        terminal.enterRawMode();

        StaticLine staticLine = new StaticLine(systemOut, "Loading...");
        out.setStaticLine(staticLine);
        err.setStaticLine(staticLine);

        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tT %3$s/%4$s] %5$s %n");
        log.info("Initializing...");

        urlQueuer = new URLQueuer(urls);
        viewBots = new ViewBot[processes];
        randy = new Random();
        viewsGenerated = new AtomicLong(0);

        if (proxies == null) {
            // prevents tor proxies from being created and never used
            if (torProxies > processes) {
                log.severe("Can not use more tor proxies than processes!");
                return null;
            }

            log.info(String.format("Proxy list not specified. Generating and using %s tor proxies...", torProxies));

            List<ProxyHost> proxyHosts = new ArrayList<>();

            int port = TOR_START_PORT;
            for (int i = 0;i<torProxies;i++) {
                TorProxyHost proxy = new TorProxyHost(port, torrc, TorProxyHost.calculateRefreshPoint(randy, proxyRefreshInterval, proxyRefreshIntervalVariation));
                try {
                    proxy.start();
                    proxyHosts.add(proxy);
                    log.info(String.format("Tor proxy started on port %s", port));
                } catch (IOException e) {
                    log.severe(String.format("Could not start tor proxy on port %s. Error: %s", port, e.getLocalizedMessage()));
                    e.printStackTrace();
                }
                port++;
            }

            proxyQueuer = new ProxyHostQueuer(proxyHosts);
        } else {
            proxyQueuer = new ProxyHostQueuer(proxies);
        }

        Reader identityReader;
        if (identities == null) {
            identityReader = new InputStreamReader(getClass().getResourceAsStream("/assets/identities.txt"));
        } else {
            log.info("Identities list specified. Using that instead of the internal one...");
            identityReader = new FileReader(identities);
        }
        identityQueuer = new IdentityQueuer(identityReader, randy);

        log.info("Spawning processes...");

        // extract extensions
        File extNoScript = new File(TEMP_FOLDER, "NoScript.xpi");
        Files.copy(getClass().getResourceAsStream("/assets/NoScript.xpi"), extNoScript.toPath(), StandardCopyOption.REPLACE_EXISTING);
        extNoScript.deleteOnExit();

        // stops awful log spam by selenium
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null"); // NUL - Windows | /dev/null - Linux

        // spawn processes
        for (int i = 0;i<processes;i++) {
            String name = String.format("ViewBot-%s", i);
            staticLine.setLine(String.format("Starting %s...", name));
            viewBots[i] = new ViewBot(name, randy, urlQueuer, identityQueuer, proxyQueuer, TimeUnit.SECONDS.toMillis(watchTime), TimeUnit.SECONDS.toMillis(watchTimeVariation), viewsGenerated, proxyRefreshInterval, proxyRefreshIntervalVariation, extNoScript).start();
        }

        // add shutdown hook to stop selenium instances which would persist otherwise
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (ViewBot bot : viewBots) bot.shutdown();

            // ensures tor processes close no matter what
            if (proxyQueuer.getObject() instanceof TorProxyHost) {
                ((ProxyHostQueuer)proxyQueuer).getObjects().forEach(v -> {
                    try {
                        ((TorProxyHost)v).stop();
                    } catch (Exception e) { }
                });
            }
        }));

        log.info("Running.");

        staticLine.setLine("[s]tatus [d]onate [c]lose");

        NonBlockingReader reader = terminal.reader();

        while (true) {
            switch (reader.read()) {
                case 's':
                    log.info(new AttributedStringBuilder()
                            .style(AttributedStyle.INVERSE)
                            .append("Status").toAnsi());
                    for (ViewBot bot : viewBots) {
                        int[] resetPointStats = bot.getProxyResetPointStats();
                        log.info(String.format("%s (%s / %s) - %s", bot.getName(), resetPointStats[0], resetPointStats[1], bot.getCurrentlyViewing() == null ? "loading" : bot.getCurrentlyViewing()));
                    }
                    log.info(String.format("Views Generated: %s", NumberFormat.getNumberInstance().format(viewsGenerated.get())));
                    log.info("-----------------------------------------------------------");
                    break;
                case 'd':
                    log.info(new AttributedStringBuilder()
                            .style(AttributedStyle.INVERSE)
                            .append("Donation Info").toAnsi());
                    log.info("BTC: 1FpywKn3H2CrGUR1tziq5wjhwLeXHSet9C");
                    log.info("BTH: bitcoincash:qz32f4h83dn9fpju594eafm4hytr528l4c4utgyw66");
                    log.info("-----------------------------------------------------------");
                    break;
                case 'c':
                    out.staticLine.setCarry(false, "Exiting (this may take a while)... Have a nice day!");
                    System.exit(0);
                    break;
                default:
                    log.warning("Invalid key command.");
            }
        }
    }

    private static class OutputRedirect extends OutputStream {

        private StaticLine staticLine;
        private StringBuilder sb;

        public OutputRedirect() {
            this.staticLine = null;
            this.sb = new StringBuilder();
        }

        @Override
        public final void write(int i) throws IOException {
            char c = (char) i;
            if(c == '\r' || c == '\n') {
                if(sb.length()>0) {
                    // fixes picocli not printing
                    if (staticLine == null) {
                        systemOut.println(sb.toString());
                    } else staticLine.println(sb.toString());
                    sb = new StringBuilder();
                }
            } else sb.append(c);
        }

        public void setStaticLine(StaticLine staticLine) {
            this.staticLine = staticLine;
        }
    }
}
