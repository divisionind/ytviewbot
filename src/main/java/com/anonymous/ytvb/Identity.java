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

import java.awt.*;

public class Identity {

    private String userAgent;
    private Dimension screenSize;

    public Identity(String userAgent, int width, int height) {
        this(userAgent, new Dimension(width, height));
    }

    public Identity(String userAgent, Dimension screenSize) {
        this.userAgent = userAgent;
        this.screenSize = screenSize;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Dimension getScreenSize() {
        return screenSize;
    }
}
