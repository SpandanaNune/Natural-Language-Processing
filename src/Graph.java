import java.util.TreeSet;

public abstract class Graph {
	public TreeSet<Node> rootNodes;
	
	public Graph(){
		rootNodes = new TreeSet<Node>();
	}
	
	protected class Node implements Comparable<Node>{
		public Node parent;
		public String pos, relToParent, name;
		public TreeSet<Node> children;
		public int sentence;
		public boolean root;
		
		public Node(String name, String pos, int sentence, String relToParent){
			this.name = name;
			this.pos = pos;
			this.sentence = sentence;
			this.relToParent = relToParent;
			children = new TreeSet<Node>();
			
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
			/*
			 * Indentation not correct!!!!
			 */
			/*StringBuilder sb = new StringBuilder();
			sb.append(String.format("(%s: (POS-%s, REL-%s))", name, pos, relToParent));
			
			for(Node child : children){
				sb.append(String.format("\n\t%s", child));
			}
			
			return sb.toString();*/
			
			return name;
		}

		@Override
		public int compareTo(Node o) {
			return name.compareTo(o.name);
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
	}

}
