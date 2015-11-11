package graph;
import java.util.ArrayList;
import java.util.Collection;

public class GlobalGraph extends Graph {
	private static final long serialVersionUID = -2762101244064956520L;
	public ArrayList<SentenceGraph> sentences;
	
	public GlobalGraph(){
		super();
		sentences = new ArrayList<SentenceGraph>();
	}
	
	public GlobalGraph(Collection<SentenceGraph> graphs){
		for(SentenceGraph graph : graphs){
			add(graph);
		}
	}
	
	public void add(SentenceGraph sGraph){
		sGraph.calculateDistancesToRoot();
		sentences.add(sGraph);

		for(Node rootNode : sGraph.rootNodes){
			add(rootNode);
		}
	}
	
	private void add(Node nodeToAdd){
		rootNodes.add(nodeToAdd);
	}
}
