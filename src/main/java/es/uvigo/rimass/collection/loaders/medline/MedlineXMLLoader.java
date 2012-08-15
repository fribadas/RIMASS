package es.uvigo.rimass.collection.loaders.medline;

import es.uvigo.rimass.collection.Configuration;
import es.uvigo.rimass.collection.Loader;
import es.uvigo.rimass.collection.Collection;
import es.uvigo.rimass.core.*;
import es.uvigo.rimass.collection.loaders.medline.jaxb.Abstract;
import es.uvigo.rimass.collection.loaders.medline.jaxb.AbstractText;
import es.uvigo.rimass.collection.loaders.medline.jaxb.Article;
import es.uvigo.rimass.collection.loaders.medline.jaxb.MedlineCitation;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Iterator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author ribadas
 */
public class MedlineXMLLoader implements Loader {

    public void initialize(Collection collection, Configuration configuration) {
    }

    public Iterable<Document<? extends Metadata>> documentIterable(String resource) {
        return new MedlineIterable(resource);
    }
}

class MedlineIterable implements Iterable<Document<? extends Metadata>> {

    private String resource;

    public MedlineIterable(String resource) {
        this.resource = resource;
    }

    public Iterator<Document<? extends Metadata>> iterator() {
        return new MedlineIterator(resource);
    }
}

class MedlineIterator implements Iterator<Document<? extends Metadata>> {

    private Document<MedlineMetadata> next = null;
    private BufferedReader in;
    private JAXBContext context;
    private Unmarshaller unmarshaller;

    MedlineIterator(String resource) {
        try {
            this.in = new BufferedReader(new FileReader(resource));
            this.context = JAXBContext.newInstance(MedlineCitation.class);
            this.unmarshaller = context.createUnmarshaller();

            this.next = loadMedlineCitation();
        } catch (Exception e) {
            System.err.println("Error loading Medline file " + resource);
            e.printStackTrace(System.err);
        }

    }

    public boolean hasNext() {
        return next != null;
    }

    public Document<MedlineMetadata> next() {
        Document<MedlineMetadata> current = next;
        next = loadMedlineCitation();
        return current;
    }

    public void remove() {
        // Not implemented
    }

    private Document<MedlineMetadata> loadMedlineCitation() {
        Document<MedlineMetadata> result = null;

        try {
            String line = this.in.readLine();
            while (!line.matches("<MedlineCitation .+>")) {
                line = this.in.readLine();
                if ((line == null) || line.isEmpty()) {
                    return null;  // TODO: change to unexpectedEOFException
                }
            }

            StringBuilder builder = new StringBuilder();
            while (!line.trim().startsWith("</MedlineCitation>")) {
                builder.append(line);
                builder.append('\n');
                line = this.in.readLine();
                if ((line == null) || line.isEmpty()) {
                    return null; // TODO: change to unexpectedEOFException
                }
            }
            builder.append(line);  // Add last closing element "</MedlineCitation>"
            if (builder.length() > 0) {
                builder.insert(0, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                //System.out.println("---CITATION---------\n"+builder.toString().replaceAll("\n", "  ") +"----------------------\n");
                MedlineCitation medlineCitation = (MedlineCitation) unmarshaller.unmarshal(new StringReader(builder.toString()));
                result = extractDocument(medlineCitation);
            }
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    private Document<MedlineMetadata> extractDocument(MedlineCitation medlineCitation) {
        Document<MedlineMetadata> document = new Document<MedlineMetadata>();
        MedlineMetadata medlineMetadata = new MedlineMetadata(medlineCitation);

        document.setDocid(Long.parseLong(medlineMetadata.getPMID()));

        if (medlineCitation.getArticle() != null) {
            Article article = medlineCitation.getArticle();

            document.setTitle(article.getArticleTitle());
            document.setText(extractText(article));
        }
        document.setMetadata(medlineMetadata);

        return document;
    }

    private String extractText(Article article) {
        StringBuilder builder = new StringBuilder();
                    
        Abstract articleAbstract = article.getAbstract();
        if (articleAbstract != null) {
            for (AbstractText text : articleAbstract.getAbstractText()) {
                builder.append(text.getvalue());
                builder.append(' ');
            }
        }
        return builder.toString();
    }
}