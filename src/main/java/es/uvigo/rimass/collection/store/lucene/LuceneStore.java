package es.uvigo.rimass.collection.store.lucene;

import es.uvigo.rimass.collection.Collection;
import es.uvigo.rimass.collection.Configuration;
import es.uvigo.rimass.collection.Store;
import es.uvigo.rimass.core.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author ribadas
 */
public class LuceneStore implements Store {

    // Lucene field names
    public static String FIELD_DOCID = "DOCID";
    public static String FIELD_TEXT = "TEXT";
    public static String FIELD_ENTITIES = "ENTITIES";
    public static String FIELD_LEMMAS = "LEMMAS";
    public static String FIELD_DEPENDENCES = "DEPENDENCES";
    public static String FIELD_HEADS = "HEADS";
    public static String FIELD_MODIFIERS = "MODIFIERS";
    
    private File storeDir;
    private Directory directory;
    private Analyzer analizer;
    private IndexWriter indexWriter;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;

    public void initialize(Collection collection, Configuration configuration) {
        this.storeDir = new File(configuration.getProperty("base_dir") + File.separator
                + collection.getName() + File.separator + "store" + File.separator + "lucene");

        if (!this.storeDir.exists()) {
            storeDir.mkdirs();
        }

        try {
            if (this.directory == null) {
                this.directory = new SimpleFSDirectory(this.storeDir);
            }

            Map<String, Analyzer> fieldAnalyzers = new HashMap<String, Analyzer>();
            fieldAnalyzers.put(LuceneStore.FIELD_DOCID, new KeywordAnalyzer());
            fieldAnalyzers.put(LuceneStore.FIELD_TEXT, new ClassicAnalyzer(Version.LUCENE_36));
            fieldAnalyzers.put(LuceneStore.FIELD_LEMMAS, new WhitespaceAnalyzer(Version.LUCENE_36));
            fieldAnalyzers.put(LuceneStore.FIELD_ENTITIES, new WhitespaceAnalyzer(Version.LUCENE_36));
            fieldAnalyzers.put(LuceneStore.FIELD_DEPENDENCES, new WhitespaceAnalyzer(Version.LUCENE_36));
            fieldAnalyzers.put(LuceneStore.FIELD_HEADS, new WhitespaceAnalyzer(Version.LUCENE_36));
            fieldAnalyzers.put(LuceneStore.FIELD_MODIFIERS, new WhitespaceAnalyzer(Version.LUCENE_36));

            this.analizer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_36), fieldAnalyzers);

            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analizer);

            this.indexWriter = new IndexWriter(directory, config);
        } catch (IOException ex) {
            Logger.getLogger(LuceneStore.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void storeDocument(Document<? extends Metadata> document) {

        List<String> lemmas = null;
        List<String> entities = null;
        List<Dependence> dependences = null;

        if (document.getRepresentations() != null) {
            for (Representation r : document.getRepresentations()) {
                
                if (r.getType().equals("lemmas")) {
                    lemmas = ((SingleTermsRepresentation) r).getTerms();
                } else if (r.getType().equals("entities")) {
                    entities = ((SingleTermsRepresentation) r).getTerms();
                } else if (r.getType().equals("dependences")) {
                    dependences = ((DependencesRepresentation) r).getDependences();
               }
            }
        }

        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
        doc.add(new Field(LuceneStore.FIELD_DOCID, Long.toString(document.getDocid()), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
        doc.add(new Field(LuceneStore.FIELD_TEXT, document.getTitle() + document.getText(), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.NO));
        if (lemmas != null) {
            doc.add(new Field(LuceneStore.FIELD_LEMMAS, arrayToSpacedString(lemmas), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES));
        }
        if (entities != null) {
            doc.add(new Field(LuceneStore.FIELD_ENTITIES, arrayToSpacedString(entities), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES));
        }
        if (dependences != null) {
            doc.add(new Field(LuceneStore.FIELD_DEPENDENCES, dependencesToString(dependences), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES));
            doc.add(new Field(LuceneStore.FIELD_HEADS, dependenceHeadsToString(dependences), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.NO));
            doc.add(new Field(LuceneStore.FIELD_MODIFIERS, dependenceModifiersToString(dependences), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.NO));
        }

        try {
            this.indexWriter.addDocument(doc);
            this.indexWriter.prepareCommit();   // TODO: avoid too many commits (change to every 500 docs¿?)
            this.indexWriter.commit();

        } catch (Exception ex) {
            Logger.getLogger(LuceneStore.class.getName()).log(Level.SEVERE, "error indexing document " + document.getDocid(), ex);
        }

    }

    public Document<? extends Metadata> retrieveDocument(long docid) {
        return null;
    }

    private String arrayToSpacedString(List<String> labels) {
        StringBuilder builder = new StringBuilder();

        if (labels != null) {
            for (String label : labels) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                builder.append(label);
            }
        }

        return builder.toString();
    }

    private String dependencesToString(List<Dependence> dependences) {
        StringBuilder builder = new StringBuilder();

        if (dependences != null) {
            for (Dependence dependence : dependences) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                builder.append(dependence.getHead());
                builder.append("::");
                builder.append(dependence.getRelation());
                builder.append("::");
                builder.append(dependence.getModifier());
            }
        }

        return builder.toString();
    }

    private String dependenceHeadsToString(List<Dependence> dependences) {
        return dependencePartsToString(dependences, true);
    }

    private String dependenceModifiersToString(List<Dependence> dependences) {
        return dependencePartsToString(dependences, false);
    }

    private String dependencePartsToString(List<Dependence> dependences, boolean selectHeads) {
        StringBuilder builder = new StringBuilder();

        if (dependences != null) {
            for (Dependence dependence : dependences) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                if (selectHeads) {
                    builder.append(dependence.getHead());
                } else {
                    builder.append(dependence.getModifier());
                }
            }
        }

        return builder.toString();
    }
}
