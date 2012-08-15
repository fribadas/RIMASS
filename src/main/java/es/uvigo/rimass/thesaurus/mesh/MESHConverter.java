package es.uvigo.rimass.thesaurus.mesh;

import es.uvigo.rimass.thesaurus.Descriptor;
import es.uvigo.rimass.thesaurus.Root;
import es.uvigo.rimass.thesaurus.Thesaurus;
import es.uvigo.rimass.thesaurus.mesh.jaxb.*;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 *
 */
public class MESHConverter {

    public static Map<String, String> rootLabels;

    static {
        HashMap<String, String> temp = new HashMap<String, String>();
        temp.put("A", "Anatomy");
        temp.put("B", "Organisms");
        temp.put("C", "Diseases");
        temp.put("D", "Chemicals and Drugs");
        temp.put("E", "Analytical, Diagnostic and Therapeutic Techniques and Equipment");
        temp.put("F", "Psychiatry and Psychology");
        temp.put("G", "Biological Sciences");
        temp.put("H", "Physical Sciences");
        temp.put("I", "Anthropology, Education, Sociology and Social Phenomena");
        temp.put("J", "Technology and Food and Beverages");
        temp.put("K", "Humanities");
        temp.put("L", "Information Science");
        temp.put("M", "Persons");
        temp.put("N", "Health Care");
        temp.put("V", "Publication Characteristics");
        temp.put("Z", "Geographic Locations");
        MESHConverter.rootLabels = Collections.unmodifiableMap(temp);
    }
    
    HashMap<String, String> treeNumber2id;
    HashMap<String, Descriptor> id2descriptor;
    HashMap<String, Descriptor> roots;

    public void convertMESHFileToGraph(String inputFile, String outputFile) {
        convertMESHFile(inputFile, outputFile, true);
    }

    public void convertMESHFileToTree(String inputFile, String outputFile) {
        convertMESHFile(inputFile, outputFile, false);
    }

