import java.util.ArrayList;

import graph.GlobalGraph;
import graph.Node;
import graph.QuestionGraph;

public class QA {
	public QuestionGraph qGraph;
	public GlobalGraph gGraph;
	public ArrayList<Node> potentialAnswers;
	
	public QA(QuestionGraph qGraph, GlobalGraph gGraph){
		this.qGraph = qGraph;
		this.gGraph = gGraph;
		potentialAnswers = new ArrayList<Node>();
	}
	
	public void findAnswer(){
		findPotentialAnswers();
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
			potentialAnswers.add(node);
		}
	}
}
