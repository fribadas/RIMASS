package es.uvigo.rimass.thesaurus.neo4j;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.neo4j.unsafe.batchinsert.LuceneBatchInserterIndexProvider;

import es.uvigo.rimass.collection.store.neo4j.entities.FamilyRelation;
import es.uvigo.rimass.collection.store.neo4j.entities.RelatedTermRelation;
import es.uvigo.rimass.collection.store.neo4j.entities.TermNode;
import es.uvigo.rimass.thesaurus.Descriptor;
import es.uvigo.rimass.thesaurus.Root;
import es.uvigo.rimass.thesaurus.Thesaurus;

public class ThesaurusLoader {

	private static final String NEO4JEMBEDDED_GRAPH_DB = "/home/adrian/tmp/medline/store/neo4jembedded/graph.db";
	private static final String THESAURUS_MESH_XML_LOCATION = "/home/adrian/tmp/thesaurus.MESH.xml";

	private Thesaurus thesaurus;

	BatchInserter inserter;

	BatchInserterIndexProvider indexProvider;
	
	BatchInserterIndex termIndex;
	BatchInserterIndex typeIndex;
	BatchInserterIndex relationIndex;

	public ThesaurusLoader(String file) {
		thesaurus = new Thesaurus(file);
		inserter = BatchInserters.inserter(
				NEO4JEMBEDDED_GRAPH_DB);

		indexProvider = new LuceneBatchInserterIndexProvider(
				inserter);
		
		termIndex = createTermIndex(indexProvider);
		typeIndex = createTypeIndex(indexProvider);
		relationIndex = createRelationIndex(indexProvider);
	}
	
	private BatchInserterIndex createTermIndex(
			BatchInserterIndexProvider indexProvider) {
		Map<String, String> indexProps = new HashMap<String, String>();
		indexProps = new HashMap<String, String>();
		indexProps.put("type", "exact");
		indexProps.put("provider", "lucene");
		return indexProvider.nodeIndex("termIndex", indexProps);
	}

	private BatchInserterIndex createRelationIndex(
			BatchInserterIndexProvider indexProvider) {
		Map<String, String> indexProps = new HashMap<String, String>();
		indexProps = new HashMap<String, String>();
		indexProps.put("type", "exact");
		indexProps.put("provider", "lucene");
		return indexProvider.nodeIndex("__rel_types__", indexProps);
	}

	private BatchInserterIndex createTypeIndex(
			BatchInserterIndexProvider indexProvider) {
		Map<String, String> indexProps = new HashMap<String, String>();
		indexProps = new HashMap<String, String>();
		indexProps.put("type", "exact");
		indexProps.put("provider", "lucene");
		return indexProvider.nodeIndex("__types__", indexProps);
	}

	public static void main(String[] args) {
		ThesaurusLoader loader = new ThesaurusLoader(
				THESAURUS_MESH_XML_LOCATION);
		loader.load();
	}

	public void load() {

		try {
			for (Root d : thesaurus.getRoots()) {
				buildAndSaveTree(null, d.getDescriptor());
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			termIndex.flush();
			typeIndex.flush();
			relationIndex.flush();
			indexProvider.shutdown();
			inserter.shutdown();
		}
	}

	private void buildAndSaveTree(Long parent, Descriptor d) {
		IndexHits<Long> find = termIndex.get("label", d.getLabel());

		Long id = find.getSingle();

		if (id == null) {
			id = createTermNode(d.getLabel());
		}

		if (d.getTerms() != null) {
			for (String related : d.getTerms()) {
				if (related.equals(d.getLabel()))
					continue;
				Long newNode = createTermNode(related);
				createRelationship(id, newNode, new SiblingRelationshipType(), RelatedTermRelation.class);
			}
		}

		if (parent != null) {
			createRelationship(parent, id, new ParentRelationshipType(), FamilyRelation.class);
		}

		if (d.getDescendants() != null && d.getDescendants().size() > 0) {
			for (Descriptor desc : d.getDescendants()) {
				if (!desc.getLabel().equals(d.getLabel())) {
					buildAndSaveTree(id, desc);
				}
			}
		}
	}

	private void createRelationship(Long id, Long newNode, RelationshipType relType, Class<?> clazz) {
		long idRel = inserter.createRelationship(id.longValue(),
				newNode.longValue(), relType,
				MapUtil.map("__type__", clazz.getName()));
		relationIndex.add(idRel, createMapIndexRelationProperties(idRel, clazz));
	}

	private Map<String, Object> createMapIndexRelationProperties(long idRel, Class<?> clazz) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("className", clazz.getName());
		return map;
	}

	private Long createTermNode(String d) {
		long id = inserter.createNode(createMapNodeProperties(d));
		termIndex.add(id, createMapIndexTermProperties(d));
		typeIndex.add(id, createMapIndexTypeProperties(d));
		return id;
	}

	private Map<String, Object> createMapNodeProperties(String d) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("label", d);
		map.put("isThesaurusEntity", Boolean.TRUE);
		map.put("__type__", TermNode.class.getName());
		return map;
	}

	private Map<String, Object> createMapIndexTypeProperties(String d) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("className", TermNode.class.getName());
		return map;
	}
	
	private Map<String, Object> createMapIndexTermProperties(String d) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("label", d);
		return map;
	}
}
