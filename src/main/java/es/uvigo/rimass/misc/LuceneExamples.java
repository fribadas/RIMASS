/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uvigo.rimass.misc;

import es.uvigo.rimass.collection.store.lucene.LuceneStore;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author ribadas
 */
public class LuceneExamples {

    private String lucenePath;
    private Directory directory;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
    private QueryParser parserDOCID;
    private QueryParser parserTEXT;
    private QueryParser parserDEPENDENCES;
    private QueryParser parserHEADS;

    public LuceneExamples(String lucenePath) throws IOException {
        this.lucenePath = lucenePath;
        initialize();
    }

    public static void main(String[] args) throws IOException {

        LuceneExamples examples = new LuceneExamples("/tmp/medline/store/lucene");

        // Retrieve Lucene document by DOCID 19196247
        examples.retrieveDocument("19196247");

        // Search by text
        examples.searchText("diabetes symptoms");

        // Search by dependences
        examples.searchDependences("demonstrate::nsubj::methods methods::amod::microscopic consider::nsubjpass::observation");

        // Search by heads
        examples.searchHead("diabetes");

        // Retrieve depencendes vector by docid
        examples.prettyPrintDependencesVector("19196247");


    }

    private void initialize() throws IOException {

        this.directory = new SimpleFSDirectory(new File(this.lucenePath));


        this.indexReader = IndexReader.open(directory);
        this.indexSearcher = new IndexSearcher(indexReader);

        this.parserDOCID = new QueryParser(Version.LUCENE_36, LuceneStore.FIELD_DOCID, new KeywordAnalyzer());
        this.parserTEXT = new QueryParser(Version.LUCENE_36, LuceneStore.FIELD_TEXT, new ClassicAnalyzer(Version.LUCENE_36));
        this.parserDEPENDENCES = new QueryParser(Version.LUCENE_36, LuceneStore.FIELD_DEPENDENCES, new WhitespaceAnalyzer(Version.LUCENE_35));
        this.parserHEADS = new QueryParser(Version.LUCENE_36, LuceneStore.FIELD_HEADS, new WhitespaceAnalyzer(Version.LUCENE_35));
    }

    private void retrieveDocument(String docid) {
        System.out.println("RETRIEVE DOCID " + docid);
        try {
            Query query = parserDOCID.parse(docid);
            TopDocs hits = this.indexSearcher.search(query, 1);
            if (hits.totalHits == 1) {
                ScoreDoc d = hits.scoreDocs[0];

                org.apache.lucene.document.Document dd = indexReader.document(d.doc);
                System.out.println("DOCID: " + dd.get(LuceneStore.FIELD_DOCID));
                System.out.println("LEMMAS: " + retrieveLemmas(d.doc));
                System.out.println("ENTITIES: " + retrieveEntities(d.doc));
                System.out.println("DEPENDENCES: " + retrieveDependences(d.doc));
            }
        } catch (Exception ex) {
            Logger.getLogger(LuceneExamples.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void searchText(String text) {
        System.out.println("SEARCH TEXT " + text);

        try {
            Query query = parserTEXT.parse(text);
            TopDocs hits = this.indexSearcher.search(query, 1000);
            System.out.println("\tfound " + hits.totalHits + " hits");
            for (ScoreDoc d : hits.scoreDocs) {
                org.apache.lucene.document.Document dd = indexReader.document(d.doc);
                System.out.println("\tDOCID: " + dd.get(LuceneStore.FIELD_DOCID));
            }
        } catch (Exception ex) {
            Logger.getLogger(LuceneExamples.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void searchDependences(String dependences) {
        System.out.println("SEARCH DEPENDENCES " + dependences);

        try {
            // Escape :: chars in dependences
            dependences = dependences.replace(":", "\\:");

            Query query = parserDEPENDENCES.parse(dependences);
            TopDocs hits = this.indexSearcher.search(query, 1000);
            System.out.println("\tfound " + hits.totalHits + " hits");

            for (ScoreDoc d : hits.scoreDocs) {
                org.apache.lucene.document.Document dd = indexReader.document(d.doc);
                System.out.println("\tDOCID: " + dd.get(LuceneStore.FIELD_DOCID));
            }
        } catch (Exception ex) {
            Logger.getLogger(LuceneExamples.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void searchHead(String heads) {
        System.out.println("SEARCH HEADS " + heads);

        try {
            Query query = parserHEADS.parse(heads);
            TopDocs hits = this.indexSearcher.search(query, 1000);
            System.out.println("\tfound " + hits.totalHits + " hits");

            for (ScoreDoc d : hits.scoreDocs) {
                org.apache.lucene.document.Document dd = indexReader.document(d.doc);
                System.out.println("\tDOCID: " + dd.get(LuceneStore.FIELD_DOCID));
            }
        } catch (Exception ex) {
            Logger.getLogger(LuceneExamples.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String retrieveLemmas(int docNumber) {
        return retrieveVector(docNumber, LuceneStore.FIELD_LEMMAS);
    }

    private String retrieveEntities(int docNumber) {
        return retrieveVector(docNumber, LuceneStore.FIELD_ENTITIES);
    }

    private String retrieveDependences(int docNumber) {
        return retrieveVector(docNumber, LuceneStore.FIELD_DEPENDENCES);
    }

    private String retrieveVector(int docNumber, String field) {
        try {
            StringBuilder builder = new StringBuilder();

            TermFreqVector vector = indexReader.getTermFreqVector(docNumber, field);
            for (int i = 0; i < vector.size(); i++) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(vector.getTerms()[i]);
                builder.append(" [");
                builder.append(vector.getTermFrequencies()[i]);
                builder.append("]");
            }
            return builder.toString();
        } catch (IOException ex) {
            Logger.getLogger(LuceneExamples.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void prettyPrintDependencesVector(String docid) {

        System.out.println("RETRIEVE DEPENDENCES VECTOR FOR " + docid);
        try {
            Query query = parserDOCID.parse(docid);
            TopDocs hits = this.indexSearcher.search(query, 1);
            if (hits.totalHits == 1) {
                ScoreDoc d = hits.scoreDocs[0];


                TermFreqVector vector = indexReader.getTermFreqVector(d.doc, LuceneStore.FIELD_DEPENDENCES);
                for (int i = 0; i < vector.size(); i++) {
                    StringBuilder builder = new StringBuilder();

                    String[] parts = vector.getTerms()[i].split("::");
                    builder.append(parts[1]);  // Relation
                    builder.append("(");
                    builder.append(parts[0]);  // Head
                    builder.append(",");
                    builder.append(parts[2]);  // Modifier
                    builder.append(")");
                    builder.append(" [");
                    builder.append(vector.getTermFrequencies()[i]);
                    builder.append("]");
                    
                    System.out.println("\t"+builder.toString());
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(LuceneExamples.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
