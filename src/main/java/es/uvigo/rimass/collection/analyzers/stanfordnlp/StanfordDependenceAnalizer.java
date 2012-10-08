package es.uvigo.rimass.collection.analyzers.stanfordnlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import es.uvigo.rimass.collection.Collection;
import es.uvigo.rimass.collection.*;
import es.uvigo.rimass.collection.loaders.medline.MedlineJournal;
import es.uvigo.rimass.collection.loaders.medline.MedlineMetadata;
import es.uvigo.rimass.collection.store.xml.XMLStore;
import es.uvigo.rimass.core.*;
import es.uvigo.rimass.thesaurus.Descriptor;
import es.uvigo.rimass.thesaurus.Thesaurus;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ribadas
 */
public class StanfordDependenceAnalizer implements Analyzer {
    
    private boolean useThesaurusEntities;
    private boolean expandThesaurusParents;
    private Thesaurus thesaurus;
    private Map<String, Map> entitiesLookup;
    private File auxiliarDir;
    private StanfordCoreNLP pipeline;
    
    public StanfordDependenceAnalizer() {
        this.useThesaurusEntities = false;
        this.expandThesaurusParents = false;
    }
    
    public StanfordDependenceAnalizer(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
        this.useThesaurusEntities = true;
        this.expandThesaurusParents = true;
    }
    
    public void setExpandThesaurusParents(boolean expandThesaurusParents) {
        this.expandThesaurusParents = expandThesaurusParents;
    }
    
    public void setUseThesaurusEntities(boolean useThesaurusEntities) {
        this.useThesaurusEntities = useThesaurusEntities;
    }
    
    public void initialize(Collection collection, Configuration configuration) {
        if (thesaurus != null) {
            this.useThesaurusEntities = true;
            this.expandThesaurusParents = true;
            
            this.auxiliarDir = new File(configuration.getProperty("base_dir") + File.separator + collection.getName() + File.separator + "auxiliar");
            createThesaurusEntitiesRules(thesaurus);
        } else {
            this.useThesaurusEntities = false;
            this.expandThesaurusParents = false;
        }

        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
//        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner, parse, dcoref");
        if (this.useThesaurusEntities) {
            props.put("annotators", "tokenize, ssplit, pos,  lemma, regexner, parse");
            props.put("tokenize.whitespace", "true");
            props.put("regexner.mapping", this.auxiliarDir.getAbsolutePath() + File.separator + "rules.regexpner");
            props.put("regexner.ignorecase", "true");
        } else {
            props.put("annotators", "tokenize, ssplit, pos,  lemma, parse");
        }
        this.pipeline = new StanfordCoreNLP(props);
        
    }
    
