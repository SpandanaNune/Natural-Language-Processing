import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graph.GlobalGraph;
import graph.QuestionGraph;
import graph.SentenceGraph;
import params.Parameters;
import ranker.RankResult;
import ranker.Ranker;

public class MultiLevelQuestionTypeEM {
	private static Map<String, Parameters> questionTypeParams = new HashMap<String, Parameters>();
	private static List<QuestionGraph> topRanks = new ArrayList<QuestionGraph>();
	private static String basePath = "all-remedia-processed/";
	private static int MAX_RANK = 0;

	public static void main(String[] args) {
		Map<String, List<TrainingStruct>> trainingStructs = groupQuestionsByType();
		int totalQuestions = getNumQuestions(trainingStructs), numExcluded = 0;

		for(String key : trainingStructs.keySet()){
			List<TrainingStruct> structs = trainingStructs.get(key);
			
			if(isWhyQuestion(structs.get(0)) || isHowQuestion(structs.get(0))){
				numExcluded += structs.size();
				continue;
			}

			Parameters params = calculateParametersForList(structs);
			//Parameters nextParams = calculateParametersForList(structs, params);
		}

		int num = 0;
		for(String key : trainingStructs.keySet()){
			List<TrainingStruct> structs = trainingStructs.get(key);

			if(isWhyQuestion(structs.get(0)) || isHowQuestion(structs.get(0))){
				continue;
			}
			
			for(TrainingStruct struct : structs){
				QuestionGraph qGraph = struct.getQGraph();
				
				if(!topRanks.contains(qGraph)){
					num++;
					System.out.println(String.format("%d. %s", num, qGraph));
				}
			}
		}
		
		totalQuestions = totalQuestions - numExcluded;
		
		System.out.println();
		System.out.println(String.format("Max allowed rank %d", MAX_RANK));
		System.out.println(String.format("Number in top ranks: %d", topRanks.size()));
		System.out.println(String.format("Total Questions: %d", totalQuestions));
		System.out.println(String.format("Percent correct: %.2f", (double)topRanks.size()/(double)totalQuestions * 100));

	}
	
	private static boolean isWhyQuestion(TrainingStruct struct){
		return struct.getQGraph().getSentence().contains("Why");
	}
	
	private static boolean isHowQuestion(TrainingStruct struct){
		return struct.getQGraph().getSentence().contains("How");
	}
	
	private static int getNumQuestions(Map<String, List<TrainingStruct>> trainingStructs){
		int ret = 0;
		
		for(String key : trainingStructs.keySet()){
			for(TrainingStruct s : trainingStructs.get(key)){
				ret++;
			}
		}
		
		return ret;
	}

	private static Parameters calculateParametersForList(List<TrainingStruct> trainingStructs, Parameters params) {
		Map<String, Double> paramVals = params.getParameters();
		Map<QuestionGraph, Integer> currRanks = null, lastRanks;
		
		for(String key : paramVals.keySet()){
			currRanks = calculateRankResults(trainingStructs, params);
			Double currParamVal = paramVals.get(key);
			//System.out.println(key);

			do{
			//	System.out.println(currParamVal);
				currParamVal += .1;
				params.setParameter(key, currParamVal);
				lastRanks = currRanks;
				currRanks = calculateRankResults(trainingStructs, params);
			}while(ranksImproved(currRanks, lastRanks));
			
			params.setParameter(key, currParamVal - .1);
		}
		
		for(QuestionGraph key : currRanks.keySet()){
			System.out.println(String.format("Question: %s\nAnswer Rank: %d\n", key, currRanks.get(key)));
		}

		return params;
	}

	private static Parameters calculateParametersForList(List<TrainingStruct> trainingStructs){
		return calculateParametersForList(trainingStructs, new Parameters());
	}

	private static Map<QuestionGraph, Integer> calculateRankResults(List<TrainingStruct> trainingStructs, Parameters params){
		Ranker ranker = new Ranker(params);
		Map<QuestionGraph, Integer> ranks = new HashMap<QuestionGraph, Integer>();

		for(TrainingStruct struct : trainingStructs){
			GlobalGraph gGraph = struct.getGGraph();
			QuestionGraph qGraph = struct.getQGraph();
			String answer = struct.getAnswer();
			RankResult result = ranker.rankSentences(qGraph, gGraph);

			int rank = result.getRank(answer);
			ranks.put(qGraph, rank);
		}

		return ranks;
	}

	private static boolean ranksImproved(Map<QuestionGraph, Integer> currRanks, Map<QuestionGraph, Integer> lastRanks){
		int change = 0;

		for(QuestionGraph key : currRanks.keySet()){
			if(topRanks.contains(key)){
				if(currRanks.get(key) > MAX_RANK){
					return false;
				}
				
				continue;
			}
			
			if(currRanks.get(key) <= MAX_RANK){
				topRanks.add(key);
			}

			change += currRanks.get(key) - lastRanks.get(key);
		}

		return change < 0;
	}

	private static List<String> initValidationPaths(){
		List<String> paths = new ArrayList<String>();

		paths.add(String.format("%slevel3/rm3-1.ser", basePath));
		paths.add(String.format("%slevel3/rm3-10.ser", basePath));

		return paths;
	}

