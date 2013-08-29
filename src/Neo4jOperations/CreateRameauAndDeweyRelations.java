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
import Neo4jOperations.GraphDataBase.RelationTypes;
import Object.RelationsList;
import Object.XmlNode;
import Utils.StringOperations;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.ReadableIndex;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;

/**
 *
 * @author nouri
 */
public  class CreateRameauAndDeweyRelations {

    //private  final String DB_PATH = "DB/databaseNeo4j";
    private  final Logger LOG = Logger.getLogger(CreateRameauAndDeweyRelations.class.getName());
    private  GraphDatabaseService graphDb;
    private  Configuration conf;
    private  int matchKeywords = 0;

    public CreateRameauAndDeweyRelations(GraphDatabaseService Gb, Configuration pConf) {
        conf = pConf;
        graphDb = Gb;
    }

    public void start(ArrayList<XmlNode> xmlnodes) {
        //LOG.setLevel(Level.INFO);

        if (conf.getBoolean("create.rameau")) {
            LOG.info("CreateRameauAndDeweyRelations started");



            Transaction tx = graphDb.beginTx();

            try {
                for (XmlNode xmlNode : xmlnodes) {
                    LOG.debug("-------------------Creation starts-----------------------------------");
                    Node mainNode = mainNodeCreation(xmlNode, graphDb);
                    createRelationships(mainNode, xmlNode.getRelations(), graphDb);
                    LOG.debug("-------------------Creation finished-----------------------------------\n");
                }
                tx.success();
            } finally {
                tx.finish();
            }



            LOG.info("CreateRameauAndDeweyRelations finished");
        } else {
            LOG.info("Creation Rameau is: " + conf.getBoolean("create.rameau"));
        }
        //         
//         shutDown(graphDb);

//        Node test;
//        test = findNodeByName("Cheval");
//        
//        LOG.info("id " +test.getId());
//        ArrayList<Node> currentRelations = getNodeRelationsById(test.getId(), RelTypes.NARROWER);
//        getNodeRelationsPrefLabel(currentRelations);
//        currentRelations = getNodeRelationsByIdDirectonIncoming(test.getId(), RelTypes.NARROWER);
//        getNodeRelationsPrefLabel(currentRelations);

//        getBroaderNodeById(test.getId());
//        getNarrowerNodeById(test.getId());
//        getRelatedNodeById(test.getId());
        
    }


    private Node mainNodeCreation(XmlNode xmlNode, GraphDatabaseService graphDb) {
        Node currentNode = null;
        if (isNodeExist(xmlNode.getExternalId())) {
            if (getNodeUsingExternalID(xmlNode.getExternalId()) != null) {
                LOG.debug("Main Node exists. Node ExternalID: " + xmlNode.getExternalId());
                currentNode = getNodeUsingExternalID(xmlNode.getExternalId());
                currentNode = updateMainNode(currentNode, xmlNode);
            }
        } else {
            LOG.debug("Main Node doesn't exist. Node will create. Node ExternalID: " + xmlNode.getExternalId());
            currentNode = createMainNode(xmlNode, graphDb);
        }

        return currentNode;
    }

    private  void compareWords(ArrayList<String> keywords) {
        ArrayList<String> nonmatchedKeywords = new ArrayList<>();
        matchKeywords = 0;
        int count = 0;
        for (String keyword : keywords) {

            if (isNodeExist(keyword)) {
                matchKeywords++;
                LOG.debug("Matched: " + matchKeywords + " / " + keywords.size());
            } else {
                nonmatchedKeywords.add(keyword);
            }
            count++;
            LOG.debug("Count: " + count + " / " + keywords.size());
        }

        LOG.debug("There are " + matchKeywords + " Keywords matched with Rameau and There are " + nonmatchedKeywords.size() + " Keywords didn't Match");
        LOG.debug("List of nonmatched Keywords");
        printArray(nonmatchedKeywords);

    }

    private  void printArray(ArrayList<String> array) {

        for (String element : array) {
            LOG.debug(element);
        }
    }