    public Document<? extends Metadata> analizeDocument(Document<? extends Metadata> document) {
        String text = document.getText();
        
        HashSet<String> thesaurusDescriptors_lookup = new HashSet<String>();
        List<String> entities = new ArrayList<String>();
        HashSet<String> lemmas_lookup = new HashSet<String>();
        List<String> lemmas = new ArrayList<String>();
        List<Dependence> dependences = new ArrayList<Dependence>();
        
        String parts[] = text.split("\\. ");
        
        for (String part : parts) {
            if (this.useThesaurusEntities) {
                part = prefilterThesaurusEntities(part + ".");
            }
            
            Annotation annotation = new Annotation(part + ".");
            pipeline.annotate(annotation);
            
            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            
            for (CoreMap sentence : sentences) {
                // Extract tokens
                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    String word = cleanTokenPunctuation(token.get(CoreAnnotations.TextAnnotation.class));
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                    
//System.out.println("TOKEN "+word+"   ("+ne+")");
                    
                    String lemma = cleanTokenPunctuation(token.get(CoreAnnotations.LemmaAnnotation.class));

                    // Filter content words (noums, verbs, adjetives, adverbs)
                    if (pos.startsWith("N") || pos.startsWith("V") || pos.startsWith("J") || pos.startsWith("R")) {
                        lemmas.add(lemma);
//System.out.println("   ADD lema "+lemma);
                        lemmas_lookup.add(lemma);
                        if (!ne.equals("O")) {
                            entities.add(word);
//System.out.println("   ADD entity "+word);
                            if (this.useThesaurusEntities && ne.equals("THESAURUS")) {
                                thesaurusDescriptors_lookup.add(word);
                                                        
                                if (this.expandThesaurusParents) {
                                    for (String parentLabel : generalizeThesaurusLabel(thesaurus, word)) {
                                        entities.add(parentLabel);
//System.out.println("   ADD entity parent "+parentLabel);
                                    }
                                }
                            }
                        }
                    }
                }

                // Extract dependencies
                SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
                
                for (SemanticGraphEdge link : dependencies.edgeIterable()) {
                    String gov = cleanTokenPunctuation(link.getGovernor().lemma());
                    String rel = link.getRelation().toString();
                    String dep = cleanTokenPunctuation(link.getDependent().lemma());
                    
                    if (lemmas.contains(gov) && lemmas.contains(dep)) {
                        if (this.expandThesaurusParents && (thesaurusDescriptors_lookup.contains(gov) || thesaurusDescriptors_lookup.contains(dep))) {
                            // Si alguno es THESAURUS -> meter padre (replica)

                            List<String> govVariants = generalizeThesaurusLabel(thesaurus, gov);
                            List<String> depVariants = generalizeThesaurusLabel(thesaurus, dep);
                            govVariants.add(gov);
                            depVariants.add(dep);
                            for (String govV : govVariants) {
                                for (String depV : depVariants) {
                                    dependences.add(new Dependence(rel, govV, depV));
                                }
                            }
                            
                        } else {
                            dependences.add(new Dependence(rel, gov, dep));
                        }
                    }
                    
                }
                
            }
            
        }
        
        SingleTermsRepresentation lemmasRepresentation = new SingleTermsRepresentation(lemmas);
        lemmasRepresentation.setType("lemmas");
        
        SingleTermsRepresentation entitiesRepresentation = new SingleTermsRepresentation(entities);
        entitiesRepresentation.setType("entities");
        
        DependencesRepresentation dependencesRepresentation = new DependencesRepresentation(dependences);
        
        
        document.addRepresentation(lemmasRepresentation);
        document.addRepresentation(entitiesRepresentation);
        document.addRepresentation(dependencesRepresentation);
        
