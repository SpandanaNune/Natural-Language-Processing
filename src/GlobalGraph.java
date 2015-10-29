import java.util.Collection;
import java.util.Hashtable;

public class GlobalGraph extends Graph {
	public GlobalGraph(){
		super();
	}
	
	public GlobalGraph(Collection<SentenceGraph> graphs){
		for(SentenceGraph graph : graphs){
			add(graph);
		}
	}
	
	public void add(SentenceGraph sGraph){
		for(Node rootNode : sGraph.rootNodes){
			add(rootNode);
		}
	}
	
	private void add(Node nodeToAdd){
		Hashtable<Integer, Node> commonChildCounts = new Hashtable<Integer, Node>();
		Node descendant;
		
		for(Node rootNode : rootNodes){
			if(rootNode.smallName.equals(nodeToAdd.smallName)){
				commonChildCounts.put(rootNode.commonChildren(nodeToAdd), rootNode);
			}
			
			if((descendant = rootNode.hasDescendent(nodeToAdd.smallName)) != null){
				commonChildCounts.put(descendant.commonChildren(nodeToAdd), descendant);
			}
		}
		for(Node child : nodeToAdd.children){
			
		}
	}
}
