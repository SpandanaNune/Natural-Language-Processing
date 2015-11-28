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
	
	public boolean contains(String s){
		for(SentenceGraph sGraph : sentences){
			if(sGraph.getSentence().equals(s)){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean contains(SentenceGraph sGraph){
		return contains(sGraph.getSentence());
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < sentences.size(); i++){
			SentenceGraph sGraph = sentences.get(i);
			sb.append(sGraph.toString());
			
			if(i < sentences.size() - 1){
				sb.append("\n");
			}
		}
		
		return sb.toString();
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
