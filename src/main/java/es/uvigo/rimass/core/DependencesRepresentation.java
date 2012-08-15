
package es.uvigo.rimass.core;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ribadas
 */
@XmlRootElement
public class DependencesRepresentation extends Representation {
    private List<Dependence> dependences;


    public DependencesRepresentation() {
        super("dependences");
    }

    public DependencesRepresentation(List<Dependence> dependences) {
        super("dependences");
        this.dependences = dependences;
    }

    @XmlElementWrapper
    @XmlElement(name="dependence")
    public List<Dependence> getDependences() {
        return dependences;
    }

    public void setDependences(List<Dependence> dependences) {
        this.dependences = dependences;
    }

  
}
