package es.uvigo.rimass.collection.store.neo4j.entities;


import java.util.Set;
import org.neo4j.graphdb.Direction;
//import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;


/**
 *
 * @author ribadas
 */
@NodeEntity
public class DependenceNode {

    RelationNode relation;
    TermNode head;
    TermNode modifier;
    @RelatedTo(type = "CONTAINS", direction = Direction.INCOMING)
    Set<DocumentNode> documents;

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
}
