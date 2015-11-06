package graph;


public class SearchResult{
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
