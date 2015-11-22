import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import graph.GlobalGraph;
import graph.Node;
import graph.QuestionGraph;
import graph.SentenceGraph;

public class QA {
	public QuestionGraph qGraph;
	public GlobalGraph gGraph;
	public ArrayList<SentenceGraph> potentialAnswers;
	public HashMap<String, Double> parameters;
	public ArrayList<Double> sortedKeys;
	
	public QA(QuestionGraph qGraph, GlobalGraph gGraph, HashMap<String, Double> parameters){
		this.qGraph = qGraph;
		this.gGraph = gGraph;
		this.parameters = parameters;
		potentialAnswers = new ArrayList<SentenceGraph>();
		sortedKeys = new ArrayList<Double>();
	}
	
	public void findAnswer(){
		//findPotentialAnswers();
		HashMap<Double, ArrayList<SentenceGraph>> sentenceScores = rankSentences();
		ArrayList<Double> sortedKeys = new ArrayList<Double>();
		int index = 0;
		
		for(double key : sentenceScores.keySet()){
			sortedKeys.add(key);
		}
		
		Collections.sort(sortedKeys);
		
		for(double key : sortedKeys){
			index++;
			ArrayList<SentenceGraph> graphs = sentenceScores.get(key);
			for(SentenceGraph graph : graphs){
				System.out.println(String.format("GRAPH RANK %d\n%.2f: %s\n\n", index, key, graph.sentence));
			}
		}
	}
	
	public HashMap<Double, ArrayList<SentenceGraph>> rankSentences(){
		HashMap<Double, ArrayList<SentenceGraph>> rankedGraphs = new HashMap<Double, ArrayList<SentenceGraph>>();
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
	
	public void findPotentialAnswers(){
		for(Node rootNode : gGraph.rootNodes){
			findPotentialAnswers(rootNode);
		}
	}
	
	private void findPotentialAnswers(Node node){
		if(node.subclassOf.equals(qGraph.answerType) && node.parent != null){
			addToPotentialAnswers(node);
		}
		else{
			//SHOULD CHECK DIFFERENT TENSES!!!!
			for(Node qRoot : qGraph.rootNodes){
				if(qRoot.smallName.equals(node.smallName)){
					addToPotentialAnswers(node);
				}
			}
		}
		
		for(Node childNode : node.children){
			findPotentialAnswers(childNode);
		}
	}
	
	private void addToPotentialAnswers(Node node){
		if(!node.root){
			node = node.parent;
		}
		
		if(!potentialAnswers.contains(node)){
			potentialAnswers.add(gGraph.sentences.get(node.sentenceNum));
		}
	}
}
