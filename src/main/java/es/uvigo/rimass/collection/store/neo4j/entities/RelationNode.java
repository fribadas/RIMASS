
package es.uvigo.rimass.collection.store.neo4j.entities;

import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 *
 * @author ribadas
 */
@NodeEntity
public class RelationNode {
    @Indexed String label;

    public RelationNode() {
    }

    public RelationNode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    
}
