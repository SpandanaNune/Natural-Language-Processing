package ranker;

import java.util.ArrayList;
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

	public Map<Double, List<SentenceGraph>> rankSentences(QuestionGraph qGraph, GlobalGraph gGraph){
		Map<Double, List<SentenceGraph>> rankedGraphs = new HashMap<Double, List<SentenceGraph>>();
		double score;
		
		for(SentenceGraph sGraph : gGraph.sentences){
			score = 0;
			
			if(sGraph.containsSubclass(qGraph.answerType)){
				score -= parameters.get("qaAnswerTypeFound");
			}
			else{
				score += parameters.get("qaAnswerTypeNotFound");
			}

			score += qGraph.calculateSimilarityScore(sGraph, parameters);
			
			if(!rankedGraphs.containsKey(score)){
				rankedGraphs.put(score, new ArrayList<SentenceGraph>());
				sortedKeys.add(score);
			}
			
			rankedGraphs.get(score).add(sGraph);
		}
		
		Collections.sort(sortedKeys);
		

		
		return rankedGraphs;
	}

	//Getters & Setters
	
	public Map<String, Double> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Double> parameters) {
		this.parameters = parameters;
	}
}
