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
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class ViewBot implements Runnable {

    private static ViewBotFactory viewBotFactory = new ViewBotFactory();

    private static Thread createThread(ViewBot bot) {
        return viewBotFactory.newThread(bot);
    }

    private URLQueuer urlQueuer;
    private ProxyHostQueuer proxyQueuer;
    private long watchTime;
    private long watchTimeVariation;
    private WebClient browser;
    private Random randy;
    private boolean running;
    private Thread thread;
    private AtomicLong viewsGenerated;

    public ViewBot(Random randy, URLQueuer urlQueuer, ProxyHostQueuer proxyQueuer, long watchTime, long watchTimeVariation, BrowserVersion browserVersion, AtomicLong viewsGenerated) {
        this.urlQueuer = urlQueuer;
        this.proxyQueuer = proxyQueuer;
        this.watchTime = watchTime;
        this.watchTimeVariation = watchTimeVariation;
        this.randy = randy;
        this.running = false;
        this.viewsGenerated = viewsGenerated;
        browser = new WebClient(browserVersion);
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
                viewUrl();
            } catch (IOException | InterruptedException e) {
                YTViewBot.log.severe(String.format("An error occurred in %s", thread.getName()));
                e.printStackTrace();
            }
        }
    }

    private void viewUrl() throws IOException, InterruptedException {
        WebRequest webRequest = new WebRequest(urlQueuer.getObject());
        ProxyHost proxy = proxyQueuer.getObject();
        webRequest.setProxyHost(proxy.getHost());
        webRequest.setProxyPort(proxy.getPort());
        webRequest.setSocksProxy(proxy.isSocks());
        HtmlPage page = browser.getPage(webRequest);
        Thread.sleep((watchTime + watchTimeVariation) - (long)randy.nextInt((int)(watchTimeVariation * 2L) + 1));
        page.cleanUp();
        viewsGenerated.incrementAndGet();
    }

    public void shutdown() {
        running = false;
        browser.close();
    }
}
