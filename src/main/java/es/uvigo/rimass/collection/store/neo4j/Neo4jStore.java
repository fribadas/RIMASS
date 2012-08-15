
package es.uvigo.rimass.collection.store.neo4j;

import es.uvigo.rimass.collection.Collection;
import es.uvigo.rimass.collection.Configuration;
import es.uvigo.rimass.collection.Store;
import es.uvigo.rimass.core.Document;
import es.uvigo.rimass.core.Metadata;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

/**
 *
 * @author ribadas
 */
public class Neo4jStore implements Store {

    public void initialize(Collection collection, Configuration configuration) {
        
        ApplicationContext context = new StaticApplicationContext();

        
        
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void storeDocument(Document<? extends Metadata> document) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Document<? extends Metadata> retrieveDocument(long docid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
