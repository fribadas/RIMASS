
package es.uvigo.rimass.collection.store.neo4j.entities;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.support.index.IndexType;

/**
 *
 * @author ribadas
 */
@NodeEntity
public class TermNode {
    @GraphId
	private Long id;
    
    @Indexed(indexName="termIndex", indexType=IndexType.SIMPLE, unique=true)
    String label;
    
    boolean isThesaurusEntity;
    
    @RelatedTo(type="SIBLING_OF", direction=Direction.BOTH, elementClass=TermNode.class)
    Set<TermNode> siblings;
    
	@RelatedTo(type="PARENT_OF", direction=Direction.OUTGOING, elementClass=TermNode.class) 
    Set<TermNode> parents;
    
    @RelatedTo(type="PARENT_OF", direction=Direction.INCOMING, elementClass=TermNode.class)
    Set<TermNode> descendants;
    
    @RelatedTo(type="STARTS_WITH", direction=Direction.INCOMING)
    Set<DependenceNode> startsWithIn;
    
    @RelatedTo(type="ENDS_WITH", direction=Direction.INCOMING)
    Set<DependenceNode> endsWithIn;
    
    public TermNode() {
    	siblings = new HashSet<TermNode>();
    	parents = new HashSet<TermNode>();
    	descendants = new HashSet<TermNode>();
    }
    
    
    public TermNode(String label, boolean isThesaurusEntity) {
    	super();
    	this.label = label;
        this.isThesaurusEntity = isThesaurusEntity;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isIsThesaurusEntity() {
        return isThesaurusEntity;
    }

    public void setIsThesaurusEntity(boolean isThesaurusEntity) {
        this.isThesaurusEntity = isThesaurusEntity;
    }

    public Set<TermNode> getParents() {
        return parents;
    }

    public void setParents(Set<TermNode> parents) {
        this.parents = parents;
    }

    public Set<TermNode> getDescendants() {
        return descendants;
    }

    public void setDescendants(Set<TermNode> descendants) {
        this.descendants = descendants;
    }
    public Set<TermNode> getSiblings() {
    	return siblings;
    }
    
    
    public void setSiblings(Set<TermNode> siblings) {
    	this.siblings = siblings;
    }

	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}
}
