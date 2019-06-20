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

public class URLQueuer extends SequentialQueuer<String> {

    public URLQueuer(File stringFile) throws IOException {
        super(stringFile);
    }

    @Override
    public String processElement(String element) {
        return element;
    }

    @Override
    public String parseErrorMessage() {
        return "Error parsing url at line %s";
    }
}
