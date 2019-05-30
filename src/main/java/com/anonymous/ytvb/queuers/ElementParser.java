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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A useful class for parsing single line file elements of any format into a list.
 */
public abstract class ElementParser<T> {

    public abstract T processElement(String element) throws Exception;

    public abstract String parseErrorMessage();

    protected List<T> objects;

    public ElementParser(List<T> objects) {
        this.objects = objects;
    }

    public ElementParser(File file) throws IOException {
        this(new FileReader(file));
    }

    public ElementParser(Reader fr) throws IOException {
        objects = new ArrayList<>();

        try (Scanner s = new Scanner(fr)) {
            int i = 0;
            NumberFormat numberFormatter = NumberFormat.getNumberInstance();
            while (s.hasNext()) {
                i++;
                try {
                    String line = s.nextLine();
                    if (line.equals("") || line.startsWith("#")) continue; // skip blank lines and comments

                    T element = processElement(line);
                    if (element != null) objects.add(element);
                } catch (Exception e) {
                    YTViewBot.log.severe(String.format(parseErrorMessage(), numberFormatter.format(i)));
                }
            }
        }
        fr.close();
    }

    public List<T> getObjects() {
        return objects;
    }
}
