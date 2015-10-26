import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Graph {
	public List<Node> rootNodes;
	private HashMap<String, String> posMap;
	
	public Graph(HashMap<String, String> posMap){
		rootNodes = new ArrayList<Node>();
		this.posMap = posMap;
	}
	
	public void add(String parentName, String relationship, String childName, int sentenceNum){
		Node parentNode, childNode;
		
		if((parentNode = search(parentName)) != null){
            if((childNode = search(childName)) != null){
                    int childNodeIndex;
                    
                    if((childNodeIndex = Collections.binarySearch(rootNodes, childNode)) > -1){
                    	childNode.relToParent = relationship;
                    	parentNode.addChild(childNode);
                        rootNodes.remove(parentNode);
                        rootNodes.set(childNodeIndex, parentNode);
                    }
                    else{
		            	childNode = new Node(childName, posMap.get(childName), sentenceNum, relationship);
						childNode.parent = parentNode;
					
						parentNode.addChild(childNode);
                    }
            }
            else{
            	childNode = new Node(childName, posMap.get(childName), sentenceNum, relationship);
				childNode.parent = parentNode;
			
				parentNode.addChild(childNode);
            }
		}
		else if((childNode = search(childName)) != null){
			int childNodeIndex;
			parentNode = new Node(parentName, posMap.get(parentName), sentenceNum, relationship);
			
			if((childNodeIndex = Collections.binarySearch(rootNodes, childNode)) != -1){
				parentNode.addChild(childNode);
				rootNodes.set(childNodeIndex, parentNode);
			}
		}
		else{
			parentNode = new Node(parentName, posMap.get(parentName), sentenceNum, null);
			childNode = new Node(childName, posMap.get(parentName), sentenceNum, relationship);
			
			childNode.parent = parentNode;
			parentNode.addChild(childNode);
			
			rootNodes.add(parentNode);
		}
	}
	
	private Node search(String name){
		Node retVal = null;
		
		for(Node rootNode : rootNodes){
			if(rootNode.name.equals(name)){
				return rootNode;
			}
			else{
				retVal = rootNode.hasDescendent(name);
				
				if(retVal != null){
					return retVal;
				}
			}
		}
		
		return retVal;
	}
}
