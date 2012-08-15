package es.uvigo.rimass.collection;

import es.uvigo.rimass.collection.Collection;
import es.uvigo.rimass.core.Document;
import es.uvigo.rimass.thesaurus.Thesaurus;

/**
 *
 * @author ribadas
 */
public class CollectionManager {
    private Collection collection;
    private Configuration configuration;
    
    private Loader loader;
    private Analyzer analyzer;
    private Store store;

    public CollectionManager() {
    }

    public CollectionManager(Collection collection, Configuration configuration) {
        this.collection = collection;
        this.configuration = configuration;
    }

    
    
    public CollectionManager(Collection collection) {
        this(collection, null);                        
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public Loader getLoader() {
        return loader;
    }

    public void setLoader(Loader loader) {
        this.loader = loader;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }
    
    
    public void initialize(Collection collection, Configuration configuration){
        this.collection = collection;
        this.configuration = configuration;
        
        if (this.loader != null) {
            this.loader.initialize(collection, configuration);                    
        }

        if (this.analyzer != null) {
            this.analyzer.initialize(collection, configuration);                    
        }
        
        if (this.store != null) {
            this.store.initialize(collection, configuration);                    
        }                
    }
    
    public void addResource(String resource){
        if (this.loader != null){
            for (Document d : loader.documentIterable(resource)){
                 if (this.analyzer != null){
                     d = this.analyzer.analizeDocument(d);
                 }
                 if (this.store != null){
                     this.store.storeDocument(d);                                                      
                 }            
            }        
        }
    }
    
    
    public Document retrieveDocument(long docid) {
        if (this.store != null){
            return this.store.retrieveDocument(docid);
        }
        else {
            return null;
        }
    }
    
}
