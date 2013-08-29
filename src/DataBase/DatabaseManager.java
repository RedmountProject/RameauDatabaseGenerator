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
package DataBase;

import Main.Configuration;
import Object.Book;
import Object.Dewey;
import Object.HashSetList;
import Object.Keyword;
import Object.OnixGenre;
import Object.SqlKeyword;
import Parsing.DeweyParser;
import Utils.StringOperations;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class DatabaseManager {

    private static final String[] InputReplace = {"é", "è", "ê", "ë", "û", "ù", "ü", "ï", "î", "à", "â", "ö", "ô", "ç"};
    private static final String[] OutputReplace = {"e", "e", "e", "e", "u", "u", "u", "i", "i", "a", "a", "o", "o", "c"};
    public static final Logger LOG = Logger.getLogger(DatabaseManager.class.getName());
    Jdbc s = new Jdbc();
    ResultSet rs;
    private static PreparedStatement getNames = null;
    private Connection FetchCon;
    private String DatabaseName;
    private static Configuration conf;
    private final Integer FETCH_SIZE;

    public DatabaseManager(Configuration pConf) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        FETCH_SIZE = pConf.getInt("db.fetch.size");
        conf = pConf;
        FetchConnection();


    }

    private void FetchConnection() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        String DatabaseAddress = conf.getString("db.address");
        String DatabasePort = conf.getString("db.port");
        String DBName = conf.getString("db.name");
        this.DatabaseName = DBName;
        String DatabaseUserName = conf.getString("db.username");
        String DatabasePassword = conf.getString("db.password");

        FetchCon = Jdbc.Connection(DatabaseAddress, DatabasePort, DBName, DatabaseUserName, DatabasePassword, false);

    }

    public ArrayList<String> getKeywordsName() throws SQLException {
        LOG.info("getKeywordsName");

        String requete = "SELECT name FROM `" + DatabaseName + "`.`keyword`;";
        PreparedStatement statement = FetchCon.prepareStatement(requete);
        rs = statement.executeQuery();

        ArrayList<String> keywords = new ArrayList<>();
        while (rs.next()) {
            String keyword = rs.getString(1);
            keywords.add(keyword);
        }
        return keywords;

    }

    public ArrayList<Keyword> getOnixGenre() throws SQLException {
        LOG.info("getOnixGenre");

        String requete = "SELECT alt_name FROM `" + "catalog_dev" + "`.`onix_genre`;";
        PreparedStatement statement = FetchCon.prepareStatement(requete);
        rs = statement.executeQuery();
        String[] parsedKeys;
        ArrayList<Keyword> genres = new ArrayList<>();
        ArrayList<String> onix_words = new ArrayList<>();

        HashSetList<Keyword> kws = new HashSetList<>();

        Keyword keyword;
        while (rs.next()) {
            String genre = rs.getString(1).trim();
            if (rs.getString(1).contains("|")) {
                parsedKeys = StringOperations.stringParserDelimIsPipe(genre);
                for (String parsedKey : parsedKeys) {
                    kws.add(new Keyword(parsedKey.trim()));
                    //onix_words.add(parsedKey);
                }
            } else if(!genre.isEmpty()) {
                kws.add(new Keyword(genre.trim()));
                //onix_words.add(rs.getString(1));
            }
        }
        
//        HashSet templist = new HashSet();
//        templist.addAll(onix_words);
//        onix_words.clear();
//        onix_words.addAll(templist);
//
//        for (String string : onix_words) {
//            keyword = new Keyword();
//            keyword.setName(string);
//            genres.add(keyword);
//        }


        return kws;

    }
    
     public ArrayList<OnixGenre> getOnixGenreAsAnOnixGenreObject() throws SQLException {
         LOG.info("getOnixGenreAsAnOnixGenreObject");

         String requete = "SELECT alt_name, name FROM `" + "catalog_dev" + "`.`onix_genre`;";
         PreparedStatement statement = FetchCon.prepareStatement(requete);
         rs = statement.executeQuery();
         String[] parsedKeys;
         ArrayList<Keyword> genres = new ArrayList<>();
         ArrayList<OnixGenre> onix_words = new ArrayList<>();

         //HashSetList<Keyword> kws = new HashSetList<>();

         Keyword keyword;
         OnixGenre onix_word;
         while (rs.next()) {
             
             String genre = rs.getString(1).trim();
             String name = stripAccents(rs.getString(2)).trim();
             onix_word = new OnixGenre();
             onix_word.setName(name);
             
             if (rs.getString(1).contains("|")) {
                 parsedKeys = StringOperations.stringParserDelimIsPipe(genre);
                 
                 for (String parsedKey : parsedKeys) {
                     onix_word.addAltname(parsedKey.trim());
                 }
                 
             } else if (!genre.isEmpty()) {
                 onix_word.addAltname(genre);
                 //onix_words.add(rs.getString(1));
             }else if(genre.isEmpty()){
                 onix_word.addAltname(name);
             }             
             onix_words.add(onix_word);
         }
 

        return onix_words;

    }

    public ArrayList<Keyword> getKeywordsNameWithKeywordId() throws SQLException {
        LOG.info("getKeywordsName");

        String requete = "SELECT keyword_id, name FROM `" + DatabaseName + "`.`keyword`;";
        PreparedStatement statement = FetchCon.prepareStatement(requete);
        rs = statement.executeQuery();

        ArrayList<Keyword> keywords = new ArrayList<>();
        Keyword keyword;
        while (rs.next()) {
            keyword = new Keyword();
            keyword.setKeyword_id(rs.getInt(1));
            keyword.setName(rs.getString(2));
            keywords.add(keyword);
        }
        return keywords;

    }

    public void addDeweyExternalIdsToSql(ArrayList<SqlKeyword> matchedDeweyNodes) throws IOException, SQLException {

        PreparedStatement statement = null;
        for (SqlKeyword sqlKeyword : matchedDeweyNodes) {
            String insertTableSQL = "INSERT INTO keyword_deweyNode (keyword_id_fk, deweyNode_external_id) VALUES( " + sqlKeyword.getKeyword_id() + ", '" + sqlKeyword.getExternal_id() + "');";
            LOG.debug(insertTableSQL);
            try {
                statement = FetchCon.prepareStatement(insertTableSQL);
                statement.executeUpdate();
            } catch (SQLException ex) {
                LOG.error(DatabaseManager.class.getName() + " SQLException", ex);
            }
        }
        FetchCon.commit();

    }

    public void addRameauExternalIdsToSql(ArrayList<SqlKeyword> matchedRameauNodes) throws IOException, SQLException {
        PreparedStatement statement = null;
        for (SqlKeyword sqlKeyword : matchedRameauNodes) {
            String insertTableSQL = "INSERT INTO keyword_rameauNode (keyword_id_fk, rameauNode_external_id) VALUES( " + sqlKeyword.getKeyword_id() + ", '" + sqlKeyword.getExternal_id() + "');";
            LOG.debug(insertTableSQL);
            try {
                statement = FetchCon.prepareStatement(insertTableSQL);
                statement.executeUpdate();
            } catch (SQLException ex) {
                LOG.error(DatabaseManager.class.getName() + " SQLException", ex);
            }
        }
        FetchCon.commit();

    }

    public ArrayList<Book> getBooks() throws SQLException {
        LOG.info("getBooks");

        String requete = "SELECT book_id, title FROM `" + DatabaseName + "`.`book` LIMIT " + FETCH_SIZE + ";";
        PreparedStatement statement = FetchCon.prepareStatement(requete);
        rs = statement.executeQuery();
        Book book;

        ArrayList<Book> books = new ArrayList<>();
        while (rs.next()) {
            book = new Book();
            int id = rs.getInt(1);
            String title = rs.getString(2);
            book.setBook_id(id);
//            LOG.info("book id: "+id);
//            LOG.info("book title: "+title);
            book.setTitle(title);
            books.add(book);
        }
        return books;

    }
    
    public ArrayList<Book> getBooksFromCatalog_dev() throws SQLException {
        LOG.info("getBooks");

        String requete = "SELECT book_id, title FROM `" + "catalog_dev" + "`.`book` WHERE book_id IN ( SELECT DISTINCT book_id from book_onixGenre) LIMIT " + FETCH_SIZE + ";";
        
        PreparedStatement statement = FetchCon.prepareStatement(requete);
        rs = statement.executeQuery();
        Book book;

        ArrayList<Book> books = new ArrayList<>();
        while (rs.next()) {
            book = new Book();
            int id = rs.getInt(1);
            String title = rs.getString(2);
            book.setBook_id(id);
//            LOG.info("book id: "+id);
//            LOG.info("book title: "+title);
            book.setTitle(title);
            books.add(book);
        }
        return books;

    }
    public ArrayList<Book> getOnixGenresByBooks(ArrayList<Book> books) throws SQLException {
         LOG.info("getOnixGenresByBook");
         

        for (Book book : books) {
            int book_id = book.getBook_id();           
            

            String requete = "SELECT g.alt_name, g.name, g.onixGenre_id "
                    + "FROM catalog_dev.onix_genre g "
                    + "INNER JOIN book_onixGenre b ON ( g.onixGenre_id = b.onixGenre_id_fk ) "
                    + "WHERE b.book_id_fk = " + book_id + ";";

            PreparedStatement statement = FetchCon.prepareStatement(requete);
            rs = statement.executeQuery();
            
            String[] parsedKeys;            
            OnixGenre onix_word;
            
            while (rs.next()) {

                String genre = rs.getString(1).trim();
                String name = stripAccents(rs.getString(2)).trim();
                onix_word = new OnixGenre();
                onix_word.setName(name);

                if (rs.getString(1).contains("|")) {
                    parsedKeys = StringOperations.stringParserDelimIsPipe(genre);

                    for (String parsedKey : parsedKeys) {
                        onix_word.addAltname(parsedKey.trim());
                    }

                } else if (!genre.isEmpty()) {
                    onix_word.addAltname(genre);
                    //onix_words.add(rs.getString(1));
                } else if (genre.isEmpty()) {
                    onix_word.addAltname(name);
                }
                book.addOnixGenre(onix_word);
            }

        }
        return books;
    }
    


    public ArrayList<Book> getBooksKeywords(ArrayList<Book> books) throws SQLException {
        LOG.info("getBooksKeywords");

        Keyword keyword;
        for (Book book : books) {
            int book_id = book.getBook_id();
            String requete = "SELECT keyword_id_fk, keyword_weight FROM `" + DatabaseName + "`.`book_keyword` WHERE book_id_fk = " + book_id + " ;";
            PreparedStatement statement = FetchCon.prepareStatement(requete);
            rs = statement.executeQuery();
            while (rs.next()) {
                keyword = new Keyword();
                int keyword_id_fk = rs.getInt(1);
                int keyword_weight = rs.getInt(2);
//                System.out.println("keyword_id_fk "+keyword_id_fk);
//                System.out.println("keyword_weight "+keyword_weight);
                keyword.setKeyword_id(keyword_id_fk);
                keyword.setKeyword_weight(keyword_weight);
                book.addKeyword(keyword);
            }
        }

        return books;

    }

    public ArrayList<Book> getBooksKeywordsDetails(ArrayList<Book> books) throws SQLException {
        LOG.info("getBooksKeywordsDetails");


        for (Book book : books) {
            for (Keyword word : book.getKeywords()) {
                int keyword_id = word.getKeyword_id();
                String requete = "SELECT name, global_weight, global_count FROM `" + DatabaseName + "`.`keyword` WHERE keyword_id = " + keyword_id + " ;";
                PreparedStatement statement = FetchCon.prepareStatement(requete);
                rs = statement.executeQuery();
                while (rs.next()) {

                    word.setName(rs.getString(1));
                    word.setGlobal_weight(rs.getInt(2));
                    word.setGlobal_weight(rs.getInt(3));
//                    System.out.println("keyword_id_fk " + word.getKeyword_id());
//                    System.out.println("keyword name " + word.getName());

                }
            }


        }
        //writeKeywordsToFile(books);
        return books;

    }

    public void addDeweysToSql() throws IOException, SQLException {
        DeweyParser deweyParser = new DeweyParser((new File(conf.getProperty("deweySql"))));
        ArrayList<Dewey> deweys = deweyParser.parseDewey();
        PreparedStatement statement = null;
        for (Dewey dewey : deweys) {
            String insertTableSQL = "INSERT INTO dewey (dewey_id, dewey_name) VALUES( " + dewey.getDeweyExternalId() + ", '" + StringEscapeUtils.escapeSql(dewey.getCatalogName()) + "');";
            System.out.println(insertTableSQL);
            try {
                statement = FetchCon.prepareStatement(insertTableSQL);
                statement.executeUpdate();
            } catch (SQLException ex) {
                LOG.error(DatabaseManager.class.getName() + " SQLException", ex);
            }
        }
        FetchCon.commit();

    }

    public static void writeKeywordsToFile(ArrayList<Book> books) {
        LOG.info("Keywords are writting to a file...");


        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("file/keywords_output.txt", false));

            for (Book book : books) {
                for (int i = 0; i < book.getKeywords().size(); i++) {
                    String line = book.getKeywords().get(i).getKeyword_id() + ";" + book.getKeywords().get(i).getName();
                    out.write(line);
                    out.newLine();
                }

            }

            out.close();
        } catch (IOException e) {
        }
        LOG.info("Writting is done...");
    }

    public static String stripAccents(String s) {
        s = StringUtils.replaceEachRepeatedly(s.toLowerCase(), InputReplace, OutputReplace);
        s = StringEscapeUtils.escapeSql(s);
        s = Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD);
        s = s.replaceAll("('+|’+)", "'");
        s = s.replaceAll("\"+", "");


        //LOG.info("after stripAccents: " + s);
        return s;
    }
}