    private void convertMESHFile(String inputFile, String outputFile, boolean asGraph) {
        try {
            treeNumber2id = new HashMap<String, String>();
            id2descriptor = new HashMap<String, Descriptor>();
            roots = new HashMap<String, Descriptor>();

            loadDescriptorsFromFile(inputFile, asGraph);
            linkDescriptors(asGraph);
            saveXMLThesaurus(outputFile);
        } catch (JAXBException ex) {
            Logger.getLogger(MESHConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadDescriptorsFromFile(String inputFile, boolean asGraph) throws JAXBException {
        JAXBContext contextMesh = JAXBContext.newInstance(DescriptorRecordSet.class);

        Unmarshaller um = contextMesh.createUnmarshaller();
        DescriptorRecordSet rs = (DescriptorRecordSet) um.unmarshal(new File(inputFile));

        for (DescriptorRecord r : rs.getDescriptorRecord()) {
            loadDescriptorFromMESHRecord(r, asGraph);
        }
    }

    private void loadDescriptorFromMESHRecord(DescriptorRecord r, boolean asGraph) {
        if (asGraph) {
            Descriptor descriptor = new Descriptor();
            descriptor.setLabel(r.getDescriptorName().getString());
            descriptor.setMetadata(new MESHMetadata(r));
            descriptor.setTerms(extractTermList(r));

            descriptor.setId(r.getDescriptorUI());
            descriptor.setAscendants(new HashSet<Descriptor>());
            descriptor.setDescendants(new HashSet<Descriptor>());

            if (r.getTreeNumberList() != null) {
                for (TreeNumber tn : r.getTreeNumberList().getTreeNumber()) {
                    String treeNumber = tn.getvalue();
                    treeNumber2id.put(treeNumber, descriptor.getId());
                }
                id2descriptor.put(descriptor.getId(), descriptor);
            }
        } else {  // As tree -> Replicate Descriptor for every TreeNumber
            if (r.getTreeNumberList() != null) {
                for (TreeNumber tn : r.getTreeNumberList().getTreeNumber()) {
                    String treeNumber = tn.getvalue();

                    Descriptor descriptor = new Descriptor();
                    descriptor.setLabel(r.getDescriptorName().getString());
                    descriptor.setMetadata(new MESHMetadata(r));
                    descriptor.setTerms(extractTermList(r));

                    descriptor.setId(treeNumber);
                    descriptor.setAscendants(new HashSet<Descriptor>(1));
                    descriptor.setDescendants(new HashSet<Descriptor>());

                    treeNumber2id.put(treeNumber, descriptor.getId());
                    id2descriptor.put(descriptor.getId(), descriptor);
                }
            }
        }

    }

    private Set<String> extractTermList(DescriptorRecord r) {
        HashSet<String> termList = new HashSet<String>();
//        List<String> termList = new ArrayList<String>();

        if (r.getConceptList() != null) {
            for (Concept c : r.getConceptList().getConcept()) {
                termList.add(c.getConceptName().getString());
                if (c.getTermList() != null) {
                    for (Term t : c.getTermList().getTerm()) {
                        if (!t.getIsPermutedTermYN().equals("Y")) {
                            termList.add(t.getString());
                        }
                    }
                }
            }
        }

        return termList;
    }

    private void linkDescriptors(boolean asGraph) {
        for (Descriptor descriptor : id2descriptor.values()) {

            List<String> treeNumberList;
            if (asGraph) {
                treeNumberList = ((MESHMetadata) descriptor.getMetadata()).meshTreeNumbers;
            } else {  // In tree mode, descriptor where previously duplicated -> treat only the desciptorID=treenumber 
                treeNumberList = new ArrayList<String>(1);
                treeNumberList.add(descriptor.getId());
            }
            for (String treeNumber : treeNumberList) {
                String parentTreeNumber = extractParentTreeNumber(treeNumber);
                Descriptor parentDescriptor;

                // Store roots
                if (parentTreeNumber.length() == 1) {
                    // It's one of the roots
                    if (!roots.containsKey(parentTreeNumber)) {
                        // Never seen root -> create it
                        Descriptor newRoot = new Descriptor();
                        newRoot.setId(parentTreeNumber);
                        newRoot.setLabel(MESHConverter.rootLabels.get(parentTreeNumber));
                        newRoot.setAscendants(null);
                        newRoot.setDescendants(new HashSet<Descriptor>());

                        MESHMetadata metadata = new MESHMetadata();
                        metadata.setMeshID(parentTreeNumber);
                        metadata.setMeshTreeNumbers(new ArrayList<String>());
                        metadata.getMeshTreeNumbers().add(parentTreeNumber);
                        metadata.setMeshContexts(new ArrayList<MESHContext>());

                        MESHContext meshContext = new MESHContext();
                        meshContext.setTreeNumber(parentTreeNumber);
                        meshContext.setTreeDescendants(new HashSet<Descriptor>());
                        metadata.getMeshContexts().add(meshContext);

                        newRoot.setMetadata(metadata);

                        roots.put(parentTreeNumber, newRoot);

                        //System.out.println("New ROOT: " + parentTreeNumber + "  " + newRoot.toString());

                        parentDescriptor = newRoot;
                    } else {
                        parentDescriptor = roots.get(parentTreeNumber);
                    }
                } else {
                    parentDescriptor = id2descriptor.get(treeNumber2id.get(parentTreeNumber));
                }

                // Link ascendant and descendants
                if (!asGraph) {
                    parentDescriptor.getDescendants().add(descriptor);
                    descriptor.getAscendants().add(parentDescriptor);
                } else {
                    //boolean addAsDescendant = true;
                    //for (Descriptor descendant : parentDescriptor.getDescendants()) {
                    //    if (descendant.equals(descriptor)) {
                    //        // Avoid repeting when treated as a graph
                    //        addAsDescendant = false;
                    //    }
                    //}
                    //if (addAsDescendant) {
                    parentDescriptor.getDescendants().add(descriptor);

                    MESHMetadata meshMetadata = (MESHMetadata) parentDescriptor.getMetadata();
                    meshMetadata.addContextualLink(parentTreeNumber, descriptor);

                    //}

                    //boolean addAsAscendant = true;
                    //for (Descriptor ascendant : descriptor.getAscendants()) {
                    //    if (ascendant.equals(parentDescriptor)) {
                    //        // Avoid repeting when treated as a graph
                    //        addAsAscendant = false;
                    //    }
                    //}
                    //if (addAsAscendant) {
                    descriptor.getAscendants().add(parentDescriptor);
                    //}
                }
            }
        }
    }

    private String extractParentTreeNumber(String currentTreeNumber) {
        int idxLastPart = currentTreeNumber.lastIndexOf(".");

        if (idxLastPart != -1) {
            return (currentTreeNumber.substring(0, currentTreeNumber.lastIndexOf(".")));
        } else {
            // First level descendants -> get branch letter
            return currentTreeNumber.substring(0, 1);
        }
    }

    private void saveXMLThesaurus(String outputFileName) throws JAXBException {
        JAXBContext contextThesaurus = JAXBContext.newInstance(Thesaurus.class, MESHMetadata.class);
        Marshaller m = contextThesaurus.createMarshaller();

        Thesaurus t = new Thesaurus();
        t.setName(outputFileName);
        t.setRoots(new ArrayList<Root>());
        for (Descriptor aux : roots.values()) {
            Root r = new Root();
            r.setName(aux.getLabel());
            r.setDescriptor(aux);
            t.getRoots().add(r);

            id2descriptor.put(aux.getId(), aux);
        }

        t.setDescriptors(new ArrayList<Descriptor>());
        for (Descriptor descriptor : id2descriptor.values()) {
            if ((null != descriptor.getDescendants()) && descriptor.getDescendants().isEmpty()) {
                descriptor.setDescendants(null);
            }
            if ((null != descriptor.getAscendants()) && descriptor.getAscendants().isEmpty()) {
                descriptor.setAscendants(null);
            }

            //t.getDescriptors().add(descriptor);   // REFORMA THESAURUS (anadir lookup de etiquetas)
            t.addDescriptor(descriptor);
        }
        //t.getDescriptors().addAll(id2descriptor.values());

        File outputFile = new File(outputFileName);
        outputFile.getParentFile().mkdirs();
        
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(t, outputFile);

    }

    public static void main(String[] args) throws JAXBException {
        // TESTS
        // java -cp dist/CJT.jar cjt.mesh.MESHConverter tree datos/desc2011.xml datos/thesaurus.tree.xml
        // java -cp dist/CJT.jar cjt.mesh.MESHConverter graph datos/desc2011.xml datos/thesaurus.graph.xml


        if (args.length != 3) {
            showUsage();
            System.exit(0);
        }

        MESHConverter converter = new MESHConverter();

        if ("tree".equals(args[0])) {
            converter.convertMESHFileToTree(args[1], args[2]);
        } else if ("graph".equals(args[0])) {
            converter.convertMESHFileToGraph(args[1], args[2]);
        } else {
            showUsage();
        }

    }

    private static void showUsage() {
        System.out.println("Usage:");
        System.out.println("java -cp RIMASS.jar es.uvigo.rimass.thesaurus.mesh.MESHConverter tree <MESH tesaurus> <thesaurus.xml>");
        System.out.println("java -cp RIMASS.jar es.uvigo.rimass.thesaurus.mesh.MESHConverter graph <MESH tesaurus> <thesaurus.xml>");
    }

    public static void mainTest(String[] args) throws JAXBException {

        crearGrafoPrueba("/tmp/prueba.xml");

        MESHConverter converter = new MESHConverter();
        converter.convertMESHFileToTree("/tmp/prueba.xml", "/tmp/salidaTREE.xml");
        converter.convertMESHFileToGraph("/tmp/prueba.xml", "/tmp/salidaGRAPH.xml");
    }

    private static void crearGrafoPrueba(String salida) throws JAXBException {

        DescriptorRecordSet drs = new DescriptorRecordSet();

        DescriptorRecord r1 = crearDescriptorRecord("N0001", "nivel-1-a", "A01", null, null);
        DescriptorRecord r2 = crearDescriptorRecord("N0002", "nivel-1-b", "A02", null, null);
        DescriptorRecord r3 = crearDescriptorRecord("N0003", "nivel-2-a", "A01.01", null, null);
        DescriptorRecord r4 = crearDescriptorRecord("N0004", "nivel-2-b", "A01.02", null, null);
        DescriptorRecord r5 = crearDescriptorRecord("N0005", "nivel-2-c", "A01.03", null, null);
        DescriptorRecord r6 = crearDescriptorRecord("N0006", "nivel-2-d nivel-3-b", "A02.01", "A01.02.02", null);
        DescriptorRecord r7 = crearDescriptorRecord("N0007", "nivel-2-e", "A02.02", null, null);
        DescriptorRecord r8 = crearDescriptorRecord("N0008", "nivel-3-a", "A01.02.01", null, null);
        DescriptorRecord r9 = crearDescriptorRecord("N0009", "nivel-4-a nivel-3-c", "A01.02.02.01", "A02.01.01", null);

        drs.getDescriptorRecord().add(r1);
        drs.getDescriptorRecord().add(r2);
        drs.getDescriptorRecord().add(r3);
        drs.getDescriptorRecord().add(r4);
        drs.getDescriptorRecord().add(r5);
        drs.getDescriptorRecord().add(r6);
        drs.getDescriptorRecord().add(r7);
        drs.getDescriptorRecord().add(r8);
        drs.getDescriptorRecord().add(r9);

        JAXBContext contextThesaurus = JAXBContext.newInstance(DescriptorRecordSet.class);
        Marshaller m = contextThesaurus.createMarshaller();

        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(drs, new File(salida));

    }

    private static DescriptorRecord crearDescriptorRecord(String id, String nombre, String arbol1, String arbol2, String arbol3) {
        DescriptorName descriptorName;
        TreeNumberList treeNumberList;
        TreeNumber treeNumber;

        DescriptorRecord descriptorRecord = new DescriptorRecord();
        descriptorRecord.setDescriptorUI(id);
        descriptorName = new DescriptorName();
        descriptorName.setString(nombre);
        descriptorRecord.setDescriptorName(descriptorName);
        treeNumberList = new TreeNumberList();
        if (arbol1 != null) {
            treeNumber = new TreeNumber();
            treeNumber.setvalue(arbol1);
            treeNumberList.getTreeNumber().add(treeNumber);
        }
        if (arbol2 != null) {
            treeNumber = new TreeNumber();
            treeNumber.setvalue(arbol2);
            treeNumberList.getTreeNumber().add(treeNumber);
        }
        if (arbol3 != null) {
            treeNumber = new TreeNumber();
            treeNumber.setvalue(arbol3);
            treeNumberList.getTreeNumber().add(treeNumber);
        }

        descriptorRecord.setTreeNumberList(treeNumberList);
        /*
         * Concept concept = new Concept();
         *
         * concept.setConceptName(new ConceptName());
         * concept.getConceptName().setString("pepe");
         *
         *
         * descriptorRecord.setConceptList(new ConceptList());
         * descriptorRecord.getConceptList().getConcept().add(concept);
         */
        return descriptorRecord;

    }
}
