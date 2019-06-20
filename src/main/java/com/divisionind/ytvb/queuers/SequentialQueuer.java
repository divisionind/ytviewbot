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

package com.divisionind.ytvb.queuers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Used for multi-thread-safe retrieving of strings from a pool
 */
public abstract class SequentialQueuer<T> extends ElementParser<T> implements Queuer<T> {

    private AtomicInteger ai = new AtomicInteger(0);

    public SequentialQueuer(List<T> objects) {
        super(objects);
    }

    public SequentialQueuer(File file) throws IOException {
        super(file);
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
