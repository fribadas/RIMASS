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
public interface Loader {
    public void initialize(Collection collection, Configuration configuration);
    public Iterable<Document<? extends Metadata>> documentIterable(String resource);
}
