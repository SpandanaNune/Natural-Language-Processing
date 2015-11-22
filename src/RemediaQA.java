import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import graph.GlobalGraph;
import graph.QuestionGraph;
import graph.SentenceGraph;
import ranker.RankResult;
import ranker.Ranker;

public class RemediaQA {
	private static String basePath = "all-remedia-processed/";

	public static void main(String[] args) throws FileNotFoundException{
		HashMap<String, Double> parameters = getParameters();
		Ranker ranker = new Ranker(parameters);
		Parser.readSetFromFile(new File(basePath + "level2/rm2-1.ser"));
		QuestionGraph[] qGraphs = Parser.getQuestionGraphs();
		GlobalGraph gGraph = Parser.getGlobalGraph();
		
		System.out.println("Text:");
		
		for(SentenceGraph s : gGraph.getSentences()){
			System.out.println(s.getSentence());
		}
		
		for(QuestionGraph qGraph : qGraphs){
			RankResult ranks = ranker.rankSentences(qGraph, gGraph);
			
			for(Double key : ranks.getSortedKeys()){
				System.out.println(ranks.getRankedGraphs().get(key));
			}
		}
	}

	public static HashMap<String, Double> getParameters(){
		//{sentenceSameNumNodes=0.0, nodeMoreNodes=0.0, nodeLessNodes=0.0, nodeSameSubclass=0.0, nodeSameInstance=0.0, nodeDiffInstance=0.0, nodeCommonChildren=0.0, nodeDiffFactor=0.30000000000000004, nodeSameNumNodes=0.20000000000000004, qaAnswerTypeNotFound=0.0, sentenceNumMatchingRoots=0.1, sentenceLessNodes=0.1, nodeCommonRelations=0.0, sentenceMoreNodes=0.30000000000000004, nodeHigherLevel=0.0, sentenceDiffFactor=0.30000000000000004, nodeLowerLevel=0.0, nodeSameLevel=0.30000000000000004, nodeDiffSubclass=0.0, qaAnswerTypeFound=0.1}

		HashMap<String, Double> parameters = new HashMap<String, Double>();
		
		parameters.put("qaAnswerTypeFound", .1);
		parameters.put("qaAnswerTypeNotFound", 0.0);

		parameters.put("sentenceDiffFactor", 0.3);
		parameters.put("sentenceLessNodes", 0.1);
		parameters.put("sentenceMoreNodes", 0.3);
		parameters.put("sentenceSameNumNodes", 0.0);
		parameters.put("sentenceNumMatchingRoots", 0.1);
		
		parameters.put("nodeDiffFactor", 0.3);
		parameters.put("nodeLessNodes", 0.0);
		parameters.put("nodeMoreNodes", 0.0);
		parameters.put("nodeSameNumNodes", 0.2);
		parameters.put("nodeCommonChildren", 0.0);
		parameters.put("nodeCommonRelations", 0.0);
		parameters.put("nodeSameSubclass", 0.0);
		parameters.put("nodeDiffSubclass", 0.0);
		parameters.put("nodeSameInstance", 0.0);
		parameters.put("nodeDiffInstance", 0.0);
		parameters.put("nodeLowerLevel", 0.0);
		parameters.put("nodeHigherLevel", 0.0);
		parameters.put("nodeSameLevel", 0.3);
		
		return parameters;
	}
}
