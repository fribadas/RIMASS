package es.uvigo.rimass.core;

import java.util.HashSet;
import java.util.Set;

import es.uvigo.rimass.core.tree.TreeRepresentation;

public class TreeStringRepresentation extends Representation{

	private Set<TreeRepresentation> trees = new HashSet<TreeRepresentation>();
	
	public TreeStringRepresentation() {
		super("tree");
	}

	public Set<TreeRepresentation> getTrees() {
		return trees;
	}

	public void setTrees(Set<TreeRepresentation> trees) {
		this.trees = trees;
	}

	public void addTree(TreeRepresentation tree) {
		trees.add(tree);
	}
}
