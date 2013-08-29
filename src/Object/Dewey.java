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

/**
 *
 * @author nouri
 */
public class Dewey {
    private String deweyExternalId;
    private String catalogName;

    public Dewey() {
    }

    public Dewey(String externalId, String catalogName) {
        this.deweyExternalId = externalId;
        this.catalogName = catalogName;
    }

    public String getDeweyExternalId() {
        return deweyExternalId;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setDeweyExternalId(String externalId) {
        this.deweyExternalId = externalId;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }
    
    
}
