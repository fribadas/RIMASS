package es.uvigo.rimass.thesaurus.mesh;


import es.uvigo.rimass.thesaurus.ContextManager;
import es.uvigo.rimass.thesaurus.Descriptor;
import es.uvigo.rimass.thesaurus.DescriptorMetadata;
import es.uvigo.rimass.thesaurus.mesh.jaxb.DescriptorRecord;
import es.uvigo.rimass.thesaurus.mesh.jaxb.TreeNumber;
import es.uvigo.rimass.thesaurus.mesh.jaxb.TreeNumberList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.*;

/**
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"meshID", "meshTreeNumbers", "meshContexts"})
public class MESHMetadata extends DescriptorMetadata implements ContextManager {

    String meshID;
    @XmlElementWrapper
    @XmlElement(name = "meshTreeNumber")
    List<String> meshTreeNumbers; // TODO : ¿mejor como Strings separados por ; ?
    @XmlElementWrapper
    @XmlElement(name = "meshContext")
    List<MESHContext> meshContexts;

    public MESHMetadata() {
        super("MESH");
    }

    public MESHMetadata(DescriptorRecord r) {
        super("MESH");

        this.meshID = r.getDescriptorUI();

        this.meshTreeNumbers = new ArrayList<String>();

        this.meshContexts = new ArrayList<MESHContext>();

        TreeNumberList treeNumberList = r.getTreeNumberList();
        if (treeNumberList != null) {
            for (TreeNumber treeNumber : treeNumberList.getTreeNumber()) {
                String treeNumberLabel = treeNumber.getvalue();
                this.meshTreeNumbers.add(treeNumberLabel);

                MESHContext meshContext = new MESHContext();
                meshContext.setTreeNumber(treeNumberLabel);
                meshContext.setTreeDescendants(new HashSet<Descriptor>());
                this.meshContexts.add(meshContext);
            }
        }
    }

    public String getMeshID() {
        return meshID;
    }

    public void setMeshID(String meshID) {
        this.meshID = meshID;
    }

    public List<String> getMeshTreeNumbers() {
        return meshTreeNumbers;
    }

    public void setMeshTreeNumbers(List<String> meshTreeNumbers) {
        this.meshTreeNumbers = meshTreeNumbers;
    }

    public List<MESHContext> getMeshContexts() {
        return meshContexts;
    }

    public void setMeshContexts(List<MESHContext> meshContexts) {
        this.meshContexts = meshContexts;
    }

    public void addContextualLink(String parentTreeNumber, Descriptor descriptor) {
        for (MESHContext meshContext : this.meshContexts) {
            if (meshContext.getTreeNumber().equals(parentTreeNumber)) {
                meshContext.getTreeDescendants().add(descriptor);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String treeNumber : this.getMeshTreeNumbers()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(treeNumber);
        }
        sb.insert(0, '[');
        sb.append(']');
        return sb.toString();
    }

    public Set<Descriptor> descendantsByContext(String parentContext) {
        Set<Descriptor> result = null;
        if (this.meshContexts != null) {
            result = new HashSet<Descriptor>();
            for (MESHContext meshContext : this.meshContexts) { // TODO: revisar que tiene sentido devolver varios
                if (meshContext.getTreeNumber().startsWith(parentContext)) {
                    result.addAll(meshContext.getTreeDescendants());
                }
            }
            if (result.isEmpty()) {
                result = null;
            }
        }
        return result;
    }

    public String currentContext(String parentContext) { // TODO: puede devolver varios¿? (en los hijos de las raices)
        if (this.meshContexts != null) {
            for (MESHContext meshContext : this.meshContexts) {
                if (meshContext.getTreeNumber().startsWith(parentContext)) {
                    return meshContext.getTreeNumber();
                }
            }
        }
        return null;
    }
}
