package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class Node implements Comparable<Node>{
	public Node parent;
	public String pos, relToParent, name, smallName, subclassOf, instanceOf;
	public TreeSet<Node> children;
	public int sentence, distanceToRoot;
	public boolean root;
	
	public Node(String name, String pos, int sentence, String relToParent){
		this.name = name;
		this.pos = pos;
		this.sentence = sentence;
		this.relToParent = relToParent;
		subclassOf = "NO SUBCLASS";
		instanceOf = "NO INSTANCE";
		children = new TreeSet<Node>();
		distanceToRoot = 0;
		smallName = name.split("-")[0].toLowerCase();
		
		if(relToParent == null){
			root = true;
		}
		else{
			root = false;
		}
	}
	
	public void addChild(Node childNode, String relToParent){
		children.add(childNode);
		childNode.addParent(this, relToParent);
		childNode.root = false;
	}
	
	public void addParent(Node parentNode, String relToParent){
		this.relToParent = relToParent;
		parent = parentNode;
	}
	
	public void makeRoot(){
		this.relToParent = null;
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
		
		sb.append(String.format("%d - %s | %s\n", distanceToRoot, name, relToParent));
		
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
	
	private int calculateSimilarityScore(Node o, int score){
		HashMap<String, ArrayList<Node>> otherRelations = new HashMap<String, ArrayList<Node>>();

		score += Node.distance(smallName, o.smallName);
		score += Math.abs(children.size() - o.children.size());
		
		if(!instanceOf.equals(o.instanceOf)){
			score += 1;
		}

		if(!instanceOf.equals(o.instanceOf)){
			score += 1;
		}
		
		for(Node oChild : o.children){
			if(!otherRelations.containsKey(oChild.relToParent)){
				otherRelations.put(oChild.relToParent, new ArrayList<Node>());
			}
			
			otherRelations.get(oChild.relToParent).add(oChild);
		}

		for(Node child : children){
			if(otherRelations.containsKey(child.relToParent)){
				int min = Integer.MAX_VALUE, temp, minIdx = -1;
				
				for(int i = 0; i < otherRelations.get(child.relToParent).size(); i++){
					Node oChild = otherRelations.get(child.relToParent).get(i);
					
					if((temp = child.calculateSimilarityScore(oChild)) < min){
						min = temp;
						minIdx = i;
					}
				}
				
				score += min;
				otherRelations.get(child.relToParent).remove(minIdx);
			}
			else{
				score += 1;
			}
		}
		
		return score;
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

	//Levenshtein distanace between strings
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
