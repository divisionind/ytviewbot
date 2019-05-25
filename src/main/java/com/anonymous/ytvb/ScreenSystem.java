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

import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.*;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ScreenSystem {

    private static final long UPDATE_RATE = 500L;

    private static boolean running = false;
    private static Terminal terminal;
    private static Display display;

    public static void init(AtomicLong viewGenerated) throws IOException, InterruptedException {
        terminal = TerminalBuilder.builder()
                .name("YTViewBot")
                .jna(true)
                .system(true)
                .build();

        terminal.enterRawMode();
        running = true;

        new Thread(() -> {
            NonBlockingReader reader = terminal.reader();
            while (running) {
                try {
                    int i = reader.read();
                    switch (i) {
                        default:
                            // unknown input
                            break;
                        case 's':
                            // status
                            break;
                        case 'c':
                            // close
                            break;
                    }
                } catch (IOException e) {
                    YTViewBot.handleException(e);
                }
            }
        }).start();

        // run display loop
        List<AttributedString> screen = new ArrayList<>();
        while (true) {
            Size size = terminal.getSize();
            int rows = size.getRows();
            int columns = size.getColumns();

            display.resize(rows, columns);
            display.clear();
            display.reset();

            // draw title
            AttributedStringBuilder title = new AttributedStringBuilder();
            title.style(AttributedStyle.INVERSE);
            String titleString = "YTViewBot - just a YouTube view bot ";
            String titleString2 = "Views Generated: " + NumberFormat.getNumberInstance().format(viewGenerated.get());
            title.append(titleString);
            for (int i = 0;i<(columns - titleString.length() - titleString2.length());i++) title.append(" ");
            title.append(titleString2);
            screen.add(title.toAttributedString());

            // draw processes
            // if > than (rows - 5) processes, displaying "And x more..."

            // draw donate message
            int numberOfElements = rows - screen.size() - 4;
            for (int i = 0;i<numberOfElements;i++) {
                screen.add(new AttributedString("\n"));
            }
            screen.add(new AttributedStringBuilder()
                    .style(AttributedStyle.INVERSE)
                    .append("                                                                ").toAttributedString());
            screen.add(new AttributedString("Please donate using one of the crypto addresses below. Thanks! |"));
            screen.add(new AttributedString(" > BTC: 1FpywKn3H2CrGUR1tziq5wjhwLeXHSet9C                     |"));
            screen.add(new AttributedString(" > BTH: bitcoincash:qz32f4h83dn9fpju594eafm4hytr528l4c4utgyw66 |"));

            display.update(screen, size.cursorPos(rows, columns));
            screen.clear();
            Thread.sleep(UPDATE_RATE);
        }
    }
}
