
package es.uvigo.rimass.collection.analyzers.stanfordnlp;

import es.uvigo.rimass.collection.Analyzer;
import es.uvigo.rimass.collection.Collection;
import es.uvigo.rimass.collection.Configuration;
import es.uvigo.rimass.core.Document;
import es.uvigo.rimass.core.Metadata;
import es.uvigo.rimass.thesaurus.Thesaurus;


/**
 *
 * @author ribadas
 */
public class StanfordSingleAnalizer implements Analyzer {

    public void initialize(Collection collection, Configuration configuration) {
        
    }

    public Document<? extends Metadata> analizeDocument(Document<? extends Metadata> document) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
