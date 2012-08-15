package es.uvigo.rimass.thesaurus;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptorMetadata {
    @XmlAttribute
    String type;

    public DescriptorMetadata() {
    }

    public DescriptorMetadata(String type) {
        this.type = type;
    }

    
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    
}
