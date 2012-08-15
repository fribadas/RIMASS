package es.uvigo.rimass.collection.store.xml;

import es.uvigo.rimass.collection.Collection;
import es.uvigo.rimass.collection.Configuration;
import es.uvigo.rimass.collection.Store;
import es.uvigo.rimass.collection.loaders.medline.MedlineMetadata;
import es.uvigo.rimass.core.DependencesRepresentation;
import es.uvigo.rimass.core.Document;
import es.uvigo.rimass.core.Metadata;
import es.uvigo.rimass.core.SingleTermsRepresentation;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author ribadas
 */
public class XMLStore implements Store {

    private File storeDir;
    private JAXBContext context;
    private Marshaller marshaller;
    private Unmarshaller unmarshaller;

    public void initialize(Collection collection, Configuration configuration) {

        this.storeDir = new File(configuration.getProperty("base_dir") + File.separator
                + collection.getName() + File.separator + "store" + File.separator + "xml");


        if (!this.storeDir.exists()) {
            storeDir.mkdirs();
        }
        try {
            this.context = JAXBContext.newInstance(Document.class, MedlineMetadata.class, DependencesRepresentation.class, SingleTermsRepresentation.class);  // TODO: hacer independiente de MedlineMetadata, etc
            this.marshaller = context.createMarshaller();
            this.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            this.unmarshaller = context.createUnmarshaller();
        } catch (JAXBException ex) {
            Logger.getLogger(XMLStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void storeDocument(Document<? extends Metadata> document) {                
        try {
            this.marshaller.marshal(document, new File(storeDir, document.getDocid() + ".xml"));
        } catch (JAXBException ex) {
            Logger.getLogger(XMLStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Document<? extends Metadata> retrieveDocument(long docid) {
        File xmlFile = new File(this.storeDir, docid + ".xml");
        if (xmlFile.isFile()) {
            try {
                Document d = (Document) this.unmarshaller.unmarshal(xmlFile);
                return d;
            } catch (JAXBException ex) {
                Logger.getLogger(XMLStore.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null; // Otherwise
    }
}
