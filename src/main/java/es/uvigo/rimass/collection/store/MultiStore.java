package es.uvigo.rimass.collection.store;

import es.uvigo.rimass.collection.Collection;
import es.uvigo.rimass.collection.Configuration;
import es.uvigo.rimass.collection.Store;
import es.uvigo.rimass.core.Document;
import es.uvigo.rimass.core.Metadata;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ribadas
 */
public class MultiStore implements Store {

    private List<Store> stores;
    private Store retrieveStore;

    public MultiStore() {
        this.stores = new ArrayList<Store>();
    }

    public MultiStore(List<Store> stores) {
        this.stores = stores;
    }

    public List<Store> getStores() {
        return stores;
    }

    public void setStores(List<Store> stores) {
        this.stores = stores;
    }

    public void addStore(Store store) {
        if (this.stores == null) {
            this.stores = new ArrayList<Store>();
        }
        if (!stores.contains(store)) {
            this.stores.add(store);
        }
    }

    public void setRetrieveStore(Store store) {
        this.stores.add(store);
        this.retrieveStore = store;
    }

    public void initialize(Collection collection, Configuration configuration) {
        if (this.stores != null) {
            for (Store s : this.stores) {
                s.initialize(collection, configuration);
            }
        }
    }

    public void storeDocument(Document<? extends Metadata> document) {
        if (this.stores != null) {
            for (Store s : this.stores) {
                s.storeDocument(document);
            }
        }
    }

    public Document<? extends Metadata> retrieveDocument(long docid) {
        if (this.retrieveStore != null) {
            return retrieveStore.retrieveDocument(docid);
        } else {
            // Return first result (if any)
            Document<? extends Metadata> result = null;
            if (this.stores != null) {
                for (Store s : this.stores) {
                    result = s.retrieveDocument(docid);
                    if (result != null) {
                        break;  // Found
                    }
                }
            }
            return result;
        }
    }
}
