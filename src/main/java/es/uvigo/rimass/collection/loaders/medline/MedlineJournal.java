
package es.uvigo.rimass.collection.loaders.medline;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author ribadas
 */
@XmlRootElement
@XmlType(propOrder={"name", "issue", "ISSN", "volume", "pages", "date"})
public class MedlineJournal {
    private String name;
    private String issue;
    private String ISSN;
    private String volume;
    private String pages;
    private String date;
    

    public MedlineJournal() {
    }

    public MedlineJournal(String name, String issue, String ISSN, String volume, String pages, String date) {
        this.name = name;
        this.issue = issue;
        this.ISSN = ISSN;
        this.volume = volume;
        this.pages = pages;
        this.date = date;
    }

    public String getISSN() {
        return ISSN;
    }

    public void setISSN(String ISSN) {
        this.ISSN = ISSN;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }
    
    
    
}
