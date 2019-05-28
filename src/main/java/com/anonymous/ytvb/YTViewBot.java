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

import com.anonymous.ytvb.queuers.ProxyHostQueuer;
import com.anonymous.ytvb.queuers.Queuer;
import com.anonymous.ytvb.queuers.RandomQueuer;
import com.anonymous.ytvb.queuers.URLQueuer;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.NonBlockingReader;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/*
        TODO
        Vary randomly each video request:
            - local ip address (maybe just disable javascript/WebRTC)
            - user agent
            - browser type
            - screen resolution

        Vary randomly every few requests:
            - public ip address (tor/proxy)

        NOTE: YOU CAN SOLVE ALL OF THESE PROBLEMS USING THE TOR BROWSER WITH SELENIUM!!!!
        MAYBE START WITH JUST THIS?

        Tor Browser w/ Selenium:
            - https://medium.com/@manivannan_data/selenium-with-tor-browser-using-python-7b3606b8c55c
            - https://kushaldas.in/posts/tor-browser-and-selenium.html
     */

    /*
        How to use extensions: https://sites.google.com/a/chromium.org/chromedriver/extensions
        Change some settings dynamically: https://www.automation99.com/2017/06/dynamically-changing-proxy-in-browsers.html
        Info on chrome driver (including installation): https://github.com/SeleniumHQ/selenium/wiki/ChromeDriver
        More on modifying capability settings: https://github.com/machinepublishers/jbrowserdriver
     */

// see https://www.linuxuprising.com/2018/10/how-to-install-and-use-tor-as-proxy-in.html for tor proxy
// or https://github.com/dgoulet/torsocks
// https://stackoverflow.com/questions/14321214/how-to-run-multiple-tor-processes-at-once-with-different-exit-ips
// systemctl reload tor <- gives new ip address
@CommandLine.Command(name = "ytviewbot", version = "2019.0.1", description = "A bot to view youtube videos (or any site).", mixinStandardHelpOptions = true)
public class YTViewBot implements Callable<Void> {

    @CommandLine.Parameters(index = "0", description = "list of URL(s) to view")
    private File urls;

    @CommandLine.Option(names = {"-i", "--identities"}, description = "list user agents to screen resolutions to use (simulate different devices)")
    private File identities;

    @CommandLine.Option(names = {"-P", "--proxies"}, description = "list of proxies to use (if specified, tor is not required)")
    private File proxies;

    @CommandLine.Option(names = {"-w", "--watch"}, description = "how long to spend viewing a given URL in seconds (after load time)")
    private int watchTime = 6;

    @CommandLine.Option(names = {"-v", "--watch-variation"}, description = "how much to vary watch time by + or - in seconds")
    private int watchTimeVariation = 2;

    @CommandLine.Option(names = {"-p", "--processes"}, description = "number of process to view at the same time")
    private int processes = 4;

    @CommandLine.Option(names = {"-t", "--tor-proxies"}, description = "number of tor proxies to spawn, warning: increasing this will decrease anonymity") // Increasing this will decrease anonymity while allow you to raise the refresh interval.
    private int torProxies = 1;

    @CommandLine.Option(names = {"-r", "--refresh-int"}, description = "number of views to generate every tor refresh")
    private int torRefreshInterval = 50;

    @CommandLine.Option(names = {"-R", "--refresh-variation"}, description = "how much to vary the refresh interval by + or - in number of views")
    private int torRefreshIntervalVariation = 10;

    //@CommandLine.Option(names = {"-s", "--view-switch"}, description = "number of times to view the url before switching proxies")
    //private int viewSwitch = 4;

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

    private Queuer<URL> urlQueuer;
    private Queuer<ProxyHost> proxyQueuer;
    private ViewBot[] viewBots;
    private Random randy;
    private AtomicLong viewsGenerated;
    private boolean usingTor;

    @Override
    public Void call() throws Exception {
        Terminal terminal = TerminalBuilder.builder()
                .name("YTViewBot")
                .jna(true)
                .system(true)
                .build();
        terminal.enterRawMode();

        StaticLine staticLine = new StaticLine(systemOut, "[s]tatus [d]onate [c]lose");
        out.setStaticLine(staticLine);
        err.setStaticLine(staticLine);

        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tT %3$s/%4$s] %5$s %n");
        log.info("Initializing...");

        if (proxies == null) {
            usingTor = true;

            log.info(String.format("Proxy list not specified. Generating and using %s tor proxies...", torProxies));

            List<ProxyHost> proxyHosts = new ArrayList<>();
            // TODO spawn tor processes, port in proxy configs

            proxyQueuer = new RandomQueuer<>(proxyHosts, randy);
        } else {
            usingTor = false;
            proxyQueuer = new ProxyHostQueuer(proxies);
        }

        if (identities == null) {
            // TODO use internal identities list, e.g. InputStream
        } else {
            log.info("Identities list specified. Using that instead of the internal one...");
        }

        urlQueuer = new URLQueuer(urls);
        viewBots = new ViewBot[processes];
        randy = new Random();
        viewsGenerated = new AtomicLong(0);

        // TODO making config where users can specify browser link types, spawn one of these browsers for each process, (include custom one where tor proxy leads to driver of choice?), see if can set proxy while running chrome driver

        log.info("Spawning processes...");

        // spawn processes
        for (int i = 0;i<processes;i++) {
            viewBots[i] = new ViewBot(randy, urlQueuer, proxyQueuer, TimeUnit.SECONDS.toMillis(watchTime), TimeUnit.SECONDS.toMillis(watchTimeVariation), viewsGenerated).start();
        }
        log.info("Running.");

        NonBlockingReader reader = terminal.reader();

        while (true) {
            switch (reader.read()) {
                case 's':
                    log.info(new AttributedStringBuilder()
                            .style(AttributedStyle.INVERSE)
                            .append("Status").toAnsi());
                    for (ViewBot bot : viewBots) {
                        log.info(String.format("%s - %s", bot.getThread().getName(), bot.isRunning() ? "RUNNING" : "STOPPED"));
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
                    out.staticLine.setCarry(false, "Exiting... Have a nice day!");
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
