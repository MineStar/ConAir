/*
 * Copyright (C) 2013 MineStar.de 
 * 
 * This file is part of ConAir.
 * 
 * ConAir is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * ConAir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ConAir.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.minestar.conair.network.server.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.minestar.conair.network.server.api.exceptions.PluginDescriptionInvalidException;

public class PluginDescription {
    private String name = null, main = null, version = null;

    public PluginDescription(InputStream inputStream) throws PluginDescriptionInvalidException {
        this.loadFile(new BufferedReader(new InputStreamReader(inputStream)));
    }

    private void loadFile(BufferedReader bufferedReader) throws PluginDescriptionInvalidException {
        try {
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                // trim lines and replace spaces
                line = line.trim().replaceAll("\t", "").replaceAll(" ", "");

                // ignore empty lines and comments
                if (line.startsWith("#") || line.startsWith("-") || line.isEmpty() || !line.contains(":")) {
                    continue;
                }

                // split
                String split[] = line.split(":");
                if (split.length != 2) {
                    continue;
                }

                // save the var
                if (split[0].toLowerCase().startsWith("name")) {
                    this.name = split[1];
                } else if (split[0].toLowerCase().startsWith("main")) {
                    this.main = split[1];
                } else if (split[0].toLowerCase().startsWith("version")) {
                    this.version = split[1];
                }
            }
            // close and validate
            bufferedReader.close();

            if (this.version == null) {
                this.version = "0.0.0";
            }
            if (this.name == null || this.main == null) {
                throw new PluginDescriptionInvalidException("PluginDescription is incomplete!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public String getMain() {
        return main;
    }

    public String getVersion() {
        return version;
    }
}
