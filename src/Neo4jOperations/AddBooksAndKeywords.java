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

import DataBase.DatabaseManager;
import Main.Configuration;

import Object.Book;
import Object.Dewey;
import Object.HashSetList;
import Object.Keyword;
import Object.MatchNode;
import Object.OnixGenre;
import Object.SqlKeyword;
import Utils.StringOperations;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.ReadableIndex;

/**
 *
 * @author nouri
 */
public class AddBooksAndKeywords {

    public static Logger LOG = Logger.getLogger(AddBooksAndKeywords.class.getName());
    private static Configuration conf;
    public static GraphDatabaseService graphDb;

    public AddBooksAndKeywords(Configuration pConf, GraphDatabaseService Gb) {
        graphDb = Gb;
        conf = pConf;
    }

    public void createBooksKeywordsRelations() {
        if (conf.getBoolean("create.addBooksAndKeywords")) {
            LOG.info("createBooksKeywordsRelations");

            //searchKeywordsInNeo4j();

            //nodeMatchByBooks();
            
            //searchGenresInNeo4j();
            
            nodeMatchByOnixGenres();


            Transaction tx = graphDb.beginTx();
            try {

                tx.success();
            } finally {
                tx.finish();
            }

        } else {
            LOG.info("Creation Books' Keywords' Relations is: " + conf.getBoolean("create.addBooksAndKeywords"));
        }


    }

    private static void nodeMatchByBooks() {
        ArrayList<Book> allbooks = getBooksFromDb();
        Index<Node> autoIndex = graphDb.index().forNodes("node_auto_index");
        IndexHits<Node> hits;

        int count = 0,
                max = conf.getInt("db.fetch.size"),
                total_nb_kw_match = 0,
                nb_kw = 0,
                nb_book_match = 0,
                kw;
        HashMap<Keyword, Integer> kw_match_count = new HashMap<>();

        for (Book book : allbooks) {
            List<Keyword> keywords = book.getKeywords();
            nb_kw += keywords.size();
            for (Keyword keyword : keywords) {
                String query = "prefLabel:" + "\"" + keyword.getName() + "\" AND nodeType: \"dewey\"";
                hits = autoIndex.query(query);
                if (hits.size() > 0) {
                    if (kw_match_count.containsKey(keyword)) {
                        kw = kw_match_count.get(keyword);
                        kw_match_count.remove(keyword);
                        kw_match_count.put(keyword, kw + 1);
                    } else {
                        kw_match_count.put(keyword, 1);
                    }
                    nb_book_match += 1;
                }
                for (Node node : hits) {
                    MatchNode n;
                    n = new MatchNode(node, keyword.getName());
                    total_nb_kw_match += book.matched_nodes.add(n, keyword.getName());
                }
            }
            Collections.sort(book.matched_nodes);
            count++;
            if (count >= max) {
                break;
            }
        }

        for (Book book : allbooks) {
            book.matched_nodes.Reduce();
            String str = book.matched_nodes.toString();
            if (!str.equals("")) {
                LOG.info("\nbook_id : " + book.getBook_id() + "\n" + str);
            }
        }

        LOG.info("KEYWORD MATCH : " + total_nb_kw_match + "/" + nb_kw);
        LOG.info("BOOK MATCH : " + nb_book_match + "/" + allbooks.size());

        for (Map.Entry<Keyword, Integer> entry : kw_match_count.entrySet()) {
            Keyword keyword = entry.getKey();
            Integer integer = entry.getValue();
            LOG.info("Keyword : " + keyword.getName() + " matched " + integer + " times");
        }
        LOG.info("KEYWORD MATCH : " + total_nb_kw_match + "/" + nb_kw);
        LOG.info("BOOK MATCH : " + nb_book_match + "/" + allbooks.size());
    }

