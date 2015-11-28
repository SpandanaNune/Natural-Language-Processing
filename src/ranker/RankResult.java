package ranker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graph.SentenceGraph;

public class RankResult {
	private List<Double> sortedKeys;
	private Map<Double, List<SentenceGraph>> rankedGraphs;
	private Map<SentenceGraph, Integer> rankedGraphsBySentence;
	
	public RankResult(List<Double> sortedKeys2, Map<Double, List<SentenceGraph>> rankedGraphs){
		setSortedKeys(sortedKeys2);
		setRankedGraphs(rankedGraphs);
		setRankedGraphsBySentence(new HashMap<SentenceGraph, Integer>());
	}
	
	public RankResult() {
		setSortedKeys(new ArrayList<Double>());
		setRankedGraphs(new HashMap<Double, List<SentenceGraph>>());
		setRankedGraphsBySentence(new HashMap<SentenceGraph, Integer>());
	}
	
	public void removeGraph(SentenceGraph sGraph){
		if(rankedGraphsBySentence.containsKey(sGraph)){
			rankedGraphsBySentence.remove(sGraph);
			removeFromRankedGraph(sGraph);
		}
	}
	
	private void removeFromRankedGraph(SentenceGraph sGraph){
		for(Double key : sortedKeys){
			List<SentenceGraph> sGraphList = rankedGraphs.get(key);
		
			if(sGraphList.contains(sGraph)){
				sGraphList.remove(sGraph);
				rankedGraphs.replace(key, sGraphList);
				break;
			}
		}
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

		return Integer.MAX_VALUE;	
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
	
	public void calculateRankedGraphsBySentence(){
		for(Double key : rankedGraphs.keySet()){
			List<SentenceGraph> sGraphs = rankedGraphs.get(key);
			
			for(SentenceGraph sGraph : sGraphs){
				rankedGraphsBySentence.put(sGraph, getRank(sGraph.getSentence()));
			}
		}
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

	public Map<SentenceGraph, Integer> getRankedGraphsBySentence() {
		if(rankedGraphsBySentence == null){
			calculateRankedGraphsBySentence();
		}
		return rankedGraphsBySentence;
	}

	public void setRankedGraphsBySentence(Map<SentenceGraph, Integer> rankedGraphsBySentence) {
		this.rankedGraphsBySentence = rankedGraphsBySentence;
	}
}
