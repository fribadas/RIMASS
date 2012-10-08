package es.uvigo.rimass.collection.store.neo4j.entities;


import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.annotation.RelatedToVia;


/**
 *
 * @author ribadas
 */
@NodeEntity
public class DependenceNode {

	@GraphId
	private Long id;

	@RelatedTo(type="WITH_RELATION", direction=Direction.OUTGOING) 
	RelationNode relation;
	
	@Fetch @RelatedTo(type="STARTS_WITH", direction=Direction.OUTGOING)
    TermNode head;
    
	@Fetch @RelatedTo(type="ENDS_WITH", direction=Direction.OUTGOING)
	TermNode modifier;
    
    @RelatedToVia(type = "CONTAINS", direction = Direction.INCOMING)
    Set<DocumentDependenceRelation> documents;

    public TermNode getHead() {
        return head;
    }

    public void setHead(TermNode head) {
        this.head = head;
    }

    public TermNode getModifier() {
        return modifier;
    }

    public void setModifier(TermNode modifier) {
        this.modifier = modifier;
    }

    public RelationNode getRelation() {
        return relation;
    }

    public void setRelation(RelationNode relation) {
        this.relation = relation;
    }
    
    public Long getId() {
    	return id;
    }
    
    public void setId(Long id) {
    	this.id = id;
    }

}
