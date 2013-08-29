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
import java.util.List;

/**
 *
 * @author nouri
 */
public class Book {
    private int book_id;
    private String title;
    private List<Keyword> keywords;
    private ArrayList<OnixGenre> onixgenres;
    public HashSetList<MatchNode> matched_nodes;

    public Book() {
        this.matched_nodes = new HashSetList<>();
        keywords = new ArrayList<>();
        this.onixgenres = new ArrayList<>();
    }

    public ArrayList<OnixGenre> getOnixgenres() {
        return onixgenres;
    }

    public void setOnixgenres(ArrayList<OnixGenre> onixgenres) {
        this.onixgenres = onixgenres;
    }
    
    public int getBook_id() {
        return book_id;
    }

    public void setBook_id(int book_id) {
        this.book_id = book_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }   

    public List<Keyword> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<Keyword> keywords) {
        this.keywords = keywords;
    }
    
    public void addKeyword(Keyword keyword){
        this.keywords.add(keyword);
    }
    
     public void addOnixGenre(OnixGenre pOnixGenre){
        this.onixgenres.add(pOnixGenre);
    }
    
     public int getSizeOfKeywords (){
        return keywords.size();
    }
    
}
