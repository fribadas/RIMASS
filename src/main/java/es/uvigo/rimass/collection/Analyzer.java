/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uvigo.rimass.collection;

import es.uvigo.rimass.collection.Collection;
import es.uvigo.rimass.core.Document;
import es.uvigo.rimass.core.Metadata;
import es.uvigo.rimass.thesaurus.Thesaurus;

/**
 *
 * @author ribadas
 */
public interface Analyzer {
    public Document<? extends Metadata> analizeDocument(Document<? extends Metadata> document);

    public void initialize(Collection collection, Configuration configuration);
}
