package es.uvigo.rimass.thesaurus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

/**
 *
 *
 */
@XmlType(propOrder = {"label", "metadata", "terms", "ascendants", "descendants"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Descriptor implements Cloneable {

    @XmlAttribute
    @XmlID
    String id;
    String label;
    DescriptorMetadata metadata;
    @XmlElementWrapper
    @XmlElement(name = "term")
    //private List<Term> terms;
    private Set<String> terms;
    @XmlElementWrapper
    @XmlElement(name = "ascendant")
    @XmlIDREF
    private Set<Descriptor> ascendants;
    @XmlElementWrapper
    @XmlElement(name = "descendant")
    @XmlIDREF
    private Set<Descriptor> descendants;

    public Descriptor() {
    }

    public Set<Descriptor> getAscendants() {
        return ascendants;
    }

    public void setAscendants(Set<Descriptor> ascendants) {
        this.ascendants = ascendants;
    }

    public Set<Descriptor> getDescendants() {
        return descendants;
    }

    public void setDescendants(Set<Descriptor> descendants) {
        this.descendants = descendants;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public DescriptorMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(DescriptorMetadata metadata) {
        this.metadata = metadata;
    }

//    public List<Term> getTerms() {
//        return terms;
//    }
//
//    public void setTerms(List<Term> terms) {
//        this.terms = terms;
//    }
    public Set<String> getTerms() {
        return terms;
    }

    public void setTerms(Set<String> terms) {
        this.terms = terms;
    }

    public Set<Descriptor> getDescendantsByContext(String parentContext) {
        if ((this.metadata != null) &&
            (this.metadata instanceof ContextManager)) {
            ContextManager contextManager = (ContextManager) this.metadata;
            return contextManager.descendantsByContext(parentContext);
        } else {
            return this.descendants;
        }
    }

    public String getCurrentContext(String parentContext) {
        if ((this.metadata != null) &&
            (this.metadata instanceof ContextManager)) {
            ContextManager contextManager = (ContextManager) this.metadata;
            return contextManager.currentContext(parentContext);
        } else {
            return this.getId();
        }
    }
    
    
    public Descriptor copy() {
        Descriptor copy = null;
        try {
            copy = (Descriptor) this.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Descriptor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Descriptor other = (Descriptor) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return "Descriptor{" + "id=" + id + ", label=" + label + "}"; //, terms=" + terms + ", ascendants=" + ascendants + ", descendants=" + descendants + '}';
    }
}
