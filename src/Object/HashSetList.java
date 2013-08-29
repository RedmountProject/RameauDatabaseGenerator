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
import java.util.HashSet;

/**
 *
 * @author nouri
 */
public class HashSetList<T extends Object>
        extends ArrayList<T> {

    private HashSet<Integer> _this = new HashSet<>();
    private final double REDUCE_FACTOR = 55;

    @Override
    public boolean add(T obj) {
        if (_this.add(obj.hashCode())) {
            super.add(obj);
            return true;
        }
        return false;
    }

    public int add(T obj, String pKw) {
        if (_this.add(obj.hashCode())) {
            super.add(obj);
            return 1;
        } else {
            int index = indexOf(obj);
            MatchNode node = (MatchNode) get(index);
            return node.inc(pKw);
        }
    }

    @Override
    public String toString() {
        String ret = "";
        for (T node : this) {
            ret += "\t" + ((MatchNode) node).toString() + "\n";
        }
        return ret;
    }

    public void Reduce() {
        for (int i = this.size() - 1; i >= 0; i -= 1) {
            MatchNode node = (MatchNode) get(i);
            if(node.factor < REDUCE_FACTOR){
                this.remove((T)node);
            }
        }

    }
}
