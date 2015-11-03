import java.util.ArrayList;
import java.util.Collection;

public class GlobalGraph extends Graph {
	public ArrayList<SentenceGraph> sentences;
	
	public GlobalGraph(){
		super();
		sentences = new ArrayList<SentenceGraph>();
	}
	
	public GlobalGraph(Collection<SentenceGraph> graphs){
		for(SentenceGraph graph : graphs){
			sentences.add(graph);
			add(graph);
		}
	}
	
	public void add(SentenceGraph sGraph){
		for(Node rootNode : sGraph.rootNodes){
			if(rootNode.hasChildren()){
				add(rootNode);
			}
		}
	}
	
	private void add(Node nodeToAdd){
		rootNodes.add(nodeToAdd);
	}
}
