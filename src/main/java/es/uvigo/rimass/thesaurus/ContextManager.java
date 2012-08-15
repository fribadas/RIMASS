/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uvigo.rimass.thesaurus;

import java.util.Set;

/**
 *
 * @author ribadas
 */
public interface ContextManager {
    public Set<Descriptor> descendantsByContext(String parentContext);
    public String currentContext(String parentContext);
}
