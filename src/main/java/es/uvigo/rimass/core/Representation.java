
package es.uvigo.rimass.core;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ribadas
 */
@XmlRootElement
public abstract class Representation {
    private String type;

    public Representation() {
    }

    public Representation(String type) {
        this.type = type;
    }

    @XmlAttribute
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
}
