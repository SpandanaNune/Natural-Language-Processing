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
		public String pos, relToParent, name;
		public TreeSet<Node> children;
		public int sentence, distanceToRoot;
		public boolean root;
		
		public Node(String name, String pos, int sentence, String relToParent){
			this.name = name;
			this.pos = pos;
			this.sentence = sentence;
			this.relToParent = relToParent;
			children = new TreeSet<Node>();
			distanceToRoot = Integer.MAX_VALUE;
			
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
			
			for(Node child : children){
				if(child.name.equals(childName)){
					result.child = child;
					
					if(!result.parentFound()){
						result.parent = child.hasDescendent(parentName);
					}
				}
				else if(child.name.equals(parentName)){
					result.parent = child;

					if(!result.childFound()){
						result.child = child.hasDescendent(childName);
					}
				}
				else if(result.noneFound()){
					result = child.hasDescendents(parentName, childName);
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
