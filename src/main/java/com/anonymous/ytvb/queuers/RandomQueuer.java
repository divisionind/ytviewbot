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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomQueuer<T> implements Queuer<T> {

    private List<T> objects;
    private Random randy;

    public RandomQueuer(ElementParser<T> parser, Random randy) {
        this(parser.objects, randy);
    }

    public RandomQueuer(List<T> objects, Random randy) {
        this.objects = objects;
        this.randy = randy;
    }

    public RandomQueuer(Random randy) {
        this(new ArrayList<>(), randy);
    }

    public List<T> getObjects() {
        return objects;
    }

    @Override
    public T getObject() {
        return objects.get(randy.nextInt(objects.size()));
    }
}
