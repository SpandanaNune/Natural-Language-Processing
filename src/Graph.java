import java.util.TreeSet;

public abstract class Graph {
	public TreeSet<Node> rootNodes;
	
	public Graph(){
		rootNodes = new TreeSet<Node>();
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		for(Node rootNode : rootNodes){
			sb.append(String.format("%s\n\n", rootNode));
		}
		
		return sb.toString();
	}
	
	protected void calculateDistancesToRoot(){
		for(Node rootNode : rootNodes){
			rootNode.calculateDistancesToRoot();
		}
	}
	
	protected class Node implements Comparable<Node>{
		public Node parent;
		public String pos, relToParent, name, smallName;
		public TreeSet<Node> children;
		public int sentence, distanceToRoot;
		public boolean root;
		
		public Node(String name, String pos, int sentence, String relToParent){
			this.name = name;
			this.pos = pos;
			this.sentence = sentence;
			this.relToParent = relToParent;
			children = new TreeSet<Node>();
			distanceToRoot = 0;
			smallName = name.split("-")[0];
			
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
		
		private void calculateDistancesToRoot(){
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
	}
	
	protected class SearchResult{
		public Node parent, child;
		
		public SearchResult(){
			parent = null;
			child = null;
		}
		
		public boolean bothFound(){
			return parentFound() && childFound();
		}
		
		public boolean noneFound(){
			return parent == null && child == null;
		}
		
		public boolean parentFound(){
			return parent != null;
		}
		
		public boolean childFound(){
			return child != null;
		}
		
		@Override
		public String toString(){
			String parentString = parentFound() ? parent.name : "null",
					childString = childFound() ? child.name : "null";
			
			return String.format("Parent: %s\nChild: %s\n", parentString, childString);
		}
	}

}
