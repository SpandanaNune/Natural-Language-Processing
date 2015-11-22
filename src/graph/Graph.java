package graph;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

public abstract class Graph implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7482168913523508112L;
	protected Set<Node> rootNodes;
	
	public Graph(){
		rootNodes = new TreeSet<Node>();
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		int i = 1;
		
		for(Node rootNode : rootNodes){
			sb.append(String.format("Disconnected Sub-graph %d:\n", i));
			sb.append(String.format("%s\n\n", rootNode));
			i++;
		}
		
		return sb.toString();
	}
	
	protected void calculateDistancesToRoot(){
		for(Node rootNode : rootNodes){
			rootNode.calculateDistancesToRoot();
		}
	}
	
	public Set<Node> getRootNodes(){
		return this.rootNodes;
	}
	
	public void setRootNodes(Set<Node> rootNodes){
		this.rootNodes = rootNodes;
	}
}