    private  ArrayList<Node> getNodeRelationsById(long nodeID, RelationshipType type) {
        LOG.debug("Node ID: " + nodeID + " , RelationshipType: " + type);
        Iterable<Relationship> relations = graphDb.getNodeById(nodeID).getRelationships(Direction.OUTGOING, type);
        ArrayList<Node> currentRelations = new ArrayList<>();
        for (Relationship relationship : relations) {
            currentRelations.add(relationship.getEndNode());
        }

        return currentRelations;
    }

    private  ArrayList<Node> getNodeRelationsByIdDirectonIncoming(long nodeID, RelationshipType type) {
        LOG.debug("");
        LOG.debug("Node ID: " + nodeID + " , Incoming RelationshipType: " + type);
        LOG.debug("");
        Iterable<Relationship> relations = graphDb.getNodeById(nodeID).getRelationships(Direction.INCOMING, type);
        ArrayList<Node> currentRelations = new ArrayList<>();
        for (Relationship relationship : relations) {
            currentRelations.add(relationship.getStartNode());
        }

        return currentRelations;
    }

    private  void getNodeRelationsPrefLabel(ArrayList<Node> nodeRelationsList) {
        if (nodeRelationsList.isEmpty()) {
            LOG.debug("The list is empty");
        } else {
            for (Node node : nodeRelationsList) {
                print("Node id: " + node.getId() + " Node prefLabel: " + node.getProperty("prefLabel"));
            }
        }
    }

    private  void getLinkedNodesById(long nodeID, final RelationTypes rel) {
        Iterable<Relationship> relations =
                graphDb.getNodeById(nodeID).getRelationships(Direction.OUTGOING, rel);
        int count = 1;
        for (Relationship relationship : relations) {
            LOG.debug("count " + count);
            LOG.debug(printNodeBroaders(relationship.getEndNode()));
            count++;
        }

    }

    private  Traverser getBroaders(Node broader) {
        TraversalDescription td = Traversal.description().breadthFirst().relationships(GraphDataBase.RelationTypes.BROADER, Direction.OUTGOING).evaluator(Evaluators.excludeStartPosition());
        return td.traverse(broader);
    }

    private  Traverser getNarrowers(Node narrower) {
        TraversalDescription td = Traversal.description().breadthFirst().relationships(GraphDataBase.RelationTypes.NARROWER, Direction.OUTGOING).evaluator(Evaluators.excludeStartPosition());
        return td.traverse(narrower);
    }

    private  Traverser getRelated(Node related) {
        TraversalDescription td = Traversal.description().breadthFirst().relationships(GraphDataBase.RelationTypes.RELATED, Direction.OUTGOING).evaluator(Evaluators.excludeStartPosition());
        return td.traverse(related);
    }

    private  String printNodeBroaders(Node broader) {

        // START SNIPPET: friends-usage
        int numberOfBroaders = 0;
        String output = broader.getProperty("prefLabel") + "'s broaders:\n";
        Traverser broaderTraverser = getBroaders(broader);
        for (Path friendPath : broaderTraverser) {
            output += "At depth " + friendPath.length() + " => "
                    + friendPath.endNode().getProperty("prefLabel") + "\n";
            numberOfBroaders++;
        }
        output += "Number of broaders found: " + numberOfBroaders + "\n";
        // END SNIPPET: friends-usage
        return output;
    }

    private  String printNodeNarrowers(Node narrower) {

        // START SNIPPET: friends-usage
        int numberOfNarrowers = 0;
        String output = narrower.getProperty("prefLabel") + "'s narrowers:\n";
        Traverser broaderTraverser = getNarrowers(narrower);
        for (Path friendPath : broaderTraverser) {
            output += "At depth " + friendPath.length() + " => "
                    + friendPath.endNode().getProperty("prefLabel") + "\n";
            numberOfNarrowers++;
        }
        output += "Number of narrowers found: " + numberOfNarrowers + "\n";
        // END SNIPPET: friends-usage
        return output;
    }

    private  String printNodeRelateds(Node related) {

        // START SNIPPET: friends-usage
        int numberOfRelateds = 0;
        String output = related.getProperty("prefLabel") + "'s relateds:\n";
        Traverser broaderTraverser = getRelated(related);
        for (Path friendPath : broaderTraverser) {
            output += "At depth " + friendPath.length() + " => "
                    + friendPath.endNode().getProperty("prefLabel") + "\n";
            numberOfRelateds++;
        }
        output += "Number of relateds found: " + numberOfRelateds + "\n";
        // END SNIPPET: friends-usage
        return output;
    }

