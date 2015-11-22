package graph;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import graph.SentenceGraph.SimilarityScores;

public class SentenceGraph extends Graph implements Serializable{
	private static final long serialVersionUID = 1106842163611517027L;
	private HashMap<String, String> posMap;
	private HashMap<String, Node> entityMap;
	public String sentence;
	public int sentenceNum;
	
	public SentenceGraph(HashMap<String, String> posMap, String sentence, int sentenceNum){
		super();
		this.posMap = posMap;
		this.entityMap = new HashMap<String, Node>();
		this.sentence = sentence;
		this.sentenceNum = sentenceNum;
	}
	
	public void add(String parentName, String relationship, String childName){
		SearchResult searchResult = search(parentName.toLowerCase(), childName.toLowerCase());
		Node childNode, parentNode;
		
		if(relationship.equals("has_coreferent")){
			String newName = childName.replace(":", "");
			searchResult.parent.setName(newName);
			return;
		}
		
		if(relationship.equals("instance_of") || relationship.equals("is_subclass_of")){
			if(entityMap.containsKey(parentName)){
				parentNode = entityMap.get(parentName);
			}
			else if(searchResult.parentFound()){
				parentNode = searchResult.parent;
				entityMap.put(childName, parentNode);
			}
			else{
				parentNode = new Node(parentName, posMap.get(parentName), sentenceNum, null);
				entityMap.put(childName, parentNode);
				rootNodes.add(parentNode);
			}

	        if(relationship.equals("instance_of")){
	                parentNode.instanceOf = childName;
	        }
	        else if(relationship.equals("is_subclass_of")){
	                parentNode.subclassOf = childName;
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
			parentNode = searchResult.parent;
			childNode = searchResult.child;
			
			if(parentNode.root && childNode.root){
				rootNodes.remove(childNode);
				parentNode.addChild(childNode, relationship);
			}
			else if(!parentNode.root && childNode.root){
				parentNode.makeRoot();
				parentNode.addChild(childNode, relationship);

				rootNodes.remove(childNode);
				rootNodes.add(parentNode);
			}
			else if(parentNode.root && !childNode.root){
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
			parentNode = searchResult.parent;
			
			if(parentNode.root){
				//double check
				parentNode.addChild(childNode, relationship);
			}
			else{
				//double check
				parentNode.addChild(childNode, relationship);
			}
		}
		else{
			childNode = searchResult.child;
			parentNode = new Node(parentName, posMap.get(parentName), sentenceNum, null);

			if(childNode.root){
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

		//calculateDistancesToRoot();
	}
	
	private SearchResult search(String parentName, String childName){
		SearchResult result = new SearchResult();
		boolean searchBoth;
		
		for(Node rootNode : rootNodes){
			searchBoth = false;
			
			if(rootNode.name.equals(childName)){
				result.child = rootNode;
			}
			else if(rootNode.name.equals(parentName)){
				result.parent = rootNode;
			}
			else if(result.noneFound()){
				searchBoth = true;
				result = rootNode.hasDescendents(parentName, childName);
			}

			if(!searchBoth){
				if(result.parentFound() && !result.childFound()){
					result.child = rootNode.hasDescendent(childName);
				}
				else if(result.childFound() &&!result.parentFound()){
					result.parent = rootNode.hasDescendent(parentName);
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
			if(rootNode.subclassOf.equals(subclass)){
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

	public double calculateSimilarityScore(SentenceGraph o, HashMap<String, Double> parameters) {
		HashMap<Node, SimilarityScores> similarityScores = new HashMap<Node, SimilarityScores>();
		HashMap<Node, Double> tempSimilarityScores = new HashMap<Node, Double>();
		ArrayList<Node> keySet = new ArrayList<Node>();

		int nodeDiff = numNodes() - o.numNodes();
		double score = 0, tempNodeScore;
		
		score += distance(o) * parameters.get("sentenceDiffFactor");
		
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
				if(rNode.smallName.equals(oRNode.smallName)){
					numMatching += 1;
				}
			}
		}
		
	return numMatching;
	}
	
	private double findMinSimilarityScore(HashMap<Node, SimilarityScores> similarityScores, ArrayList<Node> keySet){
		return findMinSimilarityScore(similarityScores, new ArrayList<Node>(), keySet);
	}
	
	private double findMinSimilarityScore(HashMap<Node, SimilarityScores> similarityScores, ArrayList<Node> usedNodes, ArrayList<Node> keySet){
		ArrayList<Double> totalScores = new ArrayList<Double>();
		
		for(int i = 0; i < keySet.size(); i++){
			totalScores.add(0.0);
		}
		
		SimilarityScores s, tempRemovedScores;
		HashMap<Node, SimilarityScores> tempScores;
		ArrayList<Node> tempKeySet;
		Node tempRemovedNode;
		
		for(Node r : similarityScores.keySet()){
			s = similarityScores.get(r);

			tempScores = cloneScores(similarityScores);
			tempRemovedNode = r;
			tempRemovedScores = similarityScores.get(r);
			tempScores.remove(r);
		
			for(int i = 0; i < totalScores.size(); i++){
				Node n = keySet.get(i);

                if(usedNodes.contains(n)){
                        continue;
                }

				tempKeySet = cloneList(keySet);
				tempKeySet.remove(n);
                usedNodes.add(n);

                double tempScore = findMinSimilarityScore(tempScores, usedNodes, tempKeySet);
                usedNodes.remove(n);

				totalScores.set(i, totalScores.get(i) + s.scores.get(n) + tempScore);
			}
			
			tempScores.put(tempRemovedNode, tempRemovedScores);
			
			usedNodes.remove(r);
		}
		
		return totalScores.size() == 0 ? 0 : Collections.min(totalScores);
	}
	
	private HashMap<Node, SimilarityScores> cloneScores(HashMap<Node, SimilarityScores> scores){
		HashMap<Node, SimilarityScores> newScores = new HashMap<Node, SimilarityScores>();
		
		for(Node k : scores.keySet()){
			newScores.put(k, scores.get(k));
		}
		
		return newScores;
	}
	
	private ArrayList<Node> cloneList(ArrayList<Node> list){
		ArrayList<Node> newList = new ArrayList<Node>();
		
		for(Node n : list){
			newList.add(n);
		}
		
		return newList;
	}
	
	//Levenshtein distance between strings
	//From http://rosettacode.org/wiki/Levenshtein_distance#Java
    public int distance(SentenceGraph other) {
        String a = sentence.toLowerCase();
        String b = other.sentence.toLowerCase();
        // i == 0
        int [] costs = new int [b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }
	
    protected class SimilarityScores implements Comparable<SimilarityScores>{
    	public double totalScore;
    	public HashMap<Node, Double> scores;
    	public ArrayList<Node> keySet;
    	
    	public SimilarityScores(HashMap<Node, Double> similarityScores){
    		totalScore = 0;
    		scores = similarityScores;
    		keySet = new ArrayList<Node>();
    		
    		for(Node n : similarityScores.keySet()){
    			keySet.add(n);
    			totalScore += similarityScores.get(n);
    		}
    	}
    	
		@Override
		public int compareTo(SimilarityScores o) {
			return Double.compare(totalScore, o.totalScore);
		}	
    }
}
