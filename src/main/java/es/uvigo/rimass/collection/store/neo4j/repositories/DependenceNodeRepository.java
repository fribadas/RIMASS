package es.uvigo.rimass.collection.store.neo4j.repositories;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import es.uvigo.rimass.collection.store.neo4j.entities.DependenceNode;
import es.uvigo.rimass.collection.store.neo4j.entities.DocumentDependenceRelation;

@Repository
public interface DependenceNodeRepository extends GraphRepository<DependenceNode>{

	@Query("START term1=node:termIndex(label={0}), term2=node:termIndex(label={2}), rel=node:relationIndex(label = {1}) " +
			"MATCH term1<-[:STARTS_WITH]-dep-[:ENDS_WITH]->term2, dep-->rel " +
			"RETURN dep")
	Iterable<DependenceNode> findDependence(String head, String rel, String mod);
	
	@Query("START doc=node({0}), dep=node({1}) " +
			"MATCH doc-[rel:CONTAINS]->dep " +
			"RETURN rel")
	Iterable<DocumentDependenceRelation> findDocumentDependencyRelation(Long docId, Long depId);
	
	@Query("START term1=node:termIndex(label={0}) " +
			"MATCH doc-[cont:CONTAINS]->dep-[:STARTS_WITH]->term1 " +
			"WITH dep, cont, sum(cont.occurences) as occur " +
			"RETURN dep " +
			"ORDER BY occur")
	Iterable<DependenceNode> findDependencesStartWith(String term);

	@Query("START term1=node({0}), term2=node({1}), rel=node({2}) " +
			"MATCH term1<-[:STARTS_WITH]-dep-[:ENDS_WITH]->term2, dep-->rel " +
			"RETURN ID(dep)")
	List<Long> findDependenceByIds(Long termID, Long termID2, Long relationID);
}
