package es.uvigo.rimass.core.tree;

import java.util.HashMap;

public class TreeRepresentation {
	
	private TreeNode root;
	private HashMap<Long, TreeNode> nodeMap = new HashMap<Long, TreeNode>();
	
	public TreeNode getRoot() {
		return root;
	}
	
	public void setRoot(TreeNode root) {
		this.root = root;
	}
	
	public HashMap<Long, TreeNode> getNodeMap() {
		return nodeMap;
	}
	
	public void setNodeMap(HashMap<Long, TreeNode> nodeMap) {
		this.nodeMap = nodeMap;
	}
	
	public TreeNode addNode(Long parentIndex, Long index, String modifier, String name){
		TreeNode parentNode = nodeMap.get(parentIndex);
		if (parentNode == null) throw new RuntimeException("Father unknown");
		
		TreeNode newNode = new TreeNode();
		newNode.setIndex(index);
		newNode.setName(name);
		
		nodeMap.put(index, newNode);
		parentNode.addChildren(parentNode, modifier, newNode);
		return newNode;
	}
	
	public TreeNode setRoot(Long index, String name){
		TreeNode node = new TreeNode();
		node.setIndex(index);
		node.setName(name);
		this.root = node;
		nodeMap.put(index, node);
		
		return root;
	}
}
