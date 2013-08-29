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
import Object.Dewey;
import Parsing.DeweyParser;
import java.io.File;
import java.util.ArrayList;
import Utils.StringOperations;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.ReadableIndex;

/**
 *
 * @author nouri
 */
public class CreateDeweys  {

    private static Logger LOG = Logger.getLogger(CreateRameauAndDeweyRelations.class);
    
    private static Configuration conf;
    public static GraphDatabaseService graphDb;
    public CreateDeweys(Configuration pConf, GraphDatabaseService gB) {
        graphDb = gB;
        conf = pConf;
        
    }

    public void creation() {
        if (conf.getBoolean("create.dewey")) {
            LOG.info("Dewey Creation");
            DeweyParser deweyParser = new DeweyParser(new File(conf.getString("deweyNeo")));
            ArrayList<Dewey> deweys = deweyParser.parseDewey();

            ArrayList<Dewey> firstleveldeweys = getFirstLevelDeweys(deweys);
            ArrayList<Dewey> secondleveldeweys = getSecondLevelDeweys(deweys);
            ArrayList<Dewey> thirdleveldeweys = getThirdLevelDeweys(deweys);

            Transaction tx = graphDb.beginTx();
            try {
                Node root = createRoot();
                ArrayList<Node> firstLevel = createFirstLevel(root, firstleveldeweys);
                ArrayList<Node> secondLevel = createSecondLevel(secondleveldeweys);
                ArrayList<Node> thirdLevel = createThirdLevel(thirdleveldeweys);
                tx.success();
            } finally {
                tx.finish();
            }
                        
            LOG.info("Dewey Creation is finished");
        }else
        {
            LOG.info("Creation Dewey is: "+conf.getBoolean("create.dewey"));
        }
        
    }

    

    private static Node createRoot() {
        Node rootNode = graphDb.createNode();
        int deweyExternalId = -1;
        String prefLabel = "Dewey-Root";

        rootNode.setProperty("nodeType", "dewey");
        rootNode.setProperty("externalID", deweyExternalId);
        rootNode.setProperty("prefLabel", prefLabel);
        LOG.info("Root Node is created.  ID: " + rootNode.getId());
        LOG.info("");
        return rootNode;
    }

    private static Node getNodeUsingDeweyExternalId(String deweyExternalId) {

        ReadableIndex<Node> autoNodeIndex = graphDb.index().getNodeAutoIndexer().getAutoIndex();


        if (autoNodeIndex.get("externalID", deweyExternalId).getSingle() != null) {
            return autoNodeIndex.get("externalID", deweyExternalId).getSingle();
        } else {
            return null;
        }
    }

    private ArrayList<Node> createFirstLevel(Node root, ArrayList<Dewey> firstlevelnodes) {
        ArrayList<Node> firstlevel = new ArrayList<>();
        for (Dewey dewey : firstlevelnodes) {
            Node firstlevelnode = graphDb.createNode();
            firstlevelnode.setProperty("nodeType", "dewey");
            firstlevelnode.setProperty("externalID", dewey.getDeweyExternalId());
            String pref= StringOperations.stripAccents(dewey.getCatalogName());
            firstlevelnode.setProperty("prefLabel", pref);
            firstlevelnode.setProperty("prefLabelOriginal", dewey.getCatalogName());
            root.createRelationshipTo(firstlevelnode, GraphDataBase.RelationTypes.NARROWER);
            firstlevelnode.createRelationshipTo(root, GraphDataBase.RelationTypes.BROADER);
            firstlevel.add(firstlevelnode);
        }
        return firstlevel;
    }

    private ArrayList<Node> createSecondLevel(ArrayList<Dewey> secondleveldewels) {

        ArrayList<Node> secondLevels = new ArrayList<>();

        for (Dewey secondleveldewel : secondleveldewels) {
            Node secondlevel = graphDb.createNode();
            secondlevel.setProperty("nodeType", "dewey");
            secondlevel.setProperty("externalID", secondleveldewel.getDeweyExternalId());
            String pref= StringOperations.stripAccents(secondleveldewel.getCatalogName());
            secondlevel.setProperty("prefLabel", pref);
            secondlevel.setProperty("prefLabelOriginal", secondleveldewel.getCatalogName());
            String parentId = secondleveldewel.getDeweyExternalId();
            parentId = parentId.substring(0, 1);
            
            Node parentNode = getNodeUsingDeweyExternalId(parentId);
            parentNode.createRelationshipTo(secondlevel, GraphDataBase.RelationTypes.NARROWER);
            secondlevel.createRelationshipTo(parentNode, GraphDataBase.RelationTypes.BROADER);
            secondLevels.add(secondlevel);
        }


        return secondLevels;
    }

    private ArrayList<Node> createThirdLevel(ArrayList<Dewey> thirdleveldewels) {

        ArrayList<Node> thirdlevels = new ArrayList<>();

        for (Dewey thirdleveldewel : thirdleveldewels) {
            Node thirdlevel = graphDb.createNode();
            thirdlevel.setProperty("nodeType", "dewey");
            thirdlevel.setProperty("externalID", thirdleveldewel.getDeweyExternalId());
            String pref= StringOperations.stripAccents(thirdleveldewel.getCatalogName());
            thirdlevel.setProperty("prefLabel", pref);
            thirdlevel.setProperty("prefLabelOriginal", thirdleveldewel.getCatalogName());
            String parentId = thirdleveldewel.getDeweyExternalId();
            parentId = parentId.substring(0, 2);
            
            Node parentNode = getNodeUsingDeweyExternalId(parentId);            
            parentNode.createRelationshipTo(thirdlevel, GraphDataBase.RelationTypes.NARROWER);
            thirdlevel.createRelationshipTo(parentNode, GraphDataBase.RelationTypes.BROADER);
            thirdlevels.add(thirdlevel);
        }

        return thirdlevels;
    }

    private ArrayList<Dewey> getFirstLevelDeweys(ArrayList<Dewey> deweys) {
        ArrayList<Dewey> firstlevelnodes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            firstlevelnodes.add(deweys.get(i));            
        }
        return firstlevelnodes;

    }

    private ArrayList<Dewey> getSecondLevelDeweys(ArrayList<Dewey> deweys) {
        ArrayList<Dewey> secondlevelnodes = new ArrayList<>();
        for (int i = 10; i < 110; i++) {
            secondlevelnodes.add(deweys.get(i));            
        }
        return secondlevelnodes;

    }

    private ArrayList<Dewey> getThirdLevelDeweys(ArrayList<Dewey> deweys) {

        ArrayList<Dewey> thirdlevelnodes = new ArrayList<>();
        for (int i = 110; i < deweys.size(); i++) {
            thirdlevelnodes.add(deweys.get(i));            
        }        
        return thirdlevelnodes;
    }
    
    
}
