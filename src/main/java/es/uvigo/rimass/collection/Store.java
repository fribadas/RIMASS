/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uvigo.rimass.collection;

import es.uvigo.rimass.collection.Collection;
import es.uvigo.rimass.core.Document;
import es.uvigo.rimass.core.Metadata;

/**
 *
 * @author ribadas
 */
public interface Store {
        public void initialize(Collection collection, Configuration configuration);
        public void storeDocument(Document<? extends Metadata> document);
        public Document<? extends Metadata> retrieveDocument(long docid);
}
