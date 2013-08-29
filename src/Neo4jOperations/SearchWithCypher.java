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
package Neo4jOperations;

import Main.Configuration;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.impl.util.StringLogger;

/**
 *
 * @author nouri
 */
public class SearchWithCypher {

    public static Logger LOG = Logger.getLogger(SearchWithCypher.class.getName());
    public static ExecutionEngine engine;
    private static Configuration conf;
    public static GraphDatabaseService db;

    public SearchWithCypher(Configuration pConf) {
        conf = pConf;
        db = new GraphDatabaseFactory().newEmbeddedDatabase(conf.getProperty("DB_PATH"));
        engine = new ExecutionEngine(db, StringLogger.SYSTEM);
    }
    
    public void findPreflabelsInWords(String word) {
        ExecutionResult result = engine.execute("START n=node(*)\n"
                + "where n.nodeType = 'word' and n.prefLabel=~ '.*" + word + ".*'  \n"
                + "return n\n"
                + "limit 100");

        String nodeResult;
        Iterator<Node> n_column = result.columnAs("n");
        int count = 0;

        for (Node node : IteratorUtil.asIterable(n_column)) {
            // note: we're grabbing the name property from the node,
            // not from the n.name in this case.
            nodeResult = node.getId() + ": " + node.getProperty("prefLabel");
            LOG.info("findPreflabelInWords");
            LOG.info(nodeResult);

            count++;
        }

        if (count == 0) {
            LOG.error("Word couldn't find ! ! ");
        }

    }

    public boolean findExactPreflabelsInWords(String word) {
        //LOG.info("Log avant replaceAll: "+word);
        String cleanWord;        
        cleanWord = word.replaceAll("\\'", "\\\\'");        
        //LOG.info("Log apres replaceAll: "+cleanWord);
        
        ExecutionResult result = engine.execute("START n=node(*)\n"
                + "where n.nodeType = 'word' and n.prefLabel= '" + cleanWord + "'  \n"
                + "return n, n.prefLabel, n.prefLabelOriginal, n.altLabel, n.nodeType\n"
                + "limit 100");
        
        String nodeResult;


        Iterator<Node> n_column = result.columnAs("n");
        int count = 0;

        for (Node node : IteratorUtil.asIterable(n_column)) {
            // note: we're grabbing the name property from the node,
            // not from the n.name in this case.
            nodeResult = node.getId() + ": " + node.getProperty("prefLabel");
            LOG.info("findExactPreflabelInWords " + word);
            LOG.info(nodeResult);
            LOG.info("");
            count++;
        }

        if (count == 0) {
            LOG.error("Word couldn't find ! ! word: "+word);
            return false;
        }else
            return true;
        
    }
    
    public void findExactPreflabelsInDeweys(String word) {
        
        String cleanWord;        
        cleanWord = word.replaceAll("\\'", "\\\\'"); 
        
        ExecutionResult result = engine.execute("START n=node(*)\n"
                + "where n.nodeType = 'dewey' and n.prefLabel= '" + cleanWord + "'  \n"
                + "return n, n.prefLabel, n.prefLabelOriginal, n.altLabel, n.nodeType\n"
                + "limit 100");
        String nodeResult;


        Iterator<Node> n_column = result.columnAs("n");
        int count = 0;

        for (Node node : IteratorUtil.asIterable(n_column)) {
            // note: we're grabbing the name property from the node,
            // not from the n.name in this case.
            nodeResult = node.getId() + ": " + node.getProperty("prefLabel");
            LOG.info("findExactPreflabelInWords word: "+word);
            LOG.info(nodeResult);
            LOG.info("");
            count++;
        }

        if (count == 0) {
            LOG.error("Word couldn't find ! ! word: "+word);
        }
    }
    

    public void dbShutdown() {
        db.shutdown();
    }
}