    private  boolean isNodeExistUsingPrefLabel(String prefLabel) {
        if (findNodeByName(prefLabel) == null) {
            return false;
        } else {
            return true;
        }

    }

    private  Node findNodeByName(String prefLabel) {
        ReadableIndex<Node> autoNodeIndex = graphDb.index().getNodeAutoIndexer().getAutoIndex();
        Node result;
        try {
            if (autoNodeIndex.get("prefLabel", prefLabel).getSingle() != null) {

                result = autoNodeIndex.get("prefLabel", prefLabel).getSingle();
                LOG.info("Keyword is: " + prefLabel);
                LOG.info("prefLabel is: " + result.getProperty("prefLabel"));

                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOG.error("Multiple nodes with the label: " + prefLabel);
            matchKeywords++;
//            result.setProperty("prefLabel", prefLabel);
//            LOG.info("prefLabel is: "+result.getProperty("prefLabel"));
            return null;
        }
    }

    // END SNIPPET: createReltype
    private  boolean isNodeExist(String externalID) {
        LOG.debug("externalID " + externalID);
        ReadableIndex<Node> autoNodeIndex = graphDb.index().getNodeAutoIndexer().getAutoIndex();
        if (autoNodeIndex.get("externalID", externalID).getSingle() == null) {
            return false;
        }

        return true;
    }

    private Node getNodeUsingExternalID(String externalID) {
        ReadableIndex<Node> autoNodeIndex = graphDb.index().getNodeAutoIndexer().getAutoIndex();
        if (autoNodeIndex.get("externalID", externalID).getSingle() != null) {
            return autoNodeIndex.get("externalID", externalID).getSingle();
        } else {
            return null;
        }
    }

    private  Node setNodeProperties(Node node, XmlNode xmlNode) {
        String defaultvalue = "default";
        String preflabelWithoutAccents;
        String altlabelWithoutAccents;
        if (xmlNode.getPrefLabel() == null || xmlNode.getPrefLabel().contentEquals("")) {
            node.setProperty("prefLabel", defaultvalue);
        } else {
            preflabelWithoutAccents = StringOperations.stripAccentsWithoutUnnecessaryCharacters(xmlNode.getPrefLabel());
            node.setProperty("prefLabel", preflabelWithoutAccents);
            node.setProperty("prefLabelOriginal", xmlNode.getPrefLabel());
            LOG.debug("prefLabel is updated. prefLabel: " + node.getProperty("prefLabel"));
        }
        if (xmlNode.getAltLabel() == null || xmlNode.getAltLabel().contentEquals("")) {
            node.setProperty("altLabel", defaultvalue);
        } else {
            altlabelWithoutAccents = StringOperations.stripAccentsWithoutUnnecessaryCharacters(xmlNode.getAltLabel());
            node.setProperty("altLabel", altlabelWithoutAccents);
            node.setProperty("altLabelOriginal", xmlNode.getAltLabel());
            LOG.debug("altLabel is updated. altLabel: " + node.getProperty("altLabel"));
        }
        if (!xmlNode.getDeweys().isEmpty()) {
            createDeweyRelation(xmlNode, node);
        }
        return node;

    }

    private  Node updateMainNode(Node node, XmlNode xmlNode) {
        if (node.getProperty("prefLabel").toString().contentEquals("default")
                || node.getProperty("altLabel").toString().contentEquals("default")) {
            LOG.debug("Node will update. Node ExternalID: " + xmlNode.getExternalId());
            node = setNodeProperties(node, xmlNode);
        }
        return node;

    }

    private  Node createMainNode(XmlNode xmlNode, GraphDatabaseService graphDb) {
        String defaultvalue = "default";
        Node node = graphDb.createNode();
        node.setProperty("url", xmlNode.getUrl());
        node.setProperty("externalID", xmlNode.getExternalId());
        node.setProperty("nodeType", "word");

        String preflabelWithoutAccents;
        String altlabelWithoutAccents;

        if (xmlNode.getPrefLabel() == null || xmlNode.getPrefLabel().contentEquals("")) {
            node.setProperty("prefLabel", defaultvalue);
        } else {
            preflabelWithoutAccents = StringOperations.stripAccentsWithoutUnnecessaryCharacters(xmlNode.getPrefLabel());
            node.setProperty("prefLabel", preflabelWithoutAccents);
            node.setProperty("prefLabelOriginal", xmlNode.getPrefLabel());
        }
        if (xmlNode.getAltLabel() == null || xmlNode.getAltLabel().contentEquals("")) {
            node.setProperty("altLabel", defaultvalue);
        } else {
            altlabelWithoutAccents = StringOperations.stripAccentsWithoutUnnecessaryCharacters(xmlNode.getAltLabel());
            node.setProperty("altLabel", altlabelWithoutAccents);
            node.setProperty("altLabelOriginal", xmlNode.getAltLabel());
        }
        if (!xmlNode.getDeweys().isEmpty() && conf.getBoolean("create.dewey")) {
            createDeweyRelation(xmlNode, node);
        }
        LOG.debug("Main Node is created.  Node ExternalID: " + node.getProperty("externalID"));
        LOG.debug("");
        return node;
    }

    private  void createDeweyRelation(XmlNode xmlNode, Node nodeWord) {
        
        List<String> deweyRelations = xmlNode.getDeweys();
        
        for (String deweyRelation : deweyRelations) {
            Node deweyNode = getNodeUsingDeweyExternalId(deweyRelation);
            
            if (deweyNode != null) {
                if (!isRelationshipExist(deweyNode, nodeWord, "Narrower")) {
                    Relationship relationshipDN = deweyNode.createRelationshipTo(nodeWord, GraphDataBase.RelationTypes.NARROWER);
                    relationshipDN.setProperty("RelationType", "Narrower");
                    Relationship relationshipDB = nodeWord.createRelationshipTo(deweyNode, GraphDataBase.RelationTypes.BROADER);
                    relationshipDB.setProperty("RelationType", "Broader");
                    LOG.debug("Dewey Relation: " + " Nodeword ID: " + nodeWord.getId() + " " + nodeWord.getProperty("prefLabel") + " -----> " + " Dewey Id:" + deweyNode.getId() + " " + deweyNode.getProperty("prefLabel"));
                }
            }
        }

    }

    private  Node getNodeUsingDeweyExternalId(String deweyExternalId) {

        ReadableIndex<Node> autoNodeIndex = graphDb.index().getNodeAutoIndexer().getAutoIndex();


        if (autoNodeIndex.get("externalID", deweyExternalId).getSingle() != null) {
            return autoNodeIndex.get("externalID", deweyExternalId).getSingle();
        } else {
            return null;
        }
    }

    private  Node createRelatedNode(String url, String externalID, GraphDatabaseService graphDb) {
        String defaultvalue = "default";
        Node node = graphDb.createNode();

        node.setProperty("url", url);
        node.setProperty("externalID", externalID);
        node.setProperty("prefLabel", defaultvalue);
        node.setProperty("altLabel", defaultvalue);
        node.setProperty("nodeType", "word");



        LOG.debug("Related Node is created.  Node ExternalID: " + node.getProperty("externalID")+"\n");

        return node;
    }

    private  boolean isRelationshipExist(Node firstNode, Node secondNode, String relationtype) {
//        ReadableIndex<Relationship> autoRelationsIndex = graphDb.index().getRelationshipAutoIndexer().getAutoIndex();
//        autoRelationsIndex.get("RelationType", "Related")
        Iterable<Relationship> relations;
        relations = firstNode.getRelationships();
        boolean result = false;
        for (Relationship relation : relations) {
            if (relation.getType().toString().contentEquals(relationtype) && relation.getStartNode().getId() == firstNode.getId() && relation.getEndNode().getId() == secondNode.getId()) {
                result = true;
            }
        }
        return result;
    }

    // int relationtype = 0 for related
    // int relationtype = 1 for broader
    // int relationtype = 2 for narrower   
    private void createRelationships(Node firstNode, RelationsList relationlist, GraphDatabaseService graphDb) {

        List<String> relateds = relationlist.getRelateds();

        for (String relationshipUrl : relateds) {
            Node secondNode;
            if (isNodeExist(getExternalID(relationshipUrl))) {
                secondNode = getNodeUsingExternalID(getExternalID(relationshipUrl));
                LOG.debug("Related Node is already exist. ExternalID: " + secondNode.getProperty("externalID")+"\n");
            } else {
                secondNode = createRelatedNode(relationshipUrl, getExternalID(relationshipUrl), graphDb);
            }

            if (isRelationshipExist(firstNode, secondNode, "RELATED")) {
                LOG.debug("Relation has already existed " + firstNode.getId() + " ----> " + secondNode.getId() + " : " + "RELATED");

            } else {
                createRelationship(firstNode, secondNode, 0);
            }
        }

        List<String> broaders = relationlist.getBroaders();

        for (String relationshipUrl : broaders) {
            Node secondNode;
            if (isNodeExist(getExternalID(relationshipUrl))) {
                secondNode = getNodeUsingExternalID(getExternalID(relationshipUrl));
                LOG.debug("Related Node is already exist. ExternalID: " + secondNode.getProperty("externalID")+"\n");
            } else {
                secondNode = createRelatedNode(relationshipUrl, getExternalID(relationshipUrl), graphDb);
            }

            if (isRelationshipExist(firstNode, secondNode, "BROADER")) {
                LOG.debug("Relation has already existed " + firstNode.getId() + " ----> " + secondNode.getId() + " : " + "BROADER");
            } else {
                createRelationship(firstNode, secondNode, 1);
            }
        }

        List<String> narrowers = relationlist.getNarrowers();

        for (String relationshipUrl : narrowers) {
            Node secondNode;
            if (isNodeExist(getExternalID(relationshipUrl))) {
                secondNode = getNodeUsingExternalID(getExternalID(relationshipUrl));
                LOG.debug("Related Node is already exist. ExternalID: " + secondNode.getProperty("externalID")+"\n");
            } else {
                secondNode = createRelatedNode(relationshipUrl, getExternalID(relationshipUrl), graphDb);
            }

            if (isRelationshipExist(firstNode, secondNode, "NARROWER")) {

                LOG.debug("Relation has already existed " + firstNode.getId() + " ----> " + secondNode.getId() + " : " + "NARROWER");
            } else {
                createRelationship(firstNode, secondNode, 2);
            }
        }
    }

    // int relationtype = 0 for related
    // int relationtype = 1 for broader
    // int relationtype = 2 for narrower
    private void createRelationship(Node firstNode, Node secondNode, int relationtype) {
        // ,RelationshipType rt
        if (relationtype == 0) {
            Relationship relationship = firstNode.createRelationshipTo(secondNode, GraphDataBase.RelationTypes.RELATED);
            relationship.setProperty("RelationType", "Related");

            LOG.debug("Relationship is created. Id: " + firstNode.getId() + " ----> " + secondNode.getId() + " : " + "Related\n");
        }
        if (relationtype == 1) {
            Relationship relationship = firstNode.createRelationshipTo(secondNode, GraphDataBase.RelationTypes.BROADER);
            relationship.setProperty("RelationType", "Broader");
            LOG.debug("Relationship is created. Id: " + firstNode.getId() + " ----> " + secondNode.getId() + " : " + "Broader\n");
        }
        if (relationtype == 2) {
            Relationship relationship = firstNode.createRelationshipTo(secondNode, GraphDataBase.RelationTypes.NARROWER);
            relationship.setProperty("RelationType", "Narrower");
            LOG.debug("Relationship is created. Id: " + firstNode.getId() + " ----> " + secondNode.getId() + " : " + "Narrower\n");
        }

    }

    private  String getExternalID(String nodeValue) {
        String temp[];
        temp = nodeValue.split("/cb");
        temp[1] = temp[1].substring(0, 8);
        String externalId = temp[1];
        return externalId;
    }
    // END SNIPPET: shutdownHook

    private  void print(String print) {
        LOG.debug(print);
    }
//    private  void clearDb() {
//        try {
//            FileUtils.deleteRecursively(new File(DB_PATH));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