        return document;
    }
    
    private String cleanTokenPunctuation(String token) {
        String lookupToken;
        if (endsWithPunctuation(token)) {
            lookupToken = token.substring(0, token.length() - 1);
        } else {
            lookupToken = token;
        }
        return lookupToken;
    }
    
    private void createThesaurusEntitiesRules(Thesaurus thesaurus) {
        
        if (!this.auxiliarDir.exists()) {
            auxiliarDir.mkdirs();
        }
        
        try {
            PrintStream out = new PrintStream(new File(auxiliarDir, "rules.regexpner"));
            
            
            Set<String> processedEntities = new HashSet<String>();
            
            entitiesLookup = new HashMap<String, Map>();
            for (Descriptor d : thesaurus.getDescriptors()) {
//                String label = d.getLabel();
//                if (label.lastIndexOf(",") != -1) {
//                    label = label.substring(0, label.lastIndexOf(",")); // Cut at ',', a least in MESH 
//                }    

                String[] parts = d.getLabel().split(",");
                String label = parts[0];
                if (label.matches(".*(\\(|\\)|\\[|\\]|\\.|\\,|;|:).*")) {
                    label = "";
                }
                
                String[] tokens = label.split(" ");
                if (tokens[0].length() > 1) {  // Skip digits and single letters
                    StringBuilder rule = new StringBuilder();
                    Map<String, Map> currentNode = entitiesLookup;
                    for (int currentToken = 0; currentToken < tokens.length; currentToken++) {
                        if (rule.length() > 0) {
                            rule.append("_");
                        }
                        rule.append(tokens[currentToken]);
                        if (currentNode.containsKey(tokens[currentToken])) {
                            currentNode = currentNode.get(tokens[currentToken]);
                        } else {
                            Map<String, Map> newNode = new HashMap<String, Map>();
                            currentNode.put(tokens[currentToken], newNode);
                            currentNode = newNode;
                        }
                    }
                    String entity = rule.toString();
                    if (!processedEntities.contains(entity)) {
                        rule.append("\tTHESAURUS");
                        out.println(rule.toString());
                        processedEntities.add(entity);
                    }
                }
            }
            out.close();
        } catch (Exception e) {
            Logger.getLogger(StanfordDependenceAnalizer.class.getName()).log(Level.SEVERE, "error creating NER rules from thesaurus at " + this.auxiliarDir, e);
        }
        
    }
    
    public String prefilterThesaurusEntities(String text) {
        String[] tokens = text.split(" |\t|\n");
        
        StringBuilder newText = new StringBuilder();
        int i = 0;
        while (i < tokens.length) {
            String token = tokens[i];
            String lookupToken = cleanTokenPunctuation(token);
            
            if (entitiesLookup.containsKey(lookupToken)) {
                StringBuilder newToken = new StringBuilder();
                newToken.append(token);
                Map<String, Map> currentNode = entitiesLookup.get(lookupToken);
                i++;
                while ((currentNode != null) && (i < tokens.length)) {
                    token = tokens[i];
                    lookupToken = cleanTokenPunctuation(token);
                    
                    if (currentNode.containsKey(lookupToken)) {
                        newToken.append('_');
                        newToken.append(token);
                        currentNode = currentNode.get(lookupToken);
                        i++;
                    } else {
                        currentNode = null;  // Exit loop, mantaining current position (i)
                    }
                }
                newText.append(newToken.toString());
                newText.append(" ");
            } else {
                newText.append(token);
                newText.append(" ");
                i++;
            }
        }
        
        return newText.toString();
    }
    
    private boolean endsWithPunctuation(String token) {
        return (token.charAt(token.length() - 1) == '.')
                || (token.charAt(token.length() - 1) == ',')
                || (token.charAt(token.length() - 1) == ';')
                || (token.charAt(token.length() - 1) == ':')
                || (token.charAt(token.length() - 1) == '!')
                || (token.charAt(token.length() - 1) == '?');
    }
    
    public static void main(String[] args) {
        Thesaurus t = new Thesaurus("/home/ribadas/tmp/CJT/thesaurus.MESH.xml");
        
        StanfordDependenceAnalizer s = new StanfordDependenceAnalizer(t);
        s.initialize(new Collection("prueba", new Language("english", "es")), new Configuration());
        
        
        String text = "Prepulse_inhibition is a car, Epidermodysplasia Verruciformis  Musculoskeletal and Neural Physiological Phenomena the Gram-Negative Chemolithotrophic Bacteria suppression Gram-Negative Facultatively Anaerobic Rods of the Stanford startle@reflex Glycogen Storage Disease startle_reflex startle-reflex startle^reflex startle&reflex startle=reflex startle+reflex startle--reflex by the Japan_Evangelical_Lutheran_Church, "
                + "can be top-down modulated in both humans and rats. This study_investigated whether emotional-learning-induced ";
        
        text = "Usher syndromeIb (USH1B), an autosomal recessive disorder caused by mutations in myosin VIIa (MYO7A), is characterized by congenital profound hearing loss, vestibular abnormalities and retinitis pigmentosa. Promoter elements in the 5 kb upstream of the translation start were identified using adult retinal pigment epithelium cells (ARPE-19) as a model system. A 160 bp minimal promoter within the first intron was active in ARPE-19 cells, but not in HeLa cells that do not express MYO7A. A 100 bp sequence, 5' of the first exon, and repeated with 90% homology within the first intron, appeared to modulate expression in both cell lines. Segments containing these elements were screened by heteroduplex analysis. No heteroduplexes were detected in the minimal promoter, suggesting that this sequence is conserved. A -2568 A>T transversion in the 5' 100 bp repeat, eliminating a CCAAT element, was found only in USH1B patients. However, in all 5 families, -2568 A>T was in cis with the same missense mutation in the myosin VIIa tail (Arg1240Gln), and 4 of the 5 families were Dutch. These observations suggest either 1) linkage disequilibrium or 2)that a combination of a promoter mutation with a less active myosin VIIa protein results in USH1B.";
        text = "Usher syndromeIb (USH1B), an autosomal recessive disorder caused by mutations in myosin VIIa (MYO7A), is characterized by congenital profound hearing loss, vestibular abnormalities and retinitis pigmentosa.";
        
        text = "The nirIX gene cluster of Paracoccus denitrificans is located between the nir and nor gene clusters encoding nitrite and nitric oxide reductases respectively. The NirI sequence corresponds to that of a membrane-bound protein with six transmembrane helices, a large periplasmic domain and cysteine-rich cytoplasmic domains that resemble the binding sites of [4Fe-4S] clusters in many ferredoxin-like proteins. NirX is soluble and apparently located in the periplasm, as judged by the predicted signal sequence. NirI and NirX are homologues of NosR and NosX, proteins involved in regulation of the expression of the nos gene cluster encoding nitrous oxide reductase in Pseudomonas stutzeri and Sinorhizobium meliloti.";
        
        text = "The nirIX gene cluster of Paracoccus_denitrificans is located between the nir and nor gene clusters encoding nitrite and nitric oxide reductases respectively  The NirI sequence corresponds to that of a membrane-bound protein with six transmembrane helices  a large periplasmic domain and cysteine-rich cytoplasmic domains that resemble the binding sites of [4Fe-4S] clusters in many ferredoxin-like proteins  NirX is soluble and apparently located in the periplasm  as judged by the predicted signal sequence  NirI and NirX are homologues of NosR and NosX  proteins involved in regulation of the expression of the nos gene cluster encoding nitrous oxide reductase in Pseudomonas_stutzeri and Sinorhizobium_meliloti  Analysis_of a NirI-deficient mutant strain revealed that NirI is involved in transcription activation of the nir gene cluster in response to oxygen limitation and the presence of N-oxides  The NirX-deficient mutant transiently accumulated nitrite in the growth medium  but it had a final growth yield similar to that of the wild type  Transcription of the nirIX gene cluster itself was controlled by NNR  a member of the family of FNR-like transcriptional activators  An NNR binding sequence is located in the middle of the intergenic region between the nirI and nirS genes with its centre located at position -41 5 relative to the transcription start sites of both genes  Attempts to complement the NirI mutation via cloning of the nirIX gene cluster on a broad-host-range vector were unsuccessful  the ability to express nitrite reductase being restored only when the nirIX gene cluster was reintegrated into the chromosome of the NirI-deficient mutant via homologous recombination in such a way that the wild-type nirI gene was present directly upstream of the nir operon .";
        
        Document d = new Document(1, "pepe", text, null);
        
        System.out.println(">>> " + s.prefilterThesaurusEntities(text));
        
        
        MedlineMetadata metadata = new MedlineMetadata();
        metadata.setAuthors(Arrays.asList("pepe", "luis"));
        metadata.setDate(new Date());
        metadata.setJournal(new MedlineJournal("aaaa", "aaa", "ssss", "sssss", "sssss", "ss"));
        metadata.setKeyWords(Arrays.asList("fffffff", "ggggg"));
        metadata.setMeshDescriptors(Arrays.asList("dddddddd", "jjjjjjjjj", "mmmmmmmmmmmmmmmmmmm"));
        
        
        Document d2 = s.analizeDocument(d);
        d2.setMetadata(metadata);
        
        Store store = new XMLStore();
        store.initialize(new Collection("prueba", new Language("english", "es")), new Configuration());
        store.storeDocument(d2);
        
        Document d3 = store.retrieveDocument(1);
        
        d3.setDocid(1000);
        store.storeDocument(d3);
    }
    
    private List<String> generalizeThesaurusLabel(Thesaurus thesaurus, String label) {
        List<String> result = new ArrayList<String>();
        
        Set<Descriptor> descriptors = thesaurus.findDescriptorsByLabel(label);
        if (descriptors != null) {
            for (Descriptor d : descriptors) {
                Set<Descriptor> ascendants = d.getAscendants();
                if (ascendants != null) {
                    for (Descriptor parent : ascendants) {
                        result.add(parent.getLabel().toLowerCase().replaceAll(" ", "_"));
                    }
                }
            }
        }
        return result;
    }
}
