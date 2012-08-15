
package es.uvigo.rimass.core;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author ribadas
 */
@XmlRootElement
@XmlType(propOrder={"head","modifier"})
public class Dependence {
    private String relation;
    private String head;
    private String modifier;

    public Dependence() {
    }

    public Dependence(String relation, String head, String modifier) {
        this.relation = relation;
        this.head = head;
        this.modifier = modifier;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    @XmlAttribute
    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    @Override
    public String toString() {
        return relation + '(' + head + ',' + modifier + ')';
    }
    
    

}
