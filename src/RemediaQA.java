import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import graph.GlobalGraph;
import graph.QuestionGraph;
import graph.SentenceGraph;
import params.LevelParameters;
import ranker.LevelRankResult;
import ranker.LevelRanker;
import ranker.RankResult;
import ranker.Ranker;

public class RemediaQA {
	private static String basePath = "all-remedia-processed/",
			parametersPath = "serializedParamList.ser";
	private static final int MAX_RANK = 5;

	public static void main(String[] args) throws FileNotFoundException{
		Map<String, LevelParameters> parameters = Parser.readLevelParamsFromFile(parametersPath);
		printParameters(parameters);
		Parser.readSetFromFile(new File(basePath + "level2/rm2-21.ser"));
		QuestionGraph[] qGraphs = Parser.getQuestionGraphs();
		GlobalGraph gGraph = Parser.getGlobalGraph();
		
		for(QuestionGraph qGraph : qGraphs){
			if(isWhyQuestion(qGraph) || isHowQuestion(qGraph)){
				continue;
			}
			
			if(qGraph.getAnswerType() == null){
				if(isWhoQuestion(qGraph)){
					qGraph.setAnswerType("person");
				}
				else{
					System.out.println(qGraph);
				}
			}

			LevelParameters params = parameters.get(qGraph.getAnswerType());
			LevelRanker ranker = new LevelRanker(params);
			LevelRankResult ranks = ranker.rankSentences(gGraph, qGraph);
			
			System.out.println(String.format("---Ranks for Question: %s---", qGraph));
			ranks.printRanks(gGraph, MAX_RANK);
		}
	}

	private static void printParameters(Map<String, LevelParameters> parameters) {
		for(String key : parameters.keySet()){
			System.out.println(String.format("Parameters for '%s':\n%s", key, parameters.get(key)));
		}
	}

	private static boolean isWhoQuestion(QuestionGraph qGraph){
		return qGraph.getSentence().contains("Who");
	}
	
	private static boolean isWhyQuestion(QuestionGraph qGraph){
		return qGraph.getSentence().contains("Why");
	}

	private static boolean isHowQuestion(QuestionGraph qGraph){
		return qGraph.getSentence().contains("How");
	}
}
