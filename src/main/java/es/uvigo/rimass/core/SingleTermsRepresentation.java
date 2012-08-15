
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
public class SingleTermsRepresentation extends Representation {
    
    private List<String> terms;


    public SingleTermsRepresentation() {
        super("single_terms");
    }

    public SingleTermsRepresentation(List<String> terms) {
        super("single_terms");
        this.terms = terms;
    }

    @XmlElementWrapper
    @XmlElement(name="term")
    public List<String> getTerms() {
        return terms;
    }

    public void setTerms(List<String> terms) {
        this.terms = terms;
    }            
}
