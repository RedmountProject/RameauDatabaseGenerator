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
public class RelationsList {
    private List<String> broaders; 
    private List<String> relateds ;
    private List<String> narrowers; 

    public RelationsList() {
        relateds = new ArrayList<>();
        broaders = new ArrayList<>();
        narrowers = new ArrayList<>();
    }

    public List<String> getBroaders() {
        return broaders;
    }

    public List<String> getRelateds() {
        return relateds;
    }

    public List<String> getNarrowers() {
        return narrowers;
    }

    public void setBroaders(List<String> broaders) {
        this.broaders = broaders;
    }

    public void setRelateds(List<String> relateds) {
        this.relateds = relateds;
    }

    public void setNarrowers(List<String> narrowers) {
        this.narrowers = narrowers;
    }

    
    public void addRelated(String related){
        this.relateds.add(related);
    }
    public void addBroader(String broader){
        this.broaders.add(broader);
    }
    public void addNarrower(String narrower){
        this.narrowers.add(narrower);
    }
    public int getSizeOfRelations (){
        return relateds.size()+broaders.size()+narrowers.size();
    }
    public void printRelated(){
        System.out.println("**************** Relateds ****************");
        for(String temp : relateds)
        {
            System.out.println("Related Url is: "+temp);
        }
     
    }
    public void printBroaders(){
        System.out.println("**************** Broaders ****************");
        for(String temp : broaders)
        {
            System.out.println("Broader Url is: "+temp);
        }
    
    }
    
     public void printNarrowers(){
         System.out.println("**************** Narrowers ****************");
        for(String temp : narrowers)
        {
            System.out.println("Narrower Url is: "+temp);
        }
    
    }
    
    
}
