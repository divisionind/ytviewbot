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

import com.divisionind.ytvb.ProxyHost;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ProxyHostQueuer extends SequentialQueuer<ProxyHost> {

    private static final String SEPARATOR = " ";
    private static final String TABLE_CHAR = "\t";

    public ProxyHostQueuer(List<ProxyHost> objects) {
        super(objects);
    }

    public ProxyHostQueuer(File objectFile) throws IOException {
        super(objectFile);
    }

    @Override
    public ProxyHost processElement(String element) throws Exception {
        element = element.trim().replaceAll(" +", " ");
        if (element.contains(TABLE_CHAR)) element = element.replaceAll(TABLE_CHAR, SEPARATOR); // this is present when pasting from excel
        if (!element.contains(SEPARATOR)) throw new Exception();
        String[] parts = element.split(SEPARATOR);
        if (parts.length != 4) throw new Exception();

        return new ProxyHost(parts[0], Integer.parseInt(parts[1]), parts[2].equalsIgnoreCase("y"), Integer.parseInt(parts[3]));
    }

    @Override
    public String parseErrorMessage() {
        return "Error parsing proxy host at line %s";
    }
}
