package graph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GlobalGraph extends Graph {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8324523544264354870L;
	private List<SentenceGraph> sentences;
	
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
	
	public List<SentenceGraph> getSentences(){
		return sentences;
	}
	
	public void setSentencnes(List<SentenceGraph> sentences){
		this.sentences = sentences;
	}
}