	private static List<List<String>> initValidationAnswerLists(){
		List<List<String>> answerLists = new ArrayList<List<String>>();

		List<String> answers31 = new ArrayList<String>();
		answers31.add("At noon , two small children cut a ribbon .");
		answers31.add("It is called the Empire State Building .");
		answers31.add("-LRB- NEW YORK : May 1 , 1931 -RRB- .");
		answers31.add("-LRB- NEW YORK : May 1 , 1931 -RRB- .");
		answers31.add("They can see at least 50 miles away .");

		List<String> answers310 = new ArrayList<String>();
		answers310.add("The Walt Disney Show was the first TV show to air in color each week .");
		answers310.add("It was about the New York World 's Fair .");
		answers310.add("But not many of them were sold until about 1948 .");
		answers310.add("Each TV had to be hooked up to special wires underground .");
		answers310.add("They form the faces and buildings you see on your television .");


		answerLists.add(answers31);
		answerLists.add(answers310);

		return answerLists;
	}

	private static List<String> initPaths(){
		List<String> paths  = new ArrayList<String>();

		paths.add(basePath + "level2/rm2-1.ser");
		paths.add(basePath + "level2/rm2-2.ser");
		paths.add(basePath + "level2/rm2-3.ser");
		paths.add(basePath + "level2/rm2-13.ser");
		paths.add(basePath + "level2/rm2-15.ser");

		return paths;
	}

	private static List<List<String>> initAnswerLists(){
		List<List<String>> answerLists = new ArrayList<List<String>>();

		List<String> rm21Answers = new ArrayList<String>();
		rm21Answers.add("He is the same person that you read about in the book , Winnie the Pooh .");
		rm21Answers.add("When Chris was three years old , his father wrote a poem about him .");
		rm21Answers.add("Winnie the Pooh was written in 1925 .");
		rm21Answers.add("As a boy , Chris lived in a pretty home called Cotchfield Farm .");
		rm21Answers.add("They tell what it is like to be famous .");

		List<String> rm22Answers = new ArrayList<String>();
		rm22Answers.add("The last Pony Express rider leaves town today .");
		rm22Answers.add("Last year , the Pony Express was a new way to deliver the mail .");
		rm22Answers.add("Since April , 1860 , mail has been sent this way .");
		rm22Answers.add("Then the rider will stop at a swing station .");
		rm22Answers.add("It is called the telegraph .");

		List<String> rm23Answers = new ArrayList<String>();
		rm23Answers.add("The pledge was written by Frances Bellamy .");
		rm23Answers.add("We are named after this brave sailor .");
		rm23Answers.add("Four hundred years ago today , Christopher Columbus first saw our country .");
		rm23Answers.add("So , today , schools all over our land will display the flag .");
		rm23Answers.add("He wrote it so young people could feel proud of their land .");

		List<String> rm213Answers = new ArrayList<String>();
		rm213Answers.add("Today , a girl named Lynne Cox swam from the United States to Russia !");
		rm213Answers.add("To get ready for this swim , Lynne swam miles each day in ice-cold water .");
		rm213Answers.add("-LRB- RUSSIA , July , 1987 -RRB- .");
		rm213Answers.add("She left from an island in Alaska .");
		rm213Answers.add("They were ready to help if she needed it .");

		List<String> rm215Answers = new ArrayList<String>();
		rm215Answers.add("Baby Shamu 's mother is named Kandu .");
		rm215Answers.add("Her name is Baby Shamu .");
		rm215Answers.add("-LRB- ORLANDO , FLORIDA , September , 1985 -RRB- .");
		rm215Answers.add("She was born in a sea animal park called Sea World .");
		rm215Answers.add("Baby Shamu will be the first killer whale to grow up with people .");

		answerLists.add(rm21Answers);
		answerLists.add(rm22Answers);
		answerLists.add(rm23Answers);
		answerLists.add(rm213Answers);
		answerLists.add(rm215Answers);

		return answerLists;
	}

	private static Map<String, List<TrainingStruct>> groupQuestionsByType(){
		List<String> paths = initPaths();
		List<List<String>> answerLists = initAnswerLists();
		Map<String, List<TrainingStruct>> trainingStructs = new HashMap<String, List<TrainingStruct>>();

		for(int i = 0; i < paths.size(); i++){
			String path = paths.get(i);
			List<String> answers = answerLists.get(i);
			Parser.readSetFromFile(path);
			QuestionGraph[] qGraphs = Parser.getQuestionGraphs();
			GlobalGraph gGraph = Parser.getGlobalGraph();

			for(int j = 0; j < qGraphs.length; j++){
				QuestionGraph qGraph = qGraphs[j];
				String answer = answers.get(j);

				if(qGraph.getAnswerType() == null){
					if(qGraph.getSentence().contains("Who")){
						qGraph.setAnswerType("person");
					}
					else if(qGraph.getSentence().contains("Why")){
						qGraph.setAnswerType("reason");
					}
					else{
						System.out.println(String.format("Answer type null for '%s': ", qGraph));
					}
				}

				if(!trainingStructs.containsKey(qGraph.getAnswerType())){
					trainingStructs.put(qGraph.getAnswerType(), new ArrayList<TrainingStruct>());
					questionTypeParams.put(qGraph.getAnswerType(), new Parameters());
				}

				trainingStructs.get(qGraph.getAnswerType()).add(new TrainingStruct(qGraph, answer, gGraph));
			}
		}

		return trainingStructs;
	}
}
