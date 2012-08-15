package es.uvigo.rimass.thesaurus;

import es.uvigo.rimass.thesaurus.mesh.MESHMetadata;
import java.io.File;
import java.io.PrintStream;
import java.util.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

/**
 *
 *
 */
@XmlRootElement
@XmlType(propOrder = {"name", "roots", "descriptors"})
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Thesaurus {

    private String name;
    private List<Root> roots;
    private List<Descriptor> descriptors;
    private Map<String, Descriptor> descriptorIDLookup;
    private boolean createLabelLookup;
    private Map<String, Set<Descriptor>> labelLookup;

    public Thesaurus() {
        createLabelLookup = false;
        labelLookup = null;
        descriptorIDLookup = null;
    }

    public Thesaurus(File thesaurusFile) {
        this(thesaurusFile, false);
    }

    public Thesaurus(String thesaurusFileName) {
        this(new File(thesaurusFileName), false);
    }

    public Thesaurus(String thesaurusFileName, boolean createLabelLookup) {
        this(new File(thesaurusFileName), createLabelLookup);
    }

    public Thesaurus(File thesaurusFile, boolean createLabelLookup) {
        try {
            // JAXBContext context = JAXBContext.newInstance(Thesaurus.class);
            JAXBContext context = JAXBContext.newInstance(Thesaurus.class, MESHMetadata.class);  // TODO: independiente de MESHMetadata
            Unmarshaller um = context.createUnmarshaller();
            Thesaurus aux = (Thesaurus) um.unmarshal(thesaurusFile);

            this.name = aux.name;
            this.roots = aux.roots;
            this.descriptors = aux.descriptors;

            if (createLabelLookup) {
                this.createLabelLookup = true;
                createLabelLookup();
            } else {
                this.createLabelLookup = false;
                this.labelLookup = null;
            }

        } catch (Exception e) {
            System.err.println("Error loading thesaurus from " + thesaurusFile.getAbsolutePath());
            e.printStackTrace(System.err);
        }
    }

    @XmlElementWrapper
    @XmlElement(name = "descriptor")
    public List<Descriptor> getDescriptors() {
        return this.descriptors;
    }

    public void setDescriptors(List<Descriptor> descriptors) {
        this.descriptors = descriptors;
    }

    @XmlElementWrapper
    @XmlElement(name = "root")
    public List<Root> getRoots() {
        return roots;
    }

    public void setRoots(List<Root> roots) {
        this.roots = roots;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addDescriptor(Descriptor descriptor) {
        if (this.descriptors == null) {
            this.descriptors = new LinkedList<Descriptor>();
        }
        this.descriptors.add(descriptor);

        if (this.descriptorIDLookup == null) {
            this.descriptorIDLookup = new HashMap<String, Descriptor>();
        }
        this.descriptorIDLookup.put(descriptor.getId(), descriptor);

        if (createLabelLookup) {
            if (this.labelLookup == null) {
                this.labelLookup = new HashMap<String, Set<Descriptor>>();
            }
            anotateDescriptorInLabelLookup(descriptor);
        }
    }

    public Descriptor findDescriptorByID(String descriptorID) {
        if (this.descriptorIDLookup == null) {
            createDescriptorIDLookup();
        }
        return this.descriptorIDLookup.get(descriptorID);
    }

    public Set<Descriptor> findDescriptorsByLabel(String label) {
        if (this.labelLookup == null) {
            createLabelLookup = true;
            createLabelLookup();
        }
        if (labelLookup.containsKey(label)) {
            return labelLookup.get(label);
        } else {
            return null;
        }
    }

    private void createLabelLookup() {
        if (this.labelLookup == null) {
            this.labelLookup = new HashMap<String, Set<Descriptor>>();
        }
        for (Descriptor descriptor : this.descriptors) {
            anotateDescriptorInLabelLookup(descriptor);
        }
    }

    private void anotateDescriptorInLabelLookup(Descriptor descriptor) {
        String label = descriptor.getLabel();
        Set<Descriptor> descriptorList;
        if (this.labelLookup.containsKey(label)) {
            descriptorList = this.labelLookup.get(label);
        } else {
            descriptorList = new HashSet<Descriptor>();
            this.labelLookup.put(descriptor.getLabel().toLowerCase(), descriptorList);
        }
        descriptorList.add(descriptor);
    }

    private void createDescriptorIDLookup() {
        this.descriptorIDLookup = new HashMap<String, Descriptor>();

        for (Descriptor descriptor : this.descriptors) {                       
            descriptorIDLookup.put(descriptor.getId(), descriptor);
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            showUsuage();
            System.exit(0);
        }

        Thesaurus t = new Thesaurus(args[0]);
        t.dump(System.out);
    }

    private static void showUsuage() {
        System.out.println("Usage : java -cp RIMASS.jar es.uvigo.rimass.thesaurus.Thesaurus <Thesaurus xml file>");
    }

    public void dump(PrintStream out) {
        for (Root r : this.roots) {
            out.println("----------------------------");
            out.println(" Root " + r.getName() + "    descriptor:" + r.getDescriptor().getId() + " " + r.getDescriptor().getLabel());
            out.println("----------------------------");
            for (Descriptor d : r.getDescriptor().getDescendants()) {
                dumpRecursive(d, out, 0);
            }
            out.println();
        }
    }

    private void dumpRecursive(Descriptor d, PrintStream out, int tabs) {
        StringBuilder sbLine = new StringBuilder();
        for (int i = 0; i < tabs; i++) {
            sbLine.append("  ");
        }
        sbLine.append(d.getId());
        sbLine.append(' ');
        sbLine.append(d.getLabel());
        if (d.getMetadata() != null) {
            sbLine.append(d.getMetadata().toString());
        }

        out.println(sbLine.toString());

        if (d.getDescendants() != null) {
            for (Descriptor s : d.getDescendants()) {
                dumpRecursive(s, out, tabs + 1);
            }
        }
    }
}
