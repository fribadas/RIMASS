package es.uvigo.rimass.collection;

/**
 *
 * @author ribadas
 */
public class Collection {
    private String name;
    private Language language;

    public Collection() {
    }   
    
    public Collection(String name, Language language) {
        this.name = name;
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

 

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
    
    
}
