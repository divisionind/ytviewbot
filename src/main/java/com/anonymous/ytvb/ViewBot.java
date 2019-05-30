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

import com.anonymous.ytvb.queuers.Queuer;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class ViewBot implements Runnable {

    private static final String BASE_CONFIG_UPDATE_STRING = "Components.classes[\"@mozilla.org/preferences-service;1\"].getService(Components.interfaces.nsIPrefBranch).set%sPref(\"%s\", \"%s\");";

    private static ViewBotFactory viewBotFactory = new ViewBotFactory();

    private static Thread createThread(ViewBot bot) {
        return viewBotFactory.newThread(bot);
    }

    private Queuer<String> urlQueuer;        // every time
    private Queuer<Identity> identityQueuer; // every time
    private Queuer<ProxyHost> proxyQueuer;   // every torRefreshInterval +- torRefreshIntervalVariation
    private FirefoxDriver driver;
    private ProxyHost currentProxy;          // maybe simplify this proxy switching system later
    private long watchTime;
    private long watchTimeVariation;
    private Random randy;
    private boolean running;
    private Thread thread;
    private AtomicLong viewsGenerated;
    private int proxyRefreshInterval;
    private int proxyRefreshIntervalVariation;
    private int refreshNormalProxyAt;
    private int currentViewsGenerated;

    public ViewBot(Random randy, Queuer<String> urlQueuer, Queuer<Identity> identityQueuer, Queuer<ProxyHost> proxyQueuer, long watchTime, long watchTimeVariation, AtomicLong viewsGenerated, int proxyRefreshInterval, int proxyRefreshIntervalVariation, File extNoScript) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, InterruptedException, IOException {
        this.urlQueuer = urlQueuer;
        this.identityQueuer = identityQueuer;
        this.proxyQueuer = proxyQueuer;
        this.watchTime = watchTime;
        this.watchTimeVariation = watchTimeVariation;
        this.randy = randy;
        this.viewsGenerated = viewsGenerated;
        this.proxyRefreshInterval = proxyRefreshInterval;
        this.proxyRefreshIntervalVariation = proxyRefreshIntervalVariation;

        currentProxy = proxyQueuer.getObject();

        if (!(currentProxy instanceof TorProxyHost)) {
            refreshNormalProxyAt = TorProxyHost.calculateRefreshPoint(randy, proxyRefreshInterval, proxyRefreshIntervalVariation);
            currentViewsGenerated = 0;
        }

        FirefoxProfile profile = new FirefoxProfile();

        // disables the "frozen" options so we can change anything we want
        Field f = profile.getClass().getDeclaredField("additionalPrefs");
        f.setAccessible(true);
        Object perfs = f.get(profile);
        Field f2 = Class.forName("org.openqa.selenium.firefox.Preferences").getDeclaredField("immutablePrefs");
        f2.setAccessible(true);
        Map<String, Object> immutablePrefs = (Map<String, Object>) f2.get(perfs);
        immutablePrefs.clear();

        // NoScript plugin to prevent WebRTC local ip collection (and other JS tracking)
        profile.addExtension(extNoScript);

        // prevents site tracking (prevents yt from seeing the same browser changing ips and viewing video over-and-over again)
        profile.setPreference("browser.cache.disk.enable", false);
        profile.setPreference("network.http.use-cache", false);
        profile.setPreference("browser.cache.offline.enable", false);
        profile.setPreference("browser.cache.memory.enable", false);
        profile.setPreference("network.cookie.cookieBehavior", 2);

        // so we dont have to push the button when changing values on the fly
        profile.setPreference("general.warnOnAboutConfig", false);

        //profile.setPreference("media.peerconnection.enabled", false); // how you disable WebRTC without extensions
        //profile.setPreference("permissions.default.image", 2);        // stops the page from loading images (may decrease load time if not required)

        // enable proxy
        profile.setPreference("network.proxy.socks", currentProxy.getHost());
        profile.setPreference("network.proxy.socks_port", currentProxy.getPort());
        profile.setPreference("network.proxy.socks_version", currentProxy.getVersion());
        profile.setPreference("network.proxy.type", 1);

        // start firefox headless
        FirefoxOptions options = new FirefoxOptions();
        options.setProfile(profile);
        options.setHeadless(true);
        driver = new FirefoxDriver(options);
    }

    public Thread getThread() {
        return thread;
    }

    public boolean isRunning() {
        return running;
    }

    public ViewBot start() {
        if (running) throw new IllegalStateException("Error ViewBot already running.");
        thread = createThread(this);
        thread.start();
        return this;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                viewUrl(); // TODO make this class BaseBot and make a ViewBot subclass - use this to make other bots to i.e. make accounts, view other website types, etc.
            } catch (Exception e) {
                YTViewBot.log.severe(String.format("An error occurred in %s", thread.getName()));
                e.printStackTrace();
            }
        }
    }

    private void viewUrl() throws InterruptedException, IOException {
        // enter config mode
        driver.get("about:config");

        // essentially change what device yt thinks we are using
        Identity identity = identityQueuer.getObject();
        setPerf(PerfType.String, "general.useragent.override", identity.getUserAgent());
        driver.manage().window().setSize(identity.getScreenSize());

        // loading page!
        driver.get(urlQueuer.getObject());

        // yt doesnt autoplay videos to some visitors, if the video isn't playing, we need to find the play button and press it
        // NOTE: I could just press k here, but I have to find whether or not the video is playing first anyway so this just seems easier to me.
        List<WebElement> elements = driver.findElements(By.cssSelector("button, input[title='Play (k)']"));
        for (WebElement ele : elements) {
            // checks if has play button (meaning video is paused) and clicks it
            if (ele.getAttribute("title").toLowerCase().contains("play")) {
                ele.click();
                break;
            }
        }

        Thread.sleep((watchTime + watchTimeVariation) - (long)randy.nextInt((int)(watchTimeVariation * 2L)));
        viewsGenerated.incrementAndGet();

        // check and refresh proxy if time
        if (currentProxy instanceof TorProxyHost) {
            TorProxyHost torProxyHost = (TorProxyHost)currentProxy;

            // holds this process if tor is resetting to get new ip
            torProxyHost.waitIfResetting();

            if (torProxyHost.viewsGenerated.incrementAndGet() >= torProxyHost.refreshProxyAt.get()) {
                refreshProxy(torProxyHost);
            }
        } else {
            currentViewsGenerated++;
            if (currentViewsGenerated >= refreshNormalProxyAt) { // I would like to do this inline but it would differ in function from the above statement so thats why I didnt
                refreshProxy(null);
            }
        }
    }

    private void refreshProxy(TorProxyHost torProxyHost) throws InterruptedException, IOException {
        if (torProxyHost == null) {
            currentProxy = proxyQueuer.getObject();
            refreshNormalProxyAt = TorProxyHost.calculateRefreshPoint(randy, proxyRefreshInterval, proxyRefreshIntervalVariation);
            currentViewsGenerated = 0;

            // enter config mode
            driver.get("about:config");

            // change proxy
            setPerf(PerfType.String, "network.proxy.socks", currentProxy.getHost());
            setPerf(PerfType.Int, "network.proxy.socks_port", currentProxy.getPort());
            setPerf(PerfType.Int, "network.proxy.socks_version", currentProxy.getVersion());
        } else {
            // dont have to change proxy address in browser here because that remains the same, tor is simply restarted to yield a new ip
            torProxyHost.reset(TorProxyHost.calculateRefreshPoint(randy, proxyRefreshInterval, proxyRefreshIntervalVariation));
        }
    }

    private void setPerf(PerfType type, String perf, Object value) {
        driver.executeScript(String.format(BASE_CONFIG_UPDATE_STRING, type.name(), perf, value));
    }

    public void shutdown() {
        running = false;
        thread.interrupt();
        driver.quit();
    }

    public enum PerfType {
        String, Bool, Int
    }
}
