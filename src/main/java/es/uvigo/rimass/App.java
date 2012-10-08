package es.uvigo.rimass;

import es.uvigo.rimass.collection.Analyzer;
import es.uvigo.rimass.collection.Collection;
import es.uvigo.rimass.collection.CollectionManager;
import es.uvigo.rimass.collection.Configuration;
import es.uvigo.rimass.collection.Language;
import es.uvigo.rimass.collection.Loader;
import es.uvigo.rimass.collection.analyzers.stanfordnlp.StanfordDependenceAnalizer;
import es.uvigo.rimass.collection.loaders.medline.MedlineXMLLoader;
import es.uvigo.rimass.collection.store.MultiStore;
import es.uvigo.rimass.collection.store.neo4j.Neo4jStore;
import es.uvigo.rimass.thesaurus.Thesaurus;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) {

        Thesaurus theaurus = new Thesaurus("/home/adrian/tmp/thesaurus.MESH.xml");
        Collection collection = new Collection("medline", new Language("english", "en"));
        Configuration configuration = new Configuration("/home/adrian/tmp");

        CollectionManager collectionManager = new CollectionManager(collection, configuration);
        Loader loader = new MedlineXMLLoader();
        Analyzer analyzer = new StanfordDependenceAnalizer(theaurus);
        MultiStore store = new MultiStore();
        //store.addStore(new XMLStore());
        //store.addStore(new LuceneStore());
        
        store.addStore(new Neo4jStore());
        
        collectionManager.setLoader(loader);
        collectionManager.setAnalyzer(analyzer);
        collectionManager.setStore(store);

        collectionManager.initialize(collection, configuration);
        collectionManager.addResource("/home/adrian/tmp/medsamp2011.mini.xml");


    }
}
