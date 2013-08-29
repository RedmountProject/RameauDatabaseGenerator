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
package Object;

import java.util.ArrayList;

/**
 *
 * @author nouri
 */
public class OnixGenre {
    private String name;
    //private List<String> altnames;
    private HashSetList<String> altnames;
    private ArrayList<Long> dewey_nodeIds;

    public OnixGenre() {
        altnames = new HashSetList<>();
        this.dewey_nodeIds = new ArrayList<>();
    }

    public ArrayList<Long> getDewey_nodeIds() {
        return dewey_nodeIds;
    }

    public void setDewey_nodeIds(ArrayList<Long> dewey_nodeIds) {
        this.dewey_nodeIds = dewey_nodeIds;
    }
    
    
    public void addDeweyNodeId(long pNodeId){
        this.dewey_nodeIds.add(pNodeId);
    }
    
    public void addAltname(String pAltname){
        this.altnames.add(pAltname);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashSetList<String> getAltnames() {
        return altnames;
    }

    public void setAltnames(HashSetList<String> altnames) {
        this.altnames = altnames;
    }

    
    
    
    
}
