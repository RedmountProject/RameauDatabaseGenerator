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
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author nouri
 */
public class XmlNode {

    private String Url;
    private String externalId;
    private String inScheme;
    private String prefLabel;
    private String altLabel;
    RelationsList relations = new RelationsList();
    
    private List<String> deweys;

    public RelationsList getRelations() {
        return relations;
    }

    public void setRelations(RelationsList relations) {
        this.relations = relations;
    }

    public XmlNode() {
        deweys = new ArrayList<>();
    }

    public List<String> getDeweys() {
        return deweys;
    }

    public void setDeweys(List<String> Deweys) {
        this.deweys = Deweys;
    }
    
   public void addDeweys(String dewey){
        this.deweys.add(dewey);
    } 
    public String getUrl() {
        return Url;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setUrl(String Url) {
        this.Url = Url;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getInScheme() {
        return inScheme;
    }

    public void setInScheme(String inScheme) {
        this.inScheme = inScheme;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public String getAltLabel() {
        return altLabel;
    }

    public void setPrefLabel(String prefLabel) {
        this.prefLabel = prefLabel;
    }

    public void setAltLabel(String altLabel) {
        this.altLabel = altLabel;
    }

    
    
}
