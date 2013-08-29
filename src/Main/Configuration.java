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
package Main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author amaury
 */
public class Configuration extends Properties {

    public Configuration() {
        super();
        FileInputStream in = null;
        try {
            in = new FileInputStream("conf/sanspapier-default.xml");
            load(in);
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String getString(String key) {
        return this.getProperty(key);
    }

    public Integer getInt(String key) {
        int i = Integer.parseInt(this.getProperty(key));
        return (i == 0) ? Integer.MAX_VALUE : i;
    }
    
    public boolean getBoolean(String key){
        return Boolean.parseBoolean(this.getProperty(key));
        
    }
    
//    public T getMethod() {
//        String methodString = this.getProperty("method");
//        int method = Integer.parseInt(methodString);
//        
//        T Object = (T) new Object();
//        
//        switch (method) {
//            case 0:
//                return ;
//                
//        }
//        return null;
//    }
}
