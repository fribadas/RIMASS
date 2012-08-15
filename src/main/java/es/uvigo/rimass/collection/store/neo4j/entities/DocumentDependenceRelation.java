package es.uvigo.rimass.collection.store.neo4j.entities;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

/**
 *
 * @author ribadas
 */
@RelationshipEntity(type = "CONTAINS")
public class DocumentDependenceRelation {

    @StartNode
    DocumentNode document;
    @EndNode
    DependenceNode dependence;
    int occurences;

    public DependenceNode getDependence() {
        return dependence;
    }

    public void setDependence(DependenceNode dependence) {
        this.dependence = dependence;
    }

    public DocumentNode getDocument() {
        return document;
    }

    public void setDocument(DocumentNode document) {
        this.document = document;
    }

    public int getOccurences() {
        return occurences;
    }

    public void setOccurences(int occurences) {
        this.occurences = occurences;
    }
    
    
}
