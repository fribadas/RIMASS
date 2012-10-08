package es.uvigo.rimass.collection.analyzers.stanfordnlp;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import es.uvigo.rimass.collection.Analyzer;
import es.uvigo.rimass.collection.Collection;
import es.uvigo.rimass.collection.Configuration;
import es.uvigo.rimass.core.Document;
import es.uvigo.rimass.core.Metadata;
import es.uvigo.rimass.core.TreeStringRepresentation;
import es.uvigo.rimass.core.tree.TreeRepresentation;
import es.uvigo.rimass.thesaurus.Descriptor;
import es.uvigo.rimass.thesaurus.Thesaurus;

/**
 * 
 * @author ribadas
 */
public class StanfordSingleAnalizer implements Analyzer {

	private StanfordCoreNLP pipeline;
	private Thesaurus thesaurus;
	private File auxiliarDir;
	private Map<String, Map> entitiesLookup;

	public StanfordSingleAnalizer(Thesaurus thesaurus) {
		super();
		this.thesaurus = thesaurus;
	}

	public void initialize(Collection collection, Configuration configuration) {
		this.auxiliarDir = new File(configuration.getProperty("base_dir")
				+ File.separator + collection.getName() + File.separator
				+ "auxiliar");

		createThesaurusEntitiesRules(thesaurus);

		Properties props = new Properties();
		props.put("annotators",
				"tokenize, ssplit, pos,  lemma, regexner, parse");
		props.put("tokenize.whitespace", "true");
		props.put("regexner.mapping", this.auxiliarDir.getAbsolutePath()
				+ File.separator + "rules.regexpner");
		props.put("regexner.ignorecase", "true");

		this.pipeline = new StanfordCoreNLP(props);
	}

	public Document<? extends Metadata> analizeDocument(
			Document<? extends Metadata> document) {
		String parts[] = document.getText().split("\\. ");
		
        HashSet<String> thesaurusDescriptors_lookup = new HashSet<String>();

		TreeStringRepresentation representation = new TreeStringRepresentation();

		for (String part : parts) {
			part = prefilterThesaurusEntities(part.trim() + ".");

			Annotation annotation = new Annotation(part + ".");
			pipeline.annotate(annotation);

			List<CoreMap> sentences = annotation
					.get(CoreAnnotations.SentencesAnnotation.class);

			for (CoreMap sentence : sentences) {
				// Extract tokens
				for (CoreLabel token : sentence
						.get(CoreAnnotations.TokensAnnotation.class)) {
					String word = cleanTokenPunctuation(token
							.get(CoreAnnotations.TextAnnotation.class));
					String pos = token
							.get(CoreAnnotations.PartOfSpeechAnnotation.class);
					String ne = token
							.get(CoreAnnotations.NamedEntityTagAnnotation.class);

					// System.out.println("TOKEN "+word+"   ("+ne+")");

					String lemma = cleanTokenPunctuation(token
							.get(CoreAnnotations.LemmaAnnotation.class));

					// Filter content words (noums, verbs, adjetives, adverbs)
					if (pos.startsWith("N") || pos.startsWith("V")
							|| pos.startsWith("J") || pos.startsWith("R")
							|| pos.startsWith("W") || pos.startsWith("P")) {
						// System.out.println("   ADD lema "+lemma);
						//lemmas_lookup.add(lemma);
						if (!ne.equals("O")) {
							// System.out.println("   ADD entity "+word);
							if (ne.equals("THESAURUS")) {
								thesaurusDescriptors_lookup.add(word);
							}
						}
					}
				}

				// Extract dependencies

				SemanticGraph dependencies = sentence
						.get(CollapsedCCProcessedDependenciesAnnotation.class);

				for (IndexedWord word : dependencies.getRoots()) {
					TreeRepresentation tree = new TreeRepresentation();
					buildTree(dependencies, tree, word, null, null, thesaurusDescriptors_lookup);
					representation.addTree(tree);
				}

				// for (SemanticGraphEdge link : dependencies.edgeIterable()) {
				// String gov = cleanTokenPunctuation(link.getGovernor()
				// .lemma());
				// String rel = link.getRelation().toString();
				// String dep = cleanTokenPunctuation(link.getDependent()
				// .lemma());
				//
				// // if (lemmas.contains(gov) && lemmas.contains(dep)) {
				// // dependences.add(new Dependence(rel, gov, dep));
				// // }
				//
				// }

			}

		}

		document.addRepresentation(representation);

		return document;
	}

	private void buildTree(SemanticGraph dependencies, TreeRepresentation tree,
			IndexedWord current, IndexedWord parent,
			GrammaticalRelation relation, HashSet<String> thesaurus_lookup) {
		String lemma = cleanTokenPunctuation(current.lemma());

		String type = current.get(PartOfSpeechAnnotation.class);
		boolean toPersist = false;

		if (type.startsWith("N") || type.startsWith("V")
				|| type.startsWith("J") || type.startsWith("R")
				|| type.startsWith("W") || type.startsWith("P")) {

			toPersist = true;
			
			String toIns = lemma;
			if (thesaurus_lookup.contains(current.word())) toIns = current.word();
			
			if (parent == null) {
				tree.setRoot(new Long(current.index()), toIns);
			} else {
				tree.addNode(new Long(parent.index()),
						new Long(current.index()),
						cleanTokenPunctuation(relation.getShortName()), toIns);
			}
		}

		// Build children
		for (SemanticGraphEdge edge : dependencies.getOutEdgesSorted(current)) {
			buildTree(dependencies, tree, edge.getDependent(),
					toPersist ? current : parent, edge.getRelation(), thesaurus_lookup);
		}
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

	private boolean endsWithPunctuation(String token) {
		return (token.charAt(token.length() - 1) == '.')
				|| (token.charAt(token.length() - 1) == ',')
				|| (token.charAt(token.length() - 1) == ';')
				|| (token.charAt(token.length() - 1) == ':')
				|| (token.charAt(token.length() - 1) == '!')
				|| (token.charAt(token.length() - 1) == '?');
	}

	private void createThesaurusEntitiesRules(Thesaurus thesaurus) {

		if (!this.auxiliarDir.exists()) {
			auxiliarDir.mkdirs();
		}

		try {
			PrintStream out = new PrintStream(new File(auxiliarDir,
					"rules.regexpner"));

			Set<String> processedEntities = new HashSet<String>();

			entitiesLookup = new HashMap<String, Map>();
			for (Descriptor d : thesaurus.getDescriptors()) {
				// String label = d.getLabel();
				// if (label.lastIndexOf(",") != -1) {
				// label = label.substring(0, label.lastIndexOf(",")); // Cut at
				// ',', a least in MESH
				// }

				String[] parts = d.getLabel().split(",");
				String label = parts[0];
				if (label.matches(".*(\\(|\\)|\\[|\\]|\\.|\\,|;|:).*")) {
					label = "";
				}

				String[] tokens = label.split(" ");
				if (tokens[0].length() > 1) { // Skip digits and single letters
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
			Logger.getLogger(StanfordDependenceAnalizer.class.getName()).log(
					Level.SEVERE,
					"error creating NER rules from thesaurus at "
							+ this.auxiliarDir, e);
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
						currentNode = null; // Exit loop, mantaining current
											// position (i)
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
}
