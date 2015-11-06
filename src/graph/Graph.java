package graph;
import java.util.TreeSet;

public abstract class Graph {
	public TreeSet<Node> rootNodes;
	
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
}
