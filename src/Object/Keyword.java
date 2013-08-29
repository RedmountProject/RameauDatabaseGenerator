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

import java.util.Objects;

/**
 *
 * @author nouri
 */
public class Keyword {

    private String name;
    private int keyword_id;
    private int keyword_weight;
    private int global_weight;
    private int global_count;
    private String neo4j_external_id;

    public Keyword() {
    }

    public Keyword(String parsedKey) {
        name = parsedKey.trim();
    }

    public String getNeo4j_external_id() {
        return neo4j_external_id;
    }

    public void setNeo4j_external_id(String neo4j_external_id) {
        this.neo4j_external_id = neo4j_external_id;
    }

    public int getKeyword_weight() {
        return keyword_weight;
    }

    public void setKeyword_weight(int keyword_weight) {
        this.keyword_weight = keyword_weight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKeyword_id() {
        return keyword_id;
    }

    public void setKeyword_id(int keyword_id) {
        this.keyword_id = keyword_id;
    }

    public int getGlobal_weight() {
        return global_weight;
    }

    public void setGlobal_weight(int global_weight) {
        this.global_weight = global_weight;
    }

    public int getGlobal_count() {
        return global_count;
    }

    public void setGlobal_count(int global_count) {
        this.global_count = global_count;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Keyword other = (Keyword) obj;
        if (hashCode() != other.hashCode()) {
            return false;
        }
        return true;
    }
    
    
    
}
