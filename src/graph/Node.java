package graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class Node implements Comparable<Node>, Serializable{
	private static final long serialVersionUID = -7290263771084961128L;
	public Node parent;
	public String pos, relToParent, name, smallName, originalName, subclassOf, instanceOf;
	public TreeSet<Node> children;
	public int sentenceNum, distanceToRoot;
	public boolean root;
	
	public Node(String name, String pos, int sentenceNum, String relToParent){
		this.pos = pos;
		this.sentenceNum = sentenceNum;
		subclassOf = "NO SUBCLASS";
		instanceOf = "NO INSTANCE";
		children = new TreeSet<Node>();
		distanceToRoot = 0;
		setName(name);
		setRelToParent(relToParent);
	}
	
	public void setName(String name){
		this.name = name.toLowerCase();
		originalName = name;
		smallName = name.split("-")[0];
	}
	
	public void addChild(Node childNode, String relToParent){
		children.add(childNode);
		childNode.addParent(this, relToParent);
		childNode.root = false;
	}
	
	public void addParent(Node parentNode, String relToParent){
		parent = parentNode;
		setRelToParent(relToParent);
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
	
	public void makeRoot(){
		this.relToParent = "NONE";
		this.root = true;
	}
	
	public SearchResult hasDescendents(String parentName, String childName){
		SearchResult result = new SearchResult();
		boolean searchBoth;
		
		for(Node child : children){
			searchBoth = false;
			
			if(child.name.equals(childName)){
				result.child = child;
			}
			else if(child.name.equals(parentName)){
				result.parent = child;
			}
			else if(result.noneFound()){
				searchBoth = true;
				result = child.hasDescendents(parentName, childName);
			}
			
			if(!searchBoth){
				if(result.parentFound() && !result.childFound()){
					result.child = child.hasDescendent(childName);
				}
				else if(result.childFound() &&!result.parentFound()){
					result.parent = child.hasDescendent(parentName);
				}
			}
			
			if(result.bothFound()){
				break;
			}
		}
		
		return result;
	}
	
	public ArrayList<String> relations(){
		ArrayList<String> retVal = new ArrayList<String>();
		
		for(Node child : children){
			retVal.add(child.relToParent);
		}
		
		return retVal;
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
	
	public int commonChildren(Node otherNode){
		int numCommon = 0;
		
		for(Node child : children){
			if(otherNode.children.contains(child)){
				numCommon++;
			}
		}
		
		return numCommon;
	}

	@Override
	public int compareTo(Node o) {
		return name.compareTo(o.name);
	}
	
	public int calculateSimilarityScore(Node o){
		return calculateSimilarityScore(o, 0);
	}
	
	/*
	 * THE WEIGHT NEEDS TO BE DIFFERENT!
	 * 
	 * As it is now, the similarity score is disproportiontely influenced by the levenshtein
	 * distance between the smallNames of the two nodes. Each difference is given the same
	 * weight as a difference in the number of relations or the relation to the parent
	 * for the two nodes.
	 * 
	 * Also, taking the minimum score for the comparison between all the nodes is probably not best
	 * as it does not necessarily lead to the minimum score overall.
	 */
	private int calculateSimilarityScore(Node o, int score){
		HashMap<Node, Integer> similarityScores;
		int distance;
		ArrayList<Node> usedChildren = new ArrayList<Node>();
		ArrayList<String> sRelations, lRelations;
		Node minNode, larger, smaller;
		
		larger = children.size() >= o.children.size() ? this : o;
		lRelations = larger.relations();
		
		smaller = o.children.size() <= children.size() ? o : this;
		sRelations = smaller.relations();

		//Difference in the number of relations 
		score += Math.abs(lRelations.size() - sRelations.size());

		//Number of changes needed to get from smallName to o.smallName
		distance = distance(smallName, o.smallName);
		
		if(distance == 0 && root && o.root){
			score -= 100;
		}
		else{
			score += distance;
		}
		

		//If relationships to parent are not the same add 1
		score += relToParent.equals(o.relToParent) ? 0 : 1;
		
		for(Node lChild : larger.children){
			similarityScores = new HashMap<Node, Integer>();
			
			for(Node sChild : smaller.children){
				if(!usedChildren.contains(sChild)){
					similarityScores.put(sChild, lChild.calculateSimilarityScore(sChild));
				}
			}
			
			if(similarityScores.size() > 0){
				minNode = findMinScore(similarityScores);
				score += similarityScores.get(minNode);
				usedChildren.add(minNode);
			}
		}
		
		return score;
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

	//Levenshtein distance between strings
	//From http://rosettacode.org/wiki/Levenshtein_distance#Java
    private static int distance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
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
}
