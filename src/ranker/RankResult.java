package ranker;

import java.util.List;
import java.util.Map;

import graph.SentenceGraph;

public class RankResult {
	private List<Double> sortedKeys;
	private Map<Double, List<SentenceGraph>> rankedGraphs;
	
	public RankResult(List<Double> sortedKeys2, Map<Double, List<SentenceGraph>> rankedGraphs){
		setSortedKeys(sortedKeys2);
		setRankedGraphs(rankedGraphs);
	}

	//Getters & Setters
	
	public List<Double> getSortedKeys() {
		return sortedKeys;
	}

	public void setSortedKeys(List<Double> sortedKeys) {
		this.sortedKeys = sortedKeys;
	}

	public Map<Double, List<SentenceGraph>> getRankedGraphs() {
		return rankedGraphs;
	}

	public void setRankedGraphs(Map<Double, List<SentenceGraph>> rankedGraphs) {
		this.rankedGraphs = rankedGraphs;
	}
}
