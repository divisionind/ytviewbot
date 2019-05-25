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

import com.gargoylesoftware.htmlunit.BrowserVersion;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.Display;
import org.jline.utils.NonBlockingReader;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

// see https://www.linuxuprising.com/2018/10/how-to-install-and-use-tor-as-proxy-in.html for tor proxy
// or https://github.com/dgoulet/torsocks
// https://stackoverflow.com/questions/14321214/how-to-run-multiple-tor-processes-at-once-with-different-exit-ips
// systemctl reload tor <- gives new ip address
@CommandLine.Command(name = "ytviewbot", version = "2019.0.1", description = "A bot to view youtube videos (or any site).", mixinStandardHelpOptions = true)
public class YTViewBot implements Callable<Void> {

    //@CommandLine.Parameters(index = "0", description = "list of URL(s) to view")
    private File urls;

    //@CommandLine.Parameters(index = "1", description = "list of proxies to use")
    private File proxies;

    @CommandLine.Option(names = {"-w", "--watch"}, description = "how long to spend viewing a given URL in seconds")
    private int watchTime = 10;

    @CommandLine.Option(names = {"-v", "--watch-variation"}, description = "how much to vary watch time by + or - in seconds")
    private int watchTimeVariation = 2;

    @CommandLine.Option(names = {"-p", "--processes"}, description = "number of process to view at the same time")
    private int processes = 4;

    //@CommandLine.Option(names = {"-s", "--view-switch"}, description = "number of times to view the url before switching proxies")
    //private int viewSwitch = 4;

    @CommandLine.Option(names = {"-u", "--user-agent"}, description = "user agent to use for web calls")
    private String userAgent;

    public static StaticLine log;

    public static final String LOG_SPACER = "-----------------------------------------------------------";

    private static String msg = "no key yet pressed";

    public static void main(String[] args) {

        try {
            Terminal terminal = TerminalBuilder.builder()
                    .name("YTViewBot")
                    .jna(true)
                    .system(true)
                    .build();
            terminal.enterRawMode();

            Display display = new Display(terminal, true);
            display.resize(terminal.getSize().getRows(), terminal.getSize().getColumns());
            List<AttributedString> test = new ArrayList<>();

            new Thread(() -> {
                NonBlockingReader reader = terminal.reader();
                while (true) {
                    int i;
                    try {
                        i = reader.read();
                    } catch (IOException e) {
                        i = -1;
                    }
                    msg = "Key pressed: " + i; // it works!
                    System.out.println("HAHAHAHAHHAHAA");
                }
            }).start();

            while (true) {
                display.clear();
                display.reset();
                for (int i = 0;i<4;i++) {
                    test.add(new AttributedString("Current time: " + System.currentTimeMillis()));
                }
                test.add(new AttributedString(msg));
                display.update(test, terminal.getSize().cursorPos(0, 0));
                test.clear();
                Thread.sleep(500L);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        if (true) return;
        try {
            CommandLine.call(new YTViewBot(), args);
        } catch (Exception e) {
            log.severe("An error occurred running the program.");
            e.printStackTrace();
        }
    }

    private URLQueuer urlQueuer;
    private ProxyHostQueuer proxyQueuer;
    private ViewBot[] viewBots;
    private BrowserVersion browserVersion;
    private Random randy;
    private AtomicLong viewsGenerated;

    @Override
    public Void call() throws Exception {
        log = new StaticLine(System.out, null, false);
        log.info("Initializing...");

        urlQueuer = new URLQueuer(urls);
        proxyQueuer = new ProxyHostQueuer(proxies);
        viewBots = new ViewBot[processes];
        randy = new Random();
        viewsGenerated = new AtomicLong(0);

        // create browser version info
        if (userAgent == null) {
            browserVersion = BrowserVersion.CHROME;
        } else browserVersion = new BrowserVersion.BrowserVersionBuilder(BrowserVersion.FIREFOX_60).setUserAgent(userAgent).build();

        log.info("Spawning processes...");

        // spawn processes
        for (int i = 0;i<processes;i++) {
            viewBots[i] = new ViewBot(randy, urlQueuer, proxyQueuer, TimeUnit.SECONDS.toMillis(watchTime), TimeUnit.SECONDS.toMillis(watchTimeVariation), browserVersion, viewsGenerated).start();
        }
        log.info("Running.");
        log.info(LOG_SPACER);

        Terminal terminal = TerminalBuilder.builder()
                .name("YTViewBot")
                .jna(true)
                .system(true)
                .build();
        terminal.enterRawMode();
        NonBlockingReader reader = terminal.reader();
        log.setCarry(true, "[s]tatus [d]onate [c]lose");

        while (true) {
            //System.out.println("[s]tatus [d]onate [c]lose");
            switch (reader.read()) {
                case 's':
                    for (ViewBot bot : viewBots) {
                        log.info(bot.getThread().getName() + " " + (bot.isRunning() ? "RUNNING" : "STOPPED"));
                    }
                    log.info("Total views generated: " + NumberFormat.getNumberInstance().format(viewsGenerated.get()));
                    log.info(LOG_SPACER);
                    break;
                case 'd':
                    log.info("BTC: 1FpywKn3H2CrGUR1tziq5wjhwLeXHSet9C");
                    log.info("BTH: bitcoincash:qz32f4h83dn9fpju594eafm4hytr528l4c4utgyw66");
                    log.info(LOG_SPACER);
                    break;
                case 'c':
                    log.info("Exiting...");
                    System.exit(0);
                    break;
                default:
                    log.warning("Invalid option key.");
                    log.info(LOG_SPACER);
            }
        }
    }
}
