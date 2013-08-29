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
/**
 *
 * @author nouri
 */
package Main;

import DataBase.DatabaseManager;
import Neo4jOperations.*;
import Object.XmlNode;
import Parsing.XMLParser;
import Utils.FileUtils;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.neo4j.graphdb.GraphDatabaseService;

public class Main {

    
    private static Configuration conf;
    public static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger("Main");

    public static void main(String[] args) {
        BasicConfigurator.configure();
        LOG.setLevel(Level.INFO);
        conf = new Configuration();

        printOptions(conf);



        GraphDatabaseService myGraph = GraphDataBase.newInstance(conf.getString("DB_PATH"));
        insertDewey(myGraph);
        insertRameau(myGraph);       
        insertBooksAndKeywords(myGraph);
        booksWithGenres(myGraph);
        
        myGraph.shutdown();

    }

    private static void Search(String pSearchString) {
        BasicConfigurator.configure();
        SearchWithCypher s = new SearchWithCypher(conf);
        s.findExactPreflabelsInDeweys(pSearchString);
        s.findPreflabelsInWords(pSearchString);
        s.dbShutdown();
    }

    private static void insertDeweyinDB() {
        try {
            //add deweys in sql
            DatabaseManager db = new DatabaseManager(conf);
            db.addDeweysToSql();
        } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException | IOException ex) {
            LOG.error("SQLException "+ex);
        }
    }

    private static void insertDewey(GraphDatabaseService myGraph) {
        CreateDeweys createDeweys = new CreateDeweys(conf, myGraph);
        createDeweys.creation();
    }

    private static void insertRameau(GraphDatabaseService myGraph) {
        if (conf.getBoolean("create.rameau")) {
            LOG.info("Beginning Rameau Insertion please wait ...");
            ArrayList<XmlNode> xmlnodes;
            ArrayList<String> filenames;
            
            String basePath = conf.getString("foldername");
            filenames = FileUtils.listFilesInFolder(basePath);
            
            CreateRameauAndDeweyRelations createRD = new CreateRameauAndDeweyRelations(myGraph, conf);
            
            int count = 1;
            String currentFile;
            int max_file_count = conf.getInt("insert.files.count");
            
            
            for (int i = 0; i < filenames.size() && i < max_file_count; i++) {
                currentFile = filenames.get(i);
                LOG.debug("************************************************");
                LOG.debug("Start File name: " + currentFile);
                LOG.debug(count + "/" + filenames.size() + " Files left ");
                
                xmlnodes = XMLParser.start(basePath+"/"+currentFile);   
                createRD.start(xmlnodes);
                
                if (LOG.isDebugEnabled()) {
                    sleep((long) 0.50);
                    FileUtils.writeFileNamesInAText(currentFile);                    
                }
                 count++;
                LOG.debug("Finished File name: " + currentFile);
                LOG.debug("************************************************");
            }
            LOG.info("End Rameau Insertion");
        }
    }
    
    
    
    public static void insertBooksAndKeywords(GraphDatabaseService myGraph){
        AddBooksAndKeywords booksAndKeywords = new AddBooksAndKeywords(conf, myGraph);
        booksAndKeywords.createBooksKeywordsRelations();
    }
    
    public static void booksWithGenres(GraphDatabaseService myGraph){
        BooksWithGenres booksWithGenres = new BooksWithGenres(conf, myGraph);
        booksWithGenres.createBooksWithGenres();
    }

    public static void printOptions(Configuration pConf) {
        LOG.info("Creation Deweys: " + pConf.getBoolean("create.dewey"));
        LOG.info("Creation Rameau: " + pConf.getBoolean("create.rameau"));
        LOG.info("Creation Books and keywords: " + pConf.getBoolean("create.addBooksAndKeywords"));
        LOG.info("Creation Books with Genres: " + pConf.getBoolean("create.booksWithGenres"));
    }
    
    
    
      public static void sleep(long i) {
        try {
            Thread.sleep(i * 1000);
        } catch (InterruptedException ex) {
            LOG.error("InterruptedException " + ex.getMessage());
        }
    }
      
      
}
