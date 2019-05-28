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

package com.anonymous.ytvb.queuers;

import com.anonymous.ytvb.YTViewBot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Used for multi-thread-safe retrieving of strings from a pool
 */
public abstract class SequentialFileQueuer<T> implements Queuer<T> {

    public abstract T processElement(String element) throws Exception;

    public abstract String parseErrorMessage();

    private List<T> objects;
    private AtomicInteger ai;

    public SequentialFileQueuer(File objectFile) throws FileNotFoundException {
        ai = new AtomicInteger(0);
        objects = new ArrayList<>();

        try (Scanner s = new Scanner(new FileReader(objectFile))) {
            int i = 0;
            NumberFormat numberFormatter = NumberFormat.getNumberInstance();
            while (s.hasNext()) {
                i++;
                try {
                    objects.add(processElement(s.nextLine()));
                } catch (Exception e) {
                    YTViewBot.log.severe(String.format(parseErrorMessage(), numberFormatter.format(i)));
                }
            }
        }
    }

    @Override
    public T getObject() {
        int on = ai.getAndIncrement();
        if (on >= objects.size()) {
            ai.set(0);
            on = 0;
        }
        return objects.get(on);
    }
}
