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
package Utils;

import Object.HashSetList;
import Object.OnixGenre;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author nouri
 */
public class StringOperations {

    private StringOperations() {
    }
    private static Logger LOG = Logger.getLogger(StringOperations.class.getName());
    private static final String[] InputReplace = {"é", "è", "ê", "ë", "û", "ù", "ü", "ï", "î", "à", "â", "ö", "ô", "ç"};
    private static final String[] OutputReplace = {"e", "e", "e", "e", "u", "u", "u", "i", "i", "a", "a", "o", "o", "c"};

    public static String stripAccents(String s) {
        s = org.apache.commons.lang.StringUtils.replaceEachRepeatedly(s.toLowerCase(), InputReplace, OutputReplace);
        s = StringEscapeUtils.escapeSql(s);
        s = Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD);
        //s = s.replaceAll("''", "'");
        //LOG.info("after stripAccents: " + s);
        return s;
    }

    public static String stripAccentsWithoutUnnecessaryCharacters(String s) {
        s = org.apache.commons.lang.StringUtils.replaceEachRepeatedly(s.toLowerCase(), InputReplace, OutputReplace);
        s = StringEscapeUtils.escapeSql(s);
        s = Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD);
        s = s.replaceAll("''", "'");
        s = s.replaceAll("\"", "");
        s = s.replaceAll("\\]", "");
        s = s.replaceAll("\\[", "");

        //LOG.debug("after stripAccents: " + s);
        return s;
    }

    public static boolean charactersExist(String line) {
        boolean flag = false;

        if (line.contains(",")) {
            flag = true;
        }
        if (line.contains("(")) {
            flag = true;
        }
        if (line.contains("/")) {
            flag = true;
        }

        return flag;
    }

    public static String[] stringParserDelimIsPipe(String genre) {

        String genreLine = genre.trim();
        String delims = "[|]";
        String[] linearray = genreLine.split(delims);

        return linearray;



    }

    public static ArrayList<OnixGenre> handleOnixGenres(ArrayList<OnixGenre> genres) {


        for (OnixGenre onixGenre : genres) {

            String name = onixGenre.getName();
            HashSetList<String> altnames = onixGenre.getAltnames();

            if (!charactersExist(name)) {
                for (int i = altnames.size() - 1; i >= 0; i -= 1) {
                    String value = altnames.get(i);
                    //LOG.debug("value : " + value);
                   // LOG.debug("Name : " + name + " , value : " + value);
                    if (!name.equals(value)) {
                        onixGenre.addAltname(name);
                        //LOG.debug("\tName added : " + name);
                    }

                }
            }
        }

        return genres;
    }

    public static boolean hashSetListChecker(HashSetList<String> list, String line) {
        boolean flag = false;

        for (Iterator<String> it = list.iterator(); it.hasNext();) {
            String value = it.next();
            LOG.debug("Line : " + line + " , value : " + value);
            if (line.equals(value)) {
                flag = true;
                break;
            }

        }
        LOG.debug("flag : " + flag);
        return flag;

    }
}
