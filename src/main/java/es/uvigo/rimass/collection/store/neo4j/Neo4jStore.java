package es.uvigo.rimass.collection.store.neo4j;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Transaction;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import es.uvigo.rimass.collection.Collection;
import es.uvigo.rimass.collection.Configuration;
import es.uvigo.rimass.collection.Store;
import es.uvigo.rimass.collection.store.neo4j.entities.DependenceNode;
import es.uvigo.rimass.collection.store.neo4j.entities.DocumentDependenceRelation;
import es.uvigo.rimass.collection.store.neo4j.entities.DocumentNode;
import es.uvigo.rimass.collection.store.neo4j.entities.RelationNode;
import es.uvigo.rimass.collection.store.neo4j.entities.TermNode;
import es.uvigo.rimass.collection.store.neo4j.repositories.DependenceNodeRepository;
import es.uvigo.rimass.collection.store.neo4j.repositories.RelationNodeRepository;
import es.uvigo.rimass.collection.store.neo4j.repositories.TermNodeRepository;
import es.uvigo.rimass.core.Dependence;
import es.uvigo.rimass.core.DependencesRepresentation;
import es.uvigo.rimass.core.Document;
import es.uvigo.rimass.core.Metadata;
import es.uvigo.rimass.core.Representation;
import es.uvigo.rimass.core.SingleTermsRepresentation;
import es.uvigo.rimass.thesaurus.Thesaurus;

/**
 * 
 * @author ribadas
 */
public class Neo4jStore implements Store {

	private Neo4jTemplate template;
	private TermNodeRepository termRepo;
	private DependenceNodeRepository depRepo;
	private RelationNodeRepository relRepo;
	private Thesaurus thesaurus;

	public Neo4jStore() {
	}
	
	public void initialize(Collection collection, Configuration configuration) {

		ApplicationContext context = new ClassPathXmlApplicationContext(
				"es/uvigo/rimass/collection/store/neo4j/applicationContext.xml");
		template = context.getBean(Neo4jTemplate.class);

		thesaurus = new Thesaurus(configuration.getProperty("base_dir")
				+ "/thesaurus.MESH.xml");

		termRepo = context.getBean(TermNodeRepository.class);

		depRepo = context.getBean(DependenceNodeRepository.class);
		
		relRepo = context.getBean(RelationNodeRepository.class);
		
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
					dependences = ((DependencesRepresentation) r)
							.getDependences();
				}
			}
		}

		Transaction tx = template.getGraphDatabaseService().beginTx();
		try {
			DocumentNode doc = new DocumentNode();

			doc.setTitle(document.getTitle());
			doc.setText(document.getText());
			
			DocumentNode saved = template.save(doc);
			buildDependencies(saved, dependences);
			

			tx.success();
		} catch (Throwable t) {
			tx.failure();
			t.printStackTrace();
		} finally {
			tx.finish();
		}
	}

	private void buildDependencies(DocumentNode document,
			List<Dependence> dependences) {
		
		Set<DocumentDependenceRelation> nodesToRet = new HashSet<DocumentDependenceRelation>();

		for (Dependence dep : dependences) {
			System.out.println(dep.getHead() + "::" + dep.getRelation() + "::"
					+ dep.getModifier());
			Iterable<DependenceNode> depNodes = depRepo.findDependence(
					dep.getHead(), dep.getRelation(), dep.getModifier());

			Iterator<DependenceNode> iterator = depNodes.iterator();
			DependenceNode depNode = null;
			if (iterator.hasNext())
				depNode = iterator.next();

			if (depNode == null) {
				TermNode headNode = getNodeIfExists(dep.getHead());
				if (headNode == null) {
					headNode = new TermNode();
					headNode.setLabel(dep.getHead());
					headNode = template.save(headNode);
				}

				TermNode modifierNode = getNodeIfExists(dep.getModifier());
				if (modifierNode == null) {
					modifierNode = new TermNode();
					modifierNode.setLabel(dep.getModifier());
					modifierNode = template.save(modifierNode);
				}
				
				RelationNode relNode = getRelationIfExists(dep.getRelation());
				if (relNode == null){
					relNode = new RelationNode();
					relNode.setLabel(dep.getRelation());
					relNode = template.save(relNode);
				}
				
				
				depNode = new DependenceNode();
				depNode.setHead(headNode);
				depNode.setModifier(modifierNode);
				depNode.setRelation(relNode);
				template.save(depNode);
			}
			
			Iterator<DocumentDependenceRelation> iteratorDocDep = depRepo.findDocumentDependencyRelation(document.getId(), depNode.getId()).iterator();
			
			DocumentDependenceRelation relation = null;
			if (iteratorDocDep.hasNext()){
				relation = iteratorDocDep.next();
			}
			
			if (relation == null){
				relation = new DocumentDependenceRelation();
				relation.setDependence(depNode);
				relation.setDocument(document);
				relation.setOccurences(0);
			}
			
			relation.setOccurences(relation.getOccurences()+1);
			
			nodesToRet.add(relation);
		}
		document.setDependences(nodesToRet);
		template.save(document);
	}

	private RelationNode getRelationIfExists(String relation) {
		Iterable<RelationNode> headNodes = relRepo.findByLabel(relation);
		Iterator<RelationNode> iterator = headNodes.iterator();
		if (iterator.hasNext())
			return iterator.next();
		else
			return null;
	}

	private TermNode getNodeIfExists(String head) {
		Iterable<TermNode> headNodes = termRepo.findByLabel(head);
		Iterator<TermNode> iterator = headNodes.iterator();
		if (iterator.hasNext())
			return iterator.next();
		else
			return null;
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

	private String dependencePartsToString(List<Dependence> dependences,
			boolean selectHeads) {
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
