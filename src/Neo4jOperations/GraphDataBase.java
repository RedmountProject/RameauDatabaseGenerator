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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.Index;

/**
 *
 * @author nouri
 */
public class GraphDataBase {

    private GraphDataBase() {
    }

    public static GraphDatabaseService newInstance(String db_path) {
        final GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(db_path).
                setConfig(GraphDatabaseSettings.node_keys_indexable, "externalID,prefLabel,nodeType").
                setConfig(GraphDatabaseSettings.relationship_keys_indexable, "RelationType").
                setConfig(GraphDatabaseSettings.node_auto_indexing, "true").
                setConfig(GraphDatabaseSettings.relationship_auto_indexing, "true").
                newGraphDatabase();



        Index<Node> autoIndex = graphDb.index().forNodes("node_auto_index");

        graphDb.index().setConfiguration(autoIndex, "type", "fulltext");



//        Index<Node> index = graphDb.index().forNodes( "node_auto_index" );
//        ((LuceneIndex<Node>) index).setCacheCapacity( "prefLabel", 300000 );

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });

        return graphDb;
    }

    
    public static enum RelationTypes implements RelationshipType {

        BROADER, NARROWER, RELATED, DEWEY_NARROWER, DEWEY_BROADER, KEYWORD_BOOK, BOOK_KEYWORD, BOOK_DEWEY, DEWEY_BOOK
    }
}
