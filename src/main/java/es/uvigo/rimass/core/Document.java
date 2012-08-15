
package es.uvigo.rimass.core;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.*;

/**
 *
 * @author ribadas
 */
@XmlRootElement
@XmlType(propOrder={"metadata", "title", "text", "representations"})

public class Document<M extends Metadata> {
    private long docid;
    private String title;
    private String text;
    
    private M metadata;
    private List<Representation> representations;

    public Document() {
    }

    
    public Document(long docid, String title, String text, M metadata) {
        this(docid, title, text, metadata, null);
    }

    
    public Document(long docid, String title, String text, M metadata, List<Representation> representations) {
        this.docid = docid;
        this.title = title;
        this.text = text;
        this.metadata = metadata;
        this.representations = representations;
    }

    @XmlAttribute
    public long getDocid() {
        return docid;
    }

    public void setDocid(long docid) {
        this.docid = docid;
    }

    public M getMetadata() {
        return metadata;
    }

    public void setMetadata(M metadata) {
        this.metadata = metadata;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @XmlElementWrapper
    @XmlElement(name="representation")
    public List<Representation> getRepresentations() {
        return representations;
    }

    public void setRepresentations(List<Representation> representations) {
        this.representations = representations;
    }
    
    
    public void addRepresentation(Representation representation){
        if (this.representations == null) {
            this.representations = new ArrayList<Representation>();
        }
    
        this.representations.add(representation);
    }

    public Representation representationByType(String type){
        Representation result = null;
        
        if (this.representations != null){
            for (Representation r : this.representations){
                if (type.equals(r.getType())) {
                    return r;
                }
            }
        }
        
        return result;
    }
    
}
