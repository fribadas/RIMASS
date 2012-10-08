package es.uvigo.rimass.core.tree;

public class TreeNodeRelation {
	private TreeNode parent;
	private String relation;
	private TreeNode child;
	public TreeNode getParent() {
		return parent;
	}
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}
	public String getRelation() {
		return relation;
	}
	public void setRelation(String relation) {
		this.relation = relation;
	}
	public TreeNode getChild() {
		return child;
	}
	public void setChild(TreeNode child) {
		this.child = child;
	}
}
