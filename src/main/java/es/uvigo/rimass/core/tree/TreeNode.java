package es.uvigo.rimass.core.tree;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
	private Long index;
	private String name;
	private List<TreeNodeRelation> children;
	
	public TreeNode() {
		children = new ArrayList<TreeNodeRelation>();
	}
	
	public Long getIndex() {
		return index;
	}
	public void setIndex(Long index) {
		this.index = index;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<TreeNodeRelation> getChildren() {
		return children;
	}
	public void setChildren(List<TreeNodeRelation> children) {
		this.children = children;
	}
	public void addChildren(TreeNode parent, String modifier, TreeNode newNode) {
		TreeNodeRelation relation = new TreeNodeRelation();
		relation.setParent(parent);
		relation.setChild(newNode);
		relation.setRelation(modifier);
		children.add(relation);
	}
}
