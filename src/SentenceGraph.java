import java.util.HashMap;

public class SentenceGraph extends Graph {
	private HashMap<String, String> posMap;
	
	public SentenceGraph(HashMap<String, String> posMap){
		super();
		this.posMap = posMap;
	}
	
	public void add(String parentName, String relationship, String childName, int sentenceNum){
		SearchResult searchResult = search(parentName, childName);
		Node childNode, parentNode;
		
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
	}
	
	private SearchResult search(String parentName, String childName){
		SearchResult result = new SearchResult();

		for(Node rootNode : rootNodes){
			if(rootNode.name.equals(childName)){
				result.child = rootNode;
				
				if(!result.parentFound()){
					result.parent = rootNode.hasDescendent(parentName);
				}
			}
			else if(rootNode.name.equals(parentName)){
				result.parent = rootNode;
				
				if(!result.childFound()){
					result.child = rootNode.hasDescendent(childName);
				}
			}
			else if(result.noneFound()){
				result = rootNode.hasDescendents(parentName, childName);
			}
			
			if(result.bothFound()){
				break;
			}
		}
		
		return result;
	}
}