    private static void nodeMatchByOnixGenres() {
        ArrayList<OnixGenre> genres = getGenresFromDbAsAnObject();
        genres = StringOperations.handleOnixGenres(genres);

        Index<Node> autoIndex = graphDb.index().forNodes("node_auto_index");
        IndexHits<Node> hits;

        int count = 0,
                max = conf.getInt("db.fetch.size"),
                total_nb_kw_match = 0,
                nb_kw = 0;
        HashSetList<MatchNode> matched_nodes = new HashSetList<>();

        nb_kw += genres.size();

        for (OnixGenre onixgenre : genres) {
            HashSetList<String> altnames = onixgenre.getAltnames();
            
            for (Iterator<String> it = altnames.iterator(); it.hasNext();) {
                String altname = it.next();
                String query = "prefLabel:" + "\"" + altname + "\" AND nodeType: \"dewey\"";
                hits = autoIndex.query(query);

                for (Node node : hits) {
                    MatchNode n;
                    n = new MatchNode(node, altname);
                    total_nb_kw_match += matched_nodes.add(n, altname);
                }

            }

        }
        Collections.sort(matched_nodes);
        count++;
        matched_nodes.Reduce();
        String str = matched_nodes.toString();
        if (!str.equals("")) {
            LOG.info("\n" + str);
        }

        LOG.info("KEYWORD MATCH : " + total_nb_kw_match + "/" + nb_kw);

        
    }

