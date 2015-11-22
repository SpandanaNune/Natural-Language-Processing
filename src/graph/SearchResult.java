package graph;


public class SearchResult{
	private Node parent, child;
	
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
		String parentString = parentFound() ? parent.getName() : "null",
				childString = childFound() ? child.getName() : "null";
		
		return String.format("Parent: %s\nChild: %s\n", parentString, childString);
	}
	
	public Node getParent(){
		return parent;
	}
	
	public void setParent(Node parent){
		this.parent = parent;
	}

	public Node getChild(){
		return child;
	}
	
	public void setChild(Node child){
		this.child = child;
	}
	
	public void setParentName(String name){
		this.parent.setName(name);
	}

	public void setParentSemanticRole(String semanticRole){
		this.parent.setSemanticRole(semanticRole);
	}
}
