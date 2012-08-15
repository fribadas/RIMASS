
package es.uvigo.rimass.core;

/**
 *
 * @author ribadas
 */
public abstract class Metadata {
    private String type;

    public Metadata() {
    }

    public Metadata(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    
}
