package es.uvigo.rimass.collection;

/**
 *
 * @author ribadas
 */
public class Language {
    private String name;
    private String shortName;

    public Language(String name, String shortName) {
        this.name = name;
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
    
    
}
