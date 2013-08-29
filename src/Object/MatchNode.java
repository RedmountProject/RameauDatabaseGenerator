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
import java.util.Objects;
import org.neo4j.graphdb.Node;

/**
 *
 * @author nouri
 */
public class MatchNode implements Comparable<MatchNode> {

    final String NeoKwExtId;
    final String NeoKwName;
    final int NeoKwLen;
    double nb_found;
    double l_found;
    final long NeoKwId;
    double factor;
    private final int NeoKwCount;
    private ArrayList<String> MatchedKeywords;
    
    public MatchNode(Node pNeoKw, String pKw) {
        NeoKwExtId = pNeoKw.getProperty("externalID").toString();
        NeoKwId = pNeoKw.getId();
        NeoKwName = pNeoKw.getProperty("prefLabel").toString().trim();
        MatchedKeywords = new ArrayList<>();
        MatchedKeywords.add(pKw);
        NeoKwLen = NeoKwName.length();
        NeoKwCount = NeoKwName.split(" ").length;
        l_found = pKw.length();
        nb_found = 1;
        compute();

    }

    public long getNeoKwId() {
        return NeoKwId;
    }
    

    public int inc(String pKw) {
        //if (NeoKwName.matches("\\b" + pKw + "\\b")) {
        if (!MatchedKeywords.contains(pKw)) {
            MatchedKeywords.add(pKw);
            l_found += pKw.length();
            nb_found++;
            compute();
            return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        if (hashCode() != o.hashCode()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.NeoKwName);
        return hash;
    }
    private int j = 0;

    @Override
    public String toString() {

        String text = "Keyword : ";
        for (String s : MatchedKeywords) {
            text += s + ", ";
        }
        return text += " matched in NeoKwId : " + NeoKwId
                + " NeoKwExtId : " + NeoKwExtId
                + " prefLabel : "
                + NeoKwName + "  count : " + nb_found
                + " factor : " + String.format("%3.2f", factor);



    }

    @Override
    public int compareTo(MatchNode t) {
        //DESC
        return Double.compare(t.nb_found, nb_found);
    }

    private void compute() {
        factor = l_found / NeoKwLen * 100;
    }
}
