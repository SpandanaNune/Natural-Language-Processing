package graph;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.CommonFunctions;

public class SentenceGraph extends Graph{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2055473139541405378L;
	private Map<String, String> posMap;
	private Map<String, Node> entityMap;
	private String sentence;
	private int sentenceNum;

	public SentenceGraph(Map<String, String> posMap, String sentence, int sentenceNum){
		super();
		setPosMap(posMap);
		setSentence(sentence);
		setSentenceNum(sentenceNum);

		this.entityMap = new HashMap<String, Node>();
	}

	public void add(String parentName, String relationship, String childName){
		SearchResult searchResult = search(parentName.toLowerCase(), childName.toLowerCase());
		Node childNode, parentNode;

		if(relationship.equals("has_coreferent")){
			String newName = childName.replace(":", "");
			searchResult.setParentName(newName);
			return;
		}
		else if(relationship.equals("semantic_rolw")){
			searchResult.setParentSemanticRole(childName);
			return;
		}

		if(relationship.equals("instance_of") || relationship.equals("is_subclass_of")){
			if(entityMap.containsKey(parentName)){
				parentNode = entityMap.get(parentName);
			}
			else if(searchResult.parentFound()){
				parentNode = searchResult.getParent();
				entityMap.put(childName, parentNode);
			}
			else{
				parentNode = new Node(parentName, posMap.get(parentName), sentenceNum, null);
				entityMap.put(childName, parentNode);
				rootNodes.add(parentNode);
			}

			if(relationship.equals("instance_of")){
				parentNode.setInstanceOf(childName);
			}
			else if(relationship.equals("is_subclass_of")){
				parentNode.setSubclassOf(childName);
			}


			return;
		}


		if(searchResult.noneFound()){
			parentNode = new Node(parentName, posMap.get(parentName), sentenceNum, null);
			childNode = new Node(childName, posMap.get(childName), sentenceNum, relationship);

			parentNode.addChild(childNode, relationship);
			rootNodes.add(parentNode);
		}
		else if(searchResult.bothFound()){
			parentNode = searchResult.getParent();
			childNode = searchResult.getChild();

			if(parentNode.getRoot() && childNode.getRoot()){
				rootNodes.remove(childNode);
				parentNode.addChild(childNode, relationship);
			}
			else if(!parentNode.getRoot() && childNode.getRoot()){
				parentNode.makeRoot();
				parentNode.addChild(childNode, relationship);

				rootNodes.remove(childNode);
				rootNodes.add(parentNode);
			}
			else if(parentNode.getRoot() && !childNode.getRoot()){
				//double check
				parentNode.addChild(childNode, relationship);
			}
			else{
				//double check
				parentNode.addChild(childNode, relationship);
			}
		}
		else if(searchResult.parentFound()){
			childNode = new Node(childName, posMap.get(childName), sentenceNum, relationship);
			parentNode = searchResult.getParent();

			if(parentNode.getRoot()){
				//double check
				parentNode.addChild(childNode, relationship);
			}
			else{
				//double check
				parentNode.addChild(childNode, relationship);
			}
		}
		else{
			childNode = searchResult.getChild();
			parentNode = new Node(parentName, posMap.get(parentName), sentenceNum, null);

			if(childNode.getRoot()){
				parentNode.makeRoot();
				parentNode.addChild(childNode, relationship);

				rootNodes.remove(childNode);
				rootNodes.add(parentNode);
			}
			else{
				parentNode.makeRoot();
				parentNode.addChild(childNode, relationship);

				rootNodes.add(parentNode);
			}
		}

		calculateDistancesToRoot();
	}

	private SearchResult search(String parentName, String childName){
		SearchResult result = new SearchResult();
		boolean searchBoth;

		for(Node rootNode : rootNodes){
			searchBoth = false;

			if(rootNode.getName().equals(childName)){
				result.setChild(rootNode);
			}
			else if(rootNode.getName().equals(parentName)){
				result.setParent(rootNode);
			}
			else if(result.noneFound()){
				searchBoth = true;
				result = rootNode.hasDescendents(parentName, childName);
			}

			if(!searchBoth){
				if(result.parentFound() && !result.childFound()){
					result.setChild(rootNode.hasDescendent(childName));
				}
				else if(result.childFound() &&!result.parentFound()){
					result.setParent(rootNode.hasDescendent(parentName));
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

		for(Node rootNode : rootNodes){
			if(rootNode.getSubclassOf().equals(subclass)){
				ret = true;
			}

			if(!ret){
				ret = rootNode.containsSubclass(subclass);
			}

			if(ret){
				break;
			}
		}

		return ret;
	}

	public double calculateSimilarityScore(SentenceGraph o, Map<String, Double> parameters) {
		HashMap<Node, SimilarityScores> similarityScores = new HashMap<Node, SimilarityScores>();
		HashMap<Node, Double> tempSimilarityScores = new HashMap<Node, Double>();
		ArrayList<Node> keySet = new ArrayList<Node>();

		int nodeDiff = numNodes() - o.numNodes();
		double score = 0, tempNodeScore;

		score += CommonFunctions.distance(sentence, o.getSentence()) * parameters.get("sentenceDiffFactor");

		if(nodeDiff < 0){
			score += Math.abs(nodeDiff) * parameters.get("sentenceLessNodes");
		}
		else if(nodeDiff > 0){
			score += Math.abs(nodeDiff) * parameters.get("sentenceMoreNodes");
		}
		else{
			score -= parameters.get("sentenceSameNumNodes");
		}

		for(Node rNode : rootNodes){
			for(Node oRNode : o.rootNodes){
				tempNodeScore = rNode.calculateSimilarityScore(oRNode, parameters);
				tempSimilarityScores.put(oRNode, tempNodeScore);

				if(!keySet.contains(oRNode)){
					keySet.add(oRNode);
				}
			}

			similarityScores.put(rNode, new SimilarityScores(tempSimilarityScores));
		}

		score -= numMatchingRoots(o) * parameters.get("sentenceNumMatchingRoots");
		return score + findMinSimilarityScore(similarityScores, keySet);
	}

	public int numNodes(){
		int num = 0;
		for(Node rNode : rootNodes){
			num += rNode.numNodes();
		}

		return num;
	}

	private int numMatchingRoots(SentenceGraph o){
		int numMatching = 0;

		for(Node rNode : rootNodes){
			for(Node oRNode : o.rootNodes){
				if(rNode.getSmallName().equals(oRNode.getSmallName())){
					numMatching += 1;
				}
			}
		}

		return numMatching;
	}

	private double findMinSimilarityScore(Map<Node, SimilarityScores> similarityScores, List<Node> keySet){
		return findMinSimilarityScore(similarityScores, new ArrayList<Node>(), keySet);
	}

	private double findMinSimilarityScore(Map<Node, SimilarityScores> similarityScores, List<Node> usedNodes, List<Node> keySet){
		ArrayList<Double> totalScores = new ArrayList<Double>();

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
	
	public String printNodes(){
		return super.toString();
	}

	@Override
	public boolean equals(Object o){
		SentenceGraph oGraph = (SentenceGraph) o;
		
		return sentence.equals(oGraph.sentence);
	}
	
	@Override
	public String toString(){
		return sentence;
	}


	//Setters & Getters

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public int getSentenceNum(){
		return this.sentenceNum;
	}

	public void setSentenceNum(int sentenceNum){
		this.sentenceNum = sentenceNum;
	}
	
	public Map<String, String> getPosMap(){
		return this.posMap;
	}

	public void setPosMap(Map<String, String> posMap) {
		this.posMap = posMap;
	}
}