    private static ArrayList<Book> getBooksFromDb() {
        ArrayList<Book> books = null;
        try {
            DatabaseManager db = new DatabaseManager(conf);
            books = db.getBooks();
            books = db.getBooksKeywords(books);
            books = db.getBooksKeywordsDetails(books);

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            java.util.logging.Logger.getLogger(AddBooksAndKeywords.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return books;

    }

    private static void searchKeywordsInNeo4j() {
        ArrayList<Keyword> keywords = getKeywordsFromDb();
        //printKeywords(keywords);
        ArrayList<SqlKeyword> matchedDeweyNodes = new ArrayList<>();
        ArrayList<SqlKeyword> matchedRameauNodes = new ArrayList<>();

        int keywordcount = 1;
        for (Keyword keyword : keywords) {
            LOG.debug("\n Process: " + keywordcount + " / " + keywords.size());
            matchedDeweyNodes = getNodesUsingPreflabelAndNodeType(matchedDeweyNodes, keyword, "dewey");
            //matchedRameauNodes = getNodesUsingPreflabelAndNodeType(matchedRameauNodes, keyword, "word");
            keywordcount++;
        }

        //setDeweyExternalIdsToDb(matchedDeweyNodes);        
        //setRameauExternalIdsToDb(matchedRameauNodes);
        //LOG.debug("Size of matchedRameauNodes: "+matchedRameauNodes.size());
        LOG.debug("Size of matchedDeweyNodes: " + matchedDeweyNodes.size());
    }
    
    private static void searchGenresInNeo4j() {
        ArrayList<Keyword> genres = getGenresFromDb();
        //printKeywords(keywords);
        ArrayList<SqlKeyword> matchedDeweyNodes = new ArrayList<>();
        

        int keywordcount = 1;
        for (Keyword genre : genres) {
            LOG.debug("\n Process: " + keywordcount + " / " + genres.size());
            matchedDeweyNodes = getNodesUsingPreflabelWithParserAndNodeType(matchedDeweyNodes, genre, "dewey");
            //matchedRameauNodes = getNodesUsingPreflabelAndNodeType(matchedRameauNodes, keyword, "word");
            keywordcount++;
        }

        //setDeweyExternalIdsToDb(matchedDeweyNodes);        
        //setRameauExternalIdsToDb(matchedRameauNodes);
        //LOG.debug("Size of matchedRameauNodes: "+matchedRameauNodes.size());
        LOG.debug("Size of matchedDeweyNodes: " + matchedDeweyNodes.size());
    }

    private static ArrayList<SqlKeyword> getNodesUsingPreflabelAndNodeType(ArrayList<SqlKeyword> sqlkeywords, Keyword pKeyword, String nodeType) {
        IndexManager index = graphDb.index();
        Index<Node> autoIndex = graphDb.index().forNodes("node_auto_index");
        String key = pKeyword.getName();
        //LOG.debug("--------------------------------");
        // LOG.debug("Keyword: " + key);            
        String query = "prefLabel:" + "\"" + key + "\"" + " AND nodeType:" + nodeType;
            //LOG.debug(query);
            IndexHits<Node> hits = autoIndex.query(query);
            //LOG.debug("hit size: " + hits.size());
            SqlKeyword sqlkeyword;
            if (hits.size() > 0 && !key.isEmpty()) {
                for (Node node : hits) {
                    LOG.debug("--------------------------------");
                    LOG.debug("Keyword: " + key);
                    LOG.debug("hit size: " + hits.size());
                    sqlkeyword = new SqlKeyword();
                    sqlkeyword.setKeyword_id(pKeyword.getKeyword_id());
                    sqlkeyword.setExternal_id(node.getProperty("externalID").toString());
                    LOG.debug(key + ", Keyword_id: " + sqlkeyword.getKeyword_id() + " externalID: " + sqlkeyword.getExternal_id());
                    sqlkeywords.add(sqlkeyword);
                }

            }
        

        return sqlkeywords;
    }
    
    private static ArrayList<SqlKeyword> getNodesUsingPreflabelWithParserAndNodeType(ArrayList<SqlKeyword> sqlkeywords, Keyword pKeyword, String nodeType) {
        IndexManager index = graphDb.index();
        Index<Node> autoIndex = graphDb.index().forNodes("node_auto_index");
        String key = pKeyword.getName();
        //LOG.debug("--------------------------------");
        // LOG.debug("Keyword: " + key);
        String[] parsedKeys = StringOperations.stringParserDelimIsPipe(key);
        for (String parsedKey : parsedKeys) {
            String query = "prefLabel:" + "\"" + parsedKey.trim() + "\"" + " AND nodeType:" + nodeType;
            //LOG.debug(query);
            IndexHits<Node> hits = autoIndex.query(query);
            //LOG.debug("hit size: " + hits.size());
            SqlKeyword sqlkeyword;
            if (hits.size() > 0 && !parsedKey.isEmpty()) {
                for (Node node : hits) {
                    LOG.debug("--------------------------------");
                    LOG.debug("Hit keyword : "+node.getProperty("prefLabel"));
                    LOG.debug("Keyword : " + parsedKey);
                    LOG.debug("hit size : " + hits.size());
                    sqlkeyword = new SqlKeyword();
                    sqlkeyword.setKeyword_id(pKeyword.getKeyword_id());
                    sqlkeyword.setExternal_id(node.getProperty("externalID").toString());
                    LOG.debug(parsedKey + ", " + " externalID: " + sqlkeyword.getExternal_id());
                    sqlkeywords.add(sqlkeyword);
                }

            }
        }
        return sqlkeywords;
    }
    
    

    private static ArrayList<Keyword> getKeywordsFromDb() {
        ArrayList<Keyword> keywords = null;
        try {
            DatabaseManager db = new DatabaseManager(conf);
            keywords = db.getKeywordsNameWithKeywordId();

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            LOG.error("ClassNotFoundExceptionKeyword | InstantiationException | IllegalAccessException | SQLException", ex);
        }
        return keywords;

    }
    
    private static ArrayList<Keyword> getGenresFromDb() {
        ArrayList<Keyword> genres = null;
        try {
            DatabaseManager db = new DatabaseManager(conf);
            genres = db.getOnixGenre();

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            LOG.error("ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException", ex);
        }
        return genres;

    }
    
    private static ArrayList<OnixGenre> getGenresFromDbAsAnObject() {
        ArrayList<OnixGenre> genres = null;
        try {
            DatabaseManager db = new DatabaseManager(conf);
            genres = db.getOnixGenreAsAnOnixGenreObject();

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            LOG.error("ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException", ex);
        }
        return genres;

    }

    private static void setDeweyExternalIdsToDb(ArrayList<SqlKeyword> matchedDeweyNodes) {
        try {

            DatabaseManager db = new DatabaseManager(conf);
            db.addDeweyExternalIdsToSql(matchedDeweyNodes);

        } catch (ClassNotFoundException ex) {
            LOG.error("ClassNotFoundException", ex);
        } catch (InstantiationException ex) {
            LOG.error("InstantiationException", ex);
        } catch (IllegalAccessException ex) {
            LOG.error("IllegalAccessException", ex);
        } catch (SQLException ex) {
            LOG.error("SQLException", ex);
        } catch (IOException ex) {
            LOG.error("IOException", ex);
        }

    }

    private static void setRameauExternalIdsToDb(ArrayList<SqlKeyword> matchedRameauNodes) {
        try {

            DatabaseManager db = new DatabaseManager(conf);
            db.addRameauExternalIdsToSql(matchedRameauNodes);

        } catch (ClassNotFoundException ex) {
            LOG.error("ClassNotFoundException", ex);
        } catch (InstantiationException ex) {
            LOG.error("InstantiationException", ex);
        } catch (IllegalAccessException ex) {
            LOG.error("IllegalAccessException", ex);
        } catch (SQLException ex) {
            LOG.error("SQLException", ex);
        } catch (IOException ex) {
            LOG.error("IOException", ex);
        }

    }

    private static void printKeywords(List<Keyword> keywords) {
        LOG.debug("Size of Keywords " + keywords.size());

        for (Keyword keyword : keywords) {
            LOG.debug("Keyword Id: " + keyword.getKeyword_id() + " Keyword name: " + keyword.getName());
        }

    }

    private void countMatchedKeywordsinRameau(Configuration conf) {
        LOG.info("countMatchedKeywordsinRameau");
        SearchWithCypher swc = new SearchWithCypher(conf);
        ArrayList<Book> books = getBooksFromDb();
        ArrayList<String> nonmatchedKeywords = new ArrayList<>();
        int countTotal = 0, countMatched = 0;

        for (Book book : books) {
            for (int i = 0; i < book.getKeywords().size(); i++) {
                if (swc.findExactPreflabelsInWords(book.getKeywords().get(i).getName())) {
                    countMatched++;
                } else {
                    nonmatchedKeywords.add(book.getKeywords().get(i).getName());
                }
                countTotal++;
            }

        }
        swc.dbShutdown();
        LOG.info("There are " + countMatched + " Keywords matched with Rameau and There are " + nonmatchedKeywords.size() + " Keywords didn't Match");
        LOG.info("List of nonmatched Keywords");
        printArray(nonmatchedKeywords);


    }

    private static void printArray(ArrayList<String> array) {

        for (String element : array) {
            LOG.info(element);
        }

    }

    private static Node createBookNode(Book pbook) {
        Node bookNode = null;
        if (pbook.getSizeOfKeywords() != 0) {
            bookNode = graphDb.createNode();
            bookNode.setProperty("nodeType", "book");
            bookNode.setProperty("prefLabel", pbook.getTitle());
            bookNode.setProperty("externalID", pbook.getBook_id());

        }

        return bookNode;

    }

    private static Node createKeywordNode(Keyword keyword) {
        Node keywordNode;

        keywordNode = graphDb.createNode();
        keywordNode.setProperty("nodeType", "keyword");
        keywordNode.setProperty("prefLabel", keyword.getName());
        keywordNode.setProperty("externalID", keyword.getKeyword_id());

        return keywordNode;

    }

    private static boolean isNodeExist(int externalID, String nodeType) {
        Node result = getNodeUsingExternalIdWithNodeType(externalID, nodeType);
        if (result == null) {
            return false;

        }
        return true;

    }

    private static Node getNodeUsingExternalIdWithNodeType(int externalID, String nodeType) {
        ReadableIndex<Node> autoNodeIndex = graphDb.index().getNodeAutoIndexer().getAutoIndex();
        IndexHits<Node> nodesWithDeclaredType = autoNodeIndex.get("nodeType", nodeType);
        LOG.info("size " + nodesWithDeclaredType.size());
        Node outputNode = null;
        for (Node node : nodesWithDeclaredType) {
            int searched_externalId = Integer.parseInt(node.getProperty("externalID", externalID).toString());
            if (searched_externalId == externalID) {
                outputNode = node;

            }

        }
        return outputNode;
    }

    private void getNodesByPrefLabel(String adam) {
        ReadableIndex<Node> index = graphDb.index().getNodeAutoIndexer().getAutoIndex();
        IndexHits<Node> hits = index.query("adam");


        for (Node n : hits) {
            System.out.println(n.getProperty("prefLabel"));
        }
    }
}
