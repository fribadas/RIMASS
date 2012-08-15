
package es.uvigo.rimass.collection;

import java.io.File;
import java.util.Properties;

/**
 *
 * @author ribadas
 */
public class Configuration extends Properties {

    public Configuration() {
        this("/tmp");
    }
    
    
    public Configuration(String baseDir) {
        super();        
        setProperty("base_dir", baseDir);
    }


}
