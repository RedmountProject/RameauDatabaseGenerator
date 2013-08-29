/*  Copyright (C) 2013 TUNCER Nurettin

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package Parsing;

import Object.Dewey;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

/**
 *
 * @author nouri
 */
public class DeweyParser {
    public static File file;
    private static final Logger LOG = Logger.getLogger(DeweyParser.class.getName());

    public DeweyParser(File path) {
        file = path;
    }
    
    public ArrayList<Dewey>  parseDewey()  {
        ArrayList<Dewey> deweys = new ArrayList<>();
        try {
            LineIterator reader = FileUtils.lineIterator(file, "UTF-8");
            LOG.debug(file.getName());            
            String line;
            
            while (reader.hasNext()) {

                line = reader.next();
                if (!line.equals("")) {
                    deweys.add(getDewey(line));
                }
            }
            
        } catch (IOException ex) {
            LOG.error("IOException", ex);
        }
        return deweys;
    }

    public static Dewey getDewey(String line){
        Dewey dewey = new Dewey();
        String deweyLine = line;
        String delims = "[;]";
        String[] deweyLinearray = deweyLine.split(delims);
        dewey.setDeweyExternalId(deweyLinearray[0]);        
        dewey.setCatalogName(deweyLinearray[1]);
        return dewey;
    }
}