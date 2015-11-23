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
	
	public int getRank(SentenceGraph sGraph){
		return getRank(sGraph.getSentence());
	}

	public int getRank(String sentence){
		int rank = 0;
		
		for(Double key : sortedKeys){
			List<SentenceGraph> sGraphList = rankedGraphs.get(key);
			
			for(SentenceGraph oGraph : sGraphList){
				if(oGraph.getSentence().equals(sentence)){
					rank += sGraphList.size() - 1;
					return rank;
				}
			}
			
			rank += sGraphList.size();
		}

		return -1;	
	}
	
	
	public double getScore(String sentence){
		for(Double key : sortedKeys){
			for(SentenceGraph sGraph : rankedGraphs.get(key)){
				if(sGraph.getSentence().equals(sentence)){
					return key;
				}
			}
		}

		return -1;
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
