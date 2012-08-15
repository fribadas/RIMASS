package es.uvigo.rimass;

import es.uvigo.rimass.collection.*;
import es.uvigo.rimass.collection.analyzers.stanfordnlp.StanfordDependenceAnalizer;
import es.uvigo.rimass.collection.loaders.medline.MedlineXMLLoader;
import es.uvigo.rimass.collection.store.MultiStore;
import es.uvigo.rimass.collection.store.lucene.LuceneStore;
import es.uvigo.rimass.collection.store.xml.XMLStore;
import es.uvigo.rimass.thesaurus.Thesaurus;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) {

        Thesaurus theaurus = new Thesaurus("/tmp/thesaurus.MESH.xml");
        Collection collection = new Collection("medline", new Language("english", "en"));
        Configuration configuration = new Configuration("/tmp");

        CollectionManager collectionManager = new CollectionManager(collection, configuration);
        Loader loader = new MedlineXMLLoader();
        Analyzer analyzer = new StanfordDependenceAnalizer(theaurus);
        MultiStore store = new MultiStore();
        store.addStore(new XMLStore());
        store.addStore(new LuceneStore());

        collectionManager.setLoader(loader);
        collectionManager.setAnalyzer(analyzer);
        collectionManager.setStore(store);

        collectionManager.initialize(collection, configuration);
        collectionManager.addResource("/tmp/medsamp2011h.xml");


    }
}
