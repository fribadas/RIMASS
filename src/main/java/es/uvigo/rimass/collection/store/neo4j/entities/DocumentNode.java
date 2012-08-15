package es.uvigo.rimass.collection.store.neo4j.entities;

import java.util.Set;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

/**
 *
 * @author ribadas
 */
@NodeEntity
public class DocumentNode {
    long id;
    @Indexed
    String title;
    @Indexed
    String text;
    @RelatedTo(type = "CONTAINS", direction = Direction.OUTGOING)
    Set<DependenceNode> dependences;
    
    Set<TermNode> terms;

    public DocumentNode() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Set<DependenceNode> getDependences() {
        return dependences;
    }

    public void setDependences(Set<DependenceNode> dependences) {
        this.dependences = dependences;
    }

    public Set<TermNode> getTerms() {
        return terms;
    }

    public void setTerms(Set<TermNode> terms) {
        this.terms = terms;
    }

    void addDependence(DependenceNode dependence) {
    }
}
