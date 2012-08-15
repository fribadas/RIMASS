
package es.uvigo.rimass.collection.store.neo4j.entities;

import java.util.Set;
import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 *
 * @author ribadas
 */
@NodeEntity
public class TermNode {
    String label;
    boolean isThesaurusEntity;
    
    Set<TermNode> parents;
    Set<TermNode> descendants;
    Set<TermNode> relatedTerms;

    public TermNode() {
    }
    
    
    public TermNode(String label, boolean isThesaurusEntity) {
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

    public Set<TermNode> getRelatedTerms() {
        return relatedTerms;
    }

    public void setRelatedTerms(Set<TermNode> relatedTerms) {
        this.relatedTerms = relatedTerms;
    }
    
    
    
}
