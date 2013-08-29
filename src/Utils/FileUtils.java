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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

/**
 *
 * @author nouri
 */
public class FileUtils {

    public static Logger LOG = Logger.getLogger(FileUtils.class.getName());

    public ArrayList<String> readFile(File file) {

        ArrayList<String> fileList = new ArrayList<>();
        try {
            LineIterator reader = org.apache.commons.io.FileUtils.lineIterator(file, "UTF-8");
            LOG.info(file.getName());
            String line;

            while (reader.hasNext()) {

                line = reader.next();
                if (!line.equals("")) {
                    fileList.add(line);
                }
            }

        } catch (IOException ex) {
            LOG.error("IOException", ex);
        }
        return fileList;
    }

    public static ArrayList<String> listFilesInFolder(final String pFile) {

        File folder = new File(pFile);

        return list(folder);
    }

    private static ArrayList<String> list(final File pFolder) {
        ArrayList<String> filenames = new ArrayList<>();
        for (final File fileEntry : pFolder.listFiles()) {
            if (fileEntry.isDirectory()) {
                list(fileEntry);
            } else {
                //System.out.println(fileEntry.getName());
                filenames.add(fileEntry.getName());
            }
        }
        return filenames;
    }

    public static void writeFileNamesInAText(String filename) {

        LOG.info("File Names are writting to a file...");
        try {
            try (BufferedWriter out = new BufferedWriter(new FileWriter("file/output.txt", true))) {
                out.write(filename);
                out.newLine();
            }
        } catch (IOException e) {
        }
        LOG.info("Writting is done...");

    }
}
