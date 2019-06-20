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

import com.divisionind.ytvb.Identity;
import org.openqa.selenium.Dimension;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Random;

public class IdentityQueuer extends RandomQueuer<Identity> {

    private static final String SEPARATOR1 = "=";
    private static final String SEPARATOR2 = "x";

    public IdentityQueuer(List<Identity> objects, Random randy) {
        super(objects, randy);
    }

    public IdentityQueuer(Reader fr, Random randy) throws IOException {
        super(fr, randy);
    }

    @Override
    public Identity processElement(String element) throws Exception {
        if (!element.contains(SEPARATOR1)) throw new Exception();
        String[] parts = element.split(SEPARATOR1);

        if (parts.length != 2) throw new Exception();
        if (!parts[1].contains(SEPARATOR2)) throw new Exception();
        String[] dimensions = parts[1].split(SEPARATOR2);
        // TODO return null here and manually add identity elements so that you can specify multiple resolutions per the same user agent (e.g. User-Agent|1280x720|1920x1080|2560x1440

        return new Identity(parts[0], new Dimension(Integer.parseInt(dimensions[0]), Integer.parseInt(dimensions[1])));
    }

    @Override
    public String parseErrorMessage() {
        return "Error parsing identity at line %s";
    }
}
