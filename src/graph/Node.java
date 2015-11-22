package graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import utils.CommonFunctions;

public class Node implements Comparable<Node>, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8630353347803378375L;
	private Node parent;
	private String pos, relToParent, name, smallName, originalName, subclassOf, instanceOf, semanticRole;
	private Set<Node> children;
	private int sentenceNum, distanceToRoot;
	private boolean root;
	private List<String> relations;

	public Node(String name, String pos, int sentenceNum, String relToParent){
		this.children = new TreeSet<Node>();
		this.relations = new ArrayList<String>();

		setSubclassOf("NO SUBCLASS");
		setInstanceOf("NO INSTANCE");
		setSentenceNum(sentenceNum);
		setPos(pos);
		setDistanceToRoot(0);
		setName(name);
		setRelToParent(relToParent);
	}

	//Tree Construction Methods
	
	public void addChild(Node childNode, String relToParent){
		children.add(childNode);
		childNode.addParent(this, relToParent);
		childNode.root = false;
		relations = null;
	}

	public void addParent(Node parentNode, String relToParent){
		parent = parentNode;
		setRelToParent(relToParent);
	}

	public void makeRoot(){
		this.relToParent = "NONE";
		this.root = true;
	}

	//Similarity Score Calculation Methods

	public int numCommonChildren(Node otherNode){
		int numCommon = 0;

		for(Node child : children){
			if(otherNode.children.contains(child)){
				numCommon++;
			}
		}

		return numCommon;
	}

	/* THE WEIGHT NEEDS TO BE DIFFERENT!
	 * 
	 * As it is now, the similarity score is disproportiontely influenced by the levenshtein
	 * distance between the smallNames of the two nodes. Each difference is given the same
	 * weight as a difference in the number of relations or the relation to the parent
	 * for the two nodes.
	 * 
	 * Also, taking the minimum score for the comparison between all the nodes is probably not best
	 * as it does not necessarily lead to the minimum score overall.
	 */
	public double calculateSimilarityScore(Node o, Map<String, Double> parameters){
		double score = 0;
		
		//NodDiffFactor Param
		int distance = CommonFunctions.distance(smallName, o.getSmallName());
		score += distance * parameters.get("nodeDiffFactor");

		//NumNodes Param
		int nodeDiff = numNodes() - o.numNodes();

		if(nodeDiff < 0){
			score += Math.abs(nodeDiff) * parameters.get("nodeLessNodes");
		}
		else if(nodeDiff > 0){
			score += Math.abs(nodeDiff) * parameters.get("nodeMoreNodes");
		}
		else{
			score -= parameters.get("nodeSameNumNodes");
		}

		//NodeLevel Param
		
		int distToRootDiff = distanceToRoot - o.getDistanceToRoot();
		
		if(distToRootDiff < 0){
			score += Math.abs(distToRootDiff) * parameters.get("nodeLowerLevel");
		}
		else if(distToRootDiff > 0){
			score += Math.abs(distToRootDiff) * parameters.get("nodeHigherLevel");
		}
		else{
			score -= parameters.get("nodeSameLevel");
		}

		//NodeSubclass Param
		
		if(subclassOf.equals(o.getSubclassOf())){
			score -= parameters.get("nodeSameSubclass");
		}
		else{
			score += parameters.get("nodeDiffSubclass");
		}

		//NodeInstance Param
		
		if(this.instanceOf.equals(o.instanceOf)){
			score -= parameters.get("nodeSameInstance");
		}
		else{
			score += parameters.get("nodeDiffInstance");
		}

		//NodeCommonRelations Param

		score -= numCommonRelations(o) * parameters.get("nodeCommonRelations");
		
		//NodeCommonChildren Param

		score -= numCommonChildren(o) * parameters.get("nodeCommonChildren");

		double tempNodeScore;
		Map<Node, Double> tempSimilarityScores = new HashMap<Node, Double>();
		List<Node> keySet = new ArrayList<Node>();
		Map<Node, SimilarityScores> similarityScores = new HashMap<Node, SimilarityScores>();

		for(Node cNode : children){
			for(Node oCNode : o.children){
				tempNodeScore = cNode.calculateSimilarityScore(oCNode, parameters);
				tempSimilarityScores.put(oCNode, tempNodeScore);

				if(!keySet.contains(oCNode)){
					keySet.add(oCNode);
				}
			}

			similarityScores.put(cNode, new SimilarityScores(tempSimilarityScores));
		}			

		return score + findMinSimilarityScore(similarityScores, keySet);
	}

	public static Node findMinScore(HashMap<Node, Integer> similarityScores){
		Node minNode = null;
		int minScore = Integer.MAX_VALUE;

		for(Node key : similarityScores.keySet()){
			if(similarityScores.get(key) < minScore){
				minScore = similarityScores.get(key);
				minNode = key;
			}
		}

		return minNode;
	}

	private double findMinSimilarityScore(Map<Node, SimilarityScores> similarityScores, List<Node> keySet){
		return findMinSimilarityScore(similarityScores, new ArrayList<Node>(), keySet);
	}

	private double findMinSimilarityScore(Map<Node, SimilarityScores> similarityScores, List<Node> usedNodes, List<Node> keySet){
		List<Double> totalScores = new ArrayList<Double>();

		for(int i = 0; i < keySet.size(); i++){
			totalScores.add(0.0);
		}

		SimilarityScores s, tempRemovedScores;
		Map<Node, SimilarityScores> tempScores;
		List<Node> tempKeySet;
		Node tempRemovedNode;

		for(Node r : similarityScores.keySet()){
			s = similarityScores.get(r);

			tempScores = CommonFunctions.cloneMap(similarityScores);
			tempRemovedNode = r;
			tempRemovedScores = similarityScores.get(r);
			tempScores.remove(r);

			for(int i = 0; i < totalScores.size(); i++){
				Node n = keySet.get(i);

				if(usedNodes.contains(n)){
					continue;
				}

				tempKeySet = CommonFunctions.cloneList(keySet);
				tempKeySet.remove(n);
				usedNodes.add(n);

				double tempScore = findMinSimilarityScore(tempScores, usedNodes, tempKeySet);
				usedNodes.remove(n);

				totalScores.set(i, totalScores.get(i) + s.getScores().get(n) + tempScore);
			}

			tempScores.put(tempRemovedNode, tempRemovedScores);

			usedNodes.remove(r);
		}

		return totalScores.size() == 0 ? 0 : Collections.min(totalScores);
	}

	//Search Methods

	public SearchResult hasDescendents(String parentName, String childName){
		SearchResult result = new SearchResult();
		boolean searchBoth;

		for(Node child : children){
			searchBoth = false;

			if(child.name.equals(childName)){
				result.setChild(child);
			}
			else if(child.name.equals(parentName)){
				result.setParent(child);
			}
			else if(result.noneFound()){
				searchBoth = true;
				result = child.hasDescendents(parentName, childName);
			}

			if(!searchBoth){
				if(result.parentFound() && !result.childFound()){
					result.setChild(child.hasDescendent(childName));
				}
				else if(result.childFound() &&!result.parentFound()){
					result.setParent(child.hasDescendent(parentName));
				}
			}

			if(result.bothFound()){
				break;
			}
		}

		return result;
	}

	public boolean containsSubclass(String subclass){
		boolean ret = false;

		if(subclassOf.equals(subclass)){
			ret = true;
		}

		if(!ret){
			for(Node child : children){
				ret = child.containsSubclass(subclass);
			}
		}

		return ret;
	}

	public Node hasDescendent(String descName){
		Node descendent = null;

		for(Node child : children){
			if(child.name.equals(descName)){
				descendent = child;
			}
			else{
				descendent = child.hasDescendent(descName);		
			}

			if(descendent != null){
				break;
			}
		}

		return descendent;
	}

	public boolean hasChildren(){
		return children.size() > 0;
	}

	//Override Object Methods

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < distanceToRoot; i++){
			sb.append("   ");
		}

		sb.append(String.format("%d - %s | %s | %s\n", distanceToRoot, name, relToParent, subclassOf));

		for(Node child : children){
			sb.append(String.format("%s", child));
		}

		return sb.toString();
	}

	//Comparable Interface

	@Override
	public int compareTo(Node o) {
		return name.compareTo(o.name);
	}

	//Misc

	public void calculateDistancesToRoot(){
		if(root){
			distanceToRoot = 0;
		}
		else{
			distanceToRoot = parent.distanceToRoot + 1;
		}

		for(Node child : children){
			child.calculateDistancesToRoot();
		}
	}

	public int numNodes(){
		return numNodes(0);
	}

	private int numNodes(int num){
		num++;

		for(Node c : children){
			num += c.numNodes(0);
		}

		return num;
	}

	public int numCommonRelations(Node otherNode){
		int numCommon = 0;

		for(Node child : children){
			for(Node oChild : otherNode.children){
				if(oChild.relToParent.equals(child.relToParent)){
					numCommon++;
				}
			}
		}

		return numCommon;
	}

	//Setters & Getters

	public void setSemanticRole(String semanticRole) {
		this.semanticRole = semanticRole;
	}

	public String getSemanticRole(){
		return this.semanticRole;
	}

	public int getDistanceToRoot(){
		return this.distanceToRoot;
	}

	public void setDistanceToRoot(int distanceToRoot){
		this.distanceToRoot = distanceToRoot;
	}

	public String getPos(){
		return this.pos;
	}

	public void setPos(String pos){
		this.pos = pos;
	}

	public int getSentenceNum(){
		return this.sentenceNum;
	}

	public void setSentenceNum(int sentenceNum){
		this.sentenceNum = sentenceNum;
	}

	public String getOriginalName(){
		return this.originalName;
	}

	public String getSubclassOf(){
		return this.subclassOf;
	}

	public void setSubclassOf(String subclassOf){
		this.subclassOf = subclassOf;
	}

	public String getInstanceOf(){
		return this.instanceOf;
	}

	public void setInstanceOf(String instanceOf){
		this.instanceOf = instanceOf;
	}

	public void setName(String name){
		this.name = name.toLowerCase();
		originalName = name;
		smallName = name.split("-")[0];
	}

	public String getName(){
		return this.name;
	}

	public String getSmallName(){
		return this.smallName;
	}

	public void setRelToParent(String relToParent){
		if(relToParent == null || relToParent.isEmpty()){
			root = true;
			this.relToParent = "NONE";
		}
		else{
			root = false;
			this.relToParent = relToParent;
		}
	}

	public String getRelToParent(){
		return this.relToParent;
	}

	public List<String> getRelations(){
		if(relations == null){
			relations = new ArrayList<String>();

			for(Node child : children){
				relations.add(child.relToParent);
			}
		}

		return relations;
	}
	
	public boolean getRoot(){
		return root;
	}
	
	public void setRoot(boolean root){
		this.root = root;
	}
}
