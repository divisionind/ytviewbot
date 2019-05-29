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

import com.anonymous.ytvb.Identity;

import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import java.util.Random;

public class IdentityQueuer extends RandomQueuer<Identity> {

    private static final String SEPARATOR1 = "|";
    private static final String SEPARATOR2 = "x";

    public IdentityQueuer(Reader reader, Random randy) throws IOException {
        super(new IdentityParser(reader), randy);
    }

    private static class IdentityParser extends ElementParser<Identity> {

        private IdentityParser(Reader fr) throws IOException {
            super(fr);
        }

        @Override
        public Identity processElement(String element) throws Exception {
            if (element.startsWith("#")) return null;
            if (!element.contains(SEPARATOR1)) throw new Exception();
            String[] parts = element.split(SEPARATOR1);

            if (parts.length != 2) throw new Exception();
            if (!parts[1].contains(SEPARATOR2)) throw new Exception();
            String[] dimensions = parts[1].split(SEPARATOR2);

            return new Identity(parts[0], new Dimension(Integer.parseInt(dimensions[0]), Integer.parseInt(dimensions[1])));
        }

        @Override
        public String parseErrorMessage() {
            return "Error parsing identity at line %s";
        }
    }
}
