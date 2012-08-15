package es.uvigo.rimass.collection.analyzers;

import es.uvigo.rimass.collection.Analyzer;
import es.uvigo.rimass.collection.Collection;
import es.uvigo.rimass.collection.Configuration;
import es.uvigo.rimass.core.Document;
import es.uvigo.rimass.core.Metadata;
import es.uvigo.rimass.thesaurus.Thesaurus;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ribadas
 */
public class MultiAnaliyzer implements Analyzer {

    private List<Analyzer> analyzers;

    public MultiAnaliyzer() {
        this.analyzers = new ArrayList<Analyzer>();
    }

    public MultiAnaliyzer(List<Analyzer> analyzers) {
        this.analyzers = analyzers;
    }

    public List<Analyzer> getAnalyzers() {
        return analyzers;
    }

    public void setAnalyzers(List<Analyzer> analyzers) {
        this.analyzers = analyzers;
    }

    
    public void addAnalizer(Analyzer analyzer){
        if (this.analyzers == null){
            this.analyzers = new ArrayList<Analyzer>();                    
        }
        this.analyzers.add(analyzer);
    }
    
    
    public void initialize(Collection collection, Configuration configuration) {
        if (this.analyzers != null) {
            for (Analyzer a : this.analyzers) {
                a.initialize(collection, configuration);
            }
        }
    }

    public Document<? extends Metadata> analizeDocument(Document<? extends Metadata> document) {
        if (this.analyzers != null) {
            for (Analyzer a : this.analyzers) {
                document = a.analizeDocument(document);
            }
        }
        return document;
    }
}
