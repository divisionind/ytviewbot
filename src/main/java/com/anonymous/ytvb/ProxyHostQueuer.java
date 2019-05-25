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

import java.io.File;
import java.io.FileNotFoundException;

public class ProxyHostQueuer extends ObjectQueuer<ProxyHost> {

    private static final String SEPARATOR = " ";

    public ProxyHostQueuer(File objectFile) throws FileNotFoundException {
        super(objectFile);
    }

    @Override
    public ProxyHost processElement(String element) throws Exception {
        if (!element.contains(SEPARATOR)) throw new Exception();
        String[] parts = element.split(SEPARATOR);
        if (parts.length != 3) throw new Exception();

        return new ProxyHost(parts[0], Integer.parseInt(parts[1]), parts[2].equalsIgnoreCase("y"));
    }

    @Override
    public String parseErrorMessage() {
        return "Error parsing proxy host at line %s";
    }
}
