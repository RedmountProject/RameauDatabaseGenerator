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
package Parsing;

import Object.RelationsList;
import Object.XmlNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.log4j.Logger;

/**
 *
 * @author nouri
 */
public class XMLParser {

   
    private static final Logger LOG = Logger.getLogger(XMLParser.class.getName());

    private XMLParser() {}

    public static ArrayList<XmlNode> start(String pFilePath) {
        ArrayList<XmlNode> xmlnodes;
        Node level1 = getChildNodes(pFilePath);
        ArrayList<Node> secondlevelnodesWithFilter = getSecondLevelWithFilter(level1);
        LOG.debug("size " + secondlevelnodesWithFilter.size());
        xmlnodes = getThirdLevelWithFilter(secondlevelnodesWithFilter);
        xmlnodes = cleanXmlNodeList(xmlnodes);
        LOG.debug("size of xmlnodes: " + xmlnodes.size());
        return xmlnodes;
    }

    private static Node getChildNodes(String pFilePath) {
        Document doc = null;
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = dBuilder.parse(new File(pFilePath));
            doc.normalize();
            LOG.debug("Root element :" + doc.getDocumentElement().getNodeName());

        } catch (ParserConfigurationException ex) {
            LOG.error("ParserConfigurationException", ex);
        } catch (SAXException ex) {
            LOG.error("SAXException", ex);
        } catch (IOException ex) {
            LOG.error("IOException", ex);
        }
        return doc.getChildNodes().item(0);
    }

    private static ArrayList<XmlNode> cleanXmlNodeList(ArrayList<XmlNode> xmlnodes) {
        LOG.debug("Before deleting \"ZERO\" relations:" + xmlnodes.size());
        ArrayList<XmlNode> willdelete = new ArrayList<>();
        for (XmlNode xmlNode : xmlnodes) {
            if (xmlNode.getRelations().getSizeOfRelations() == 0) {
                willdelete.add(xmlNode);
            }
        }
        xmlnodes.removeAll(willdelete);
        LOG.debug("After deleting \"ZERO\" relations:" + xmlnodes.size());

        return xmlnodes;

    }

    private static ArrayList<Node> getSecondLevelWithFilter(Node firstlevelnode) {
        // make sure it's element node.
        ArrayList<Node> secondlevelnodesWithFilter = new ArrayList<>();
        if (firstlevelnode.getNodeType() == Node.ELEMENT_NODE) {

            if (firstlevelnode.hasChildNodes()) {
                // get attributes names and values
                NodeList secondlevelnodes = firstlevelnode.getChildNodes();
                for (int i = 0; i < secondlevelnodes.getLength(); i++) {
                    Node node = secondlevelnodes.item(i);
                    if (isRdfAboutDataBnfNode(node) && checkNodeType(node.getFirstChild(), "dc:subject")) {
                        secondlevelnodesWithFilter.add(node);
                    }
                }
            }
        }
        return secondlevelnodesWithFilter;
    }

    private static ArrayList<XmlNode> getThirdLevelWithFilter(ArrayList<Node> secondlevelnodes) {
        ArrayList<XmlNode> xmlnodes = new ArrayList<>();
        XmlNode xmlnode;


        for (Node node : secondlevelnodes) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                
                xmlnode = new XmlNode();
                xmlnode.setUrl(node.getAttributes().item(0).getNodeValue());
                xmlnode.setExternalId(getExternalID(node.getAttributes().item(0).getNodeValue()));

                xmlnode = (getThirdLevelAttributes(node.getChildNodes(), xmlnode));

                xmlnodes.add(xmlnode);

            }
        }
        return xmlnodes;
    }

    private static XmlNode getThirdLevelAttributes(NodeList nodes, XmlNode xmlnode) {
        
        RelationsList relations = new RelationsList();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {

                relations = (nodeAttributeParserSemantic(nodes.item(i), relations));
                xmlnode = nodeAttributeParserOthers(nodes.item(i), xmlnode);
            }
        }
        
        xmlnode.setRelations(relations);
        return xmlnode;
    }

    private static RelationsList nodeAttributeParserSemantic(Node nodeinlevel3, RelationsList relations) {
        String[] nodename = {"skos:related", "skos:broader", "skos:narrower"};

        for (int i = 0; i < nodename.length; i++) {
            if (nodeinlevel3.getNodeName().contentEquals(nodename[i])) {

                String nodeValue = nodeinlevel3.getAttributes().item(0).getNodeValue();
                if (nodename[i].contentEquals("skos:related")) {
                    relations.addRelated(nodeValue);
                }
                if (nodename[i].contentEquals("skos:broader")) {
                    relations.addBroader(nodeValue);
                }
                if (nodename[i].contentEquals("skos:narrower")) {
                    relations.addNarrower(nodeValue);
                }
            }
        }
        return relations;
    }

    private static XmlNode nodeAttributeParserOthers(Node nodeinlevel3, XmlNode xmlnode) {
        String[] nodename = {"skos:prefLabel", "skos:altLabel", "skos:inScheme", "skos:closeMatch"};
        for (int i = 0; i < nodename.length; i++) {
            if (nodeinlevel3.getNodeName().contentEquals(nodename[i])) {
                
                String nodeValue = nodeinlevel3.getAttributes().item(0).getNodeValue();
                if (nodename[i].contentEquals("skos:inScheme")) {
                    xmlnode.setInScheme(nodeValue);
                }
                if (nodename[i].contentEquals("skos:prefLabel")) {
                    xmlnode.setPrefLabel(nodeinlevel3.getTextContent());
                  
                }
                if (nodename[i].contentEquals("skos:altLabel")) {
                    xmlnode.setAltLabel(nodeinlevel3.getTextContent());
                }
                if (nodename[i].contentEquals("skos:closeMatch")) {
                    if (nodeValue.contains("dewey.info/class/")) {
                        String dewey = nodeValue;
                        dewey = getDeweyExternalId(dewey);
                        xmlnode.addDeweys(dewey);
                    }
                }
            }
        }
        return xmlnode;

    }

    private static boolean isRdfAboutDataBnfNode(Node tempNode) {

        if (tempNode.hasAttributes()) {
            NamedNodeMap nodeMap = tempNode.getAttributes();
            if (nodeMap.item(0).getNodeName().equals("rdf:about") && nodeMap.item(0).getNodeValue().contains("http://data.bnf.fr")) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkNodeType(Node node, String word) {
        if (!node.getNodeName().contentEquals(word)) {
            return true;
        }

        return false;

    }

    private static String getDeweyExternalId(String deweylink) {
        String temp[];
        temp = deweylink.split("class/");
        int lastindex = temp[1].indexOf("/");
        temp[1] = temp[1].substring(0, lastindex);
        String externalId = temp[1];
        return externalId;


    }

    private static String getExternalID(String nodeValue) {
        String temp[];
        temp = nodeValue.split("/cb");
        temp[1] = temp[1].substring(0, 8);
        String externalId = temp[1];
        return externalId;
    }
}
