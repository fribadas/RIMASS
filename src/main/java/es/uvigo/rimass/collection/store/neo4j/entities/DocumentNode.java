package es.uvigo.rimass.collection.store.neo4j.entities;

import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedToVia;
import org.springframework.data.neo4j.support.index.IndexType;

/**
 *
 * @author ribadas
 */
@NodeEntity
public class DocumentNode {
    @GraphId Long id;
    
    @Indexed(indexName="docTitleIndex", indexType=IndexType.FULLTEXT)
    String title;
    
    @Indexed(indexName="docTextIndex", indexType=IndexType.FULLTEXT)
    String text;
    
    @RelatedToVia(type = "CONTAINS", direction = Direction.OUTGOING)
    Set<DocumentDependenceRelation> dependences;
    
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

    public Set<DocumentDependenceRelation> getDependences() {
        return dependences;
    }

    public void setDependences(Set<DocumentDependenceRelation> dependences) {
        this.dependences = dependences;
    }

    void addDependence(DependenceNode dependence) {
    }
}
