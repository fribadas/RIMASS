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
    public static void main2(String[] args) {
        
        String text = "The nirIX gene cluster of Paracoccus denitrificans is located between the nir and nor gene clusters encoding nitrite and nitric oxide reductases respectively. The NirI sequence corresponds to that of a membrane-bound protein with six transmembrane helices, a large periplasmic domain and cysteine-rich cytoplasmic domains that resemble the binding sites of [4Fe-4S] clusters in many ferredoxin-like proteins. NirX is soluble and apparently located in the periplasm, as judged by the predicted signal sequence. NirI and NirX are homologues of NosR and NosX, proteins involved in regulation of the expression of the nos gene cluster encoding nitrous oxide reductase in Pseudomonas stutzeri and Sinorhizobium meliloti. Analysis of a NirI-deficient mutant strain revealed that NirI is involved in transcription activation of the nir gene cluster in response to oxygen limitation and the presence of N-oxides. The NirX-deficient mutant transiently accumulated nitrite in the growth medium, but it had a final growth yield similar to that of the wild type. Transcription of the nirIX gene cluster itself was controlled by NNR, a member of the family of FNR-like transcriptional activators. An NNR binding sequence is located in the middle of the intergenic region between the nirI and nirS genes with its centre located at position -41.5 relative to the transcription start sites of both genes. Attempts to complement the NirI mutation via cloning of the nirIX gene cluster on a broad-host-range vector were unsuccessful, the ability to express nitrite reductase being restored only when the nirIX gene cluster was reintegrated into the chromosome of the NirI-deficient mutant via homologous recombination in such a way that the wild-type nirI gene was present directly upstream of the nir operon.";
        
        String parts[] = text.split("\\. ");
        
        for (String part : parts){
            
            part += ".";
            System.out.println("PARTE ");
            String[] tokens = part.split(" |\t|\n");
            for (String t :tokens){
                System.out.println(" "+t);
            }
        
            
        }
        
        
    }
    public static void main(String[] args) {

        Thesaurus theaurus = new Thesaurus("/home/ribadas/tmp/CJT/thesaurus.MESH.xml");
        Collection collection = new Collection("medline", new Language("english", "en"));
        Configuration configuration = new Configuration();

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
        collectionManager.addResource("/home/ribadas/NetBeansProjects/CJT/datos/medsamp2011h.xml");


    }
}
