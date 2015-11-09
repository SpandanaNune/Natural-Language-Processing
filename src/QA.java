import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import graph.GlobalGraph;
import graph.Node;
import graph.QuestionGraph;
import graph.SentenceGraph;

public class QA {
	public QuestionGraph qGraph;
	public GlobalGraph gGraph;
	public ArrayList<SentenceGraph> potentialAnswers;
	
	public QA(QuestionGraph qGraph, GlobalGraph gGraph){
		this.qGraph = qGraph;
		this.gGraph = gGraph;
		potentialAnswers = new ArrayList<SentenceGraph>();
	}
	
	public void findAnswer(){
		//findPotentialAnswers();
		HashMap<Integer, ArrayList<SentenceGraph>> sentenceScores = rankSentences();
		ArrayList<Integer> sortedKeys = new ArrayList<Integer>();
		int index = 0;
		
		for(int key : sentenceScores.keySet()){
			sortedKeys.add(key);
		}
		
		Collections.sort(sortedKeys);
		
		for(int key : sortedKeys){
			index++;
			ArrayList<SentenceGraph> graphs = sentenceScores.get(key);
			for(SentenceGraph graph : graphs){
				System.out.println(String.format("GRAPH RANK %d\n%d: %s\n\n", index, key, graph.sentence));
			}
		}
	}
	
	public HashMap<Integer, ArrayList<SentenceGraph>> rankSentences(){
		HashMap<Integer, ArrayList<SentenceGraph>> sentenceScores = new HashMap<Integer, ArrayList<SentenceGraph>>();
		int score;
		
		for(SentenceGraph sGraph : gGraph.sentences){
			score = qGraph.calculateSimilarityScore(sGraph);
			score = sGraph.containsSubclass(qGraph.answerType) ? score - 100 : score;
			
			if(!sentenceScores.containsKey(score)){
				sentenceScores.put(score, new ArrayList<SentenceGraph>());
			}
			
			sentenceScores.get(score).add(sGraph);
		}
		
		return sentenceScores;
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
	
	private void rankPotentialAnswers(){
		
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
