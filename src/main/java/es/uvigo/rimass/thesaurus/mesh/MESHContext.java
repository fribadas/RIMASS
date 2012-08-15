/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uvigo.rimass.thesaurus.mesh;

import es.uvigo.rimass.thesaurus.Descriptor;
import java.util.Set;
import javax.xml.bind.annotation.*;

/**
 *
 * @author ribadas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"treeNumber", "treeDescendants"})
public class MESHContext {

    String treeNumber;
    
    @XmlElementWrapper
    @XmlElement(name = "treeDescendat")
    @XmlIDREF
    Set<Descriptor> treeDescendants;

    public MESHContext() {
    }

    public Set<Descriptor> getTreeDescendants() {
        return treeDescendants;
    }

    public void setTreeDescendants(Set<Descriptor> treeDescendants) {
        this.treeDescendants = treeDescendants;
    }

    public String getTreeNumber() {
        return treeNumber;
    }

    public void setTreeNumber(String treeNumber) {
        this.treeNumber = treeNumber;
    }
}
