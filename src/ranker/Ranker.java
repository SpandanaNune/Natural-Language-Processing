package ranker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graph.GlobalGraph;
import graph.QuestionGraph;
import graph.SentenceGraph;

public class Ranker {
	private Map<String, Double> parameters;
	
	public Ranker(Map<String, Double> parameters){
		setParameters(parameters);
	}

	public RankResult rankSentences(QuestionGraph qGraph, GlobalGraph gGraph){
		Map<Double, List<SentenceGraph>> rankedGraphs = new HashMap<Double, List<SentenceGraph>>();
		List<Double> sortedKeys = new ArrayList<Double>();
		
		for(SentenceGraph sGraph : gGraph.getSentences()){
			double score = calculateSimilarityScore(qGraph, sGraph); 
			
			if(!rankedGraphs.containsKey(score)){
				rankedGraphs.put(score, new ArrayList<SentenceGraph>());
			}
			
			rankedGraphs.get(score).add(sGraph);
		}
		
		sortedKeys.addAll(rankedGraphs.keySet());
		Collections.sort(sortedKeys);
		
		return new RankResult(sortedKeys, rankedGraphs);
	}
	
	public double calculateSimilarityScore(QuestionGraph qGraph, SentenceGraph sGraph){
			double score = 0;
			
			if(sGraph.containsSubclass(qGraph.getAnswerType())){
				score -= parameters.get("qaAnswerTypeFound");
			}
			else{
				score += parameters.get("qaAnswerTypeNotFound");
			}

			score += qGraph.calculateSimilarityScore(sGraph, parameters);
			
			return score;
	}

	//Getters & Setters
	
	public Map<String, Double> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Double> parameters) {
		this.parameters = parameters;
	}
}
