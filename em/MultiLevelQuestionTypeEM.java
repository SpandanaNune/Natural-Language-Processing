import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graph.GlobalGraph;
import graph.QuestionGraph;
import graph.SentenceGraph;
import params.LevelParameters;
import params.Parameters;
import ranker.LevelRankResult;
import ranker.LevelRanker;
import ranker.RankResult;
import ranker.Ranker;
import utils.Parser;

public class MultiLevelQuestionTypeEM {
	private static Map<String, Parameters> questionTypeParams = new HashMap<String, Parameters>();
	private static List<QuestionGraph> topRanks = new ArrayList<QuestionGraph>();
	private static String basePath = "all-remedia-processed/";
	private static final int MAX_RANK = 5;

	public static void main(String[] args) throws IOException {
		Map<String, LevelParameters> paramList = new HashMap<String, LevelParameters>();

		for(int validationSet = 2; validationSet < 6; validationSet++){

			Map<String, List<TrainingStruct>> trainingStructs = groupQuestionsByType(validationSet);
			int totalQuestions = getNumQuestions(trainingStructs), numExcluded = 0;

			for(String key : trainingStructs.keySet()){
				List<TrainingStruct> structs = trainingStructs.get(key);

				if(isWhyQuestion(structs.get(0)) || isHowQuestion(structs.get(0))){
					numExcluded += structs.size();
					continue;
				}

				LevelParameters params = calculateParametersForList(structs);
				paramList.put(key, params);
				//Parameters nextParams = calculateParametersForList(structs, params);
			}

			int num = 0;
			for(String key : trainingStructs.keySet()){
				List<TrainingStruct> structs = trainingStructs.get(key);

				if(isWhyQuestion(structs.get(0)) || isHowQuestion(structs.get(0))){
					continue;
				}

				System.out.println("Failed Questions:\n");
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

			//VALIDATION
			Map<Integer, List<String>> validationMap = Parser.parseAnswerFile(validationSet);
			Map<Integer, String> paths = initPaths(validationSet, validationMap);
			int idx = 0, total = 0, success = 0;

			for(Integer key : paths.keySet()){
				String path = paths.get(key);
				List<String> answers = validationMap.get(key);

				Parser.readSetFromFile(path);

				idx++;
				QuestionGraph[] qGraphs = Parser.getQuestionGraphs();
				GlobalGraph gGraph = Parser.getGlobalGraph();
				System.out.println();

				for(int i = 0; i < qGraphs.length; i++){
					QuestionGraph qGraph = qGraphs[i];

					if(qGraph.getSentence().contains("Why") || qGraph.getSentence().contains("How")){
						continue;
					}

					String answer = answers.get(i);
					LevelParameters params = null;
					
					if(qGraph.getAnswerType() == null){
						updateAnswerType(qGraph);
					}

					if(paramList.containsKey(qGraph.getAnswerType())){
						params = paramList.get(qGraph.getAnswerType());		
					}
					else{
						System.out.println(qGraph);
						System.exit(0);
					}

					LevelRanker ranker = new LevelRanker(params);
					LevelRankResult result = ranker.rankSentences(gGraph, qGraph);

					int rank = result.getRank(answer);

					if(rank <= MAX_RANK){
						System.out.println(String.format("Success: %s", qGraph));
						success++;
					}
					else{
						System.out.println(String.format("Failure: %s", qGraph));
					}

					total++;
				}
			}

			System.out.println(String.format("\nTotal: %d", total));
			System.out.println(String.format("Success: %d", success));

			writeLevelParams(paramList, validationSet);

		}
	}

	private static void writeLevelParams(Map<String, LevelParameters> paramList, int validationSet) throws IOException {
		File outFile = new File(String.format("serializedParamList-%d.ser", validationSet));

		if(outFile.exists()){
			outFile.delete();
		}

		FileOutputStream fos = new FileOutputStream(outFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		oos.writeObject(paramList);
		oos.close();
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
			ret += trainingStructs.get(key).size();
		}

		return ret;
	}

	private static LevelParameters calculateParametersForList(List<TrainingStruct> trainingStructs, Parameters params) {
		Map<String, Double> paramVals = params.getParameters();
		Map<QuestionGraph, Integer> currRanks = null, lastRanks = null;
		LevelParameters lParams = new LevelParameters();
		int currLevel = 0;
		boolean update = false;

		do{
			if(update){
				currLevel++;
				params = new Parameters();
			}

			update = false;
			int currSuccessCount, lastSuccessCount;

			for(int k = 0; k < 3; k++){
				for(String key : Parameters.getKeys()){
					currRanks = calculateRankResults(trainingStructs, params);
					currSuccessCount = successCount(currRanks);
					Double currParamVal = paramVals.get(key);
					//System.out.println(key);

					do{
						//	System.out.println(currParamVal);
						currParamVal += .1;
						params.setParameter(key, currParamVal);
						lastRanks = currRanks;
						lastSuccessCount = currSuccessCount;
						currRanks = calculateRankResults(trainingStructs, params);
						currSuccessCount = successCount(currRanks);

					}while(currSuccessCount > lastSuccessCount || ranksImproved(currRanks, lastRanks));

					params.setParameter(key, currParamVal - .1);
				}
			}

			List<Double> factors = new ArrayList<Double>();

			for(TrainingStruct struct : trainingStructs){
				GlobalGraph newGGraph = null;

				for(double factor = 0; factor < 1; factor += .01){
					Ranker ranker = new Ranker(params);
					newGGraph = updateGlobalGraph(struct, params, (int) (struct.getGGraph().getSentences().size() * factor));
					RankResult res = ranker.rankSentences(struct.getQGraph(), newGGraph);

					if(newGGraph.contains(struct.getAnswer()) && res.getRank(struct.getAnswer()) > MAX_RANK){
						factors.add(factor);
						break;
					}
				}
			}

			double maxFactor = .65;

			if(!factors.isEmpty()){
				maxFactor = Collections.max(factors);
			}

			for(int i = 0; i < trainingStructs.size(); i++){
				TrainingStruct struct = trainingStructs.get(i);
				GlobalGraph newGGraph = null;

				newGGraph = updateGlobalGraph(struct, params, (int) Math.ceil((struct.getGGraph().getSentences().size() * maxFactor)));

				if(!newGGraph.toString().equals(struct.getGGraph().toString())){
					update = true;
					struct.setGGraph(newGGraph);
					trainingStructs.set(i, struct);
				}
			}

			lParams.setParamsForLevel(currLevel, params, maxFactor);
		}while(update);

		for(QuestionGraph key : lastRanks.keySet()){
			System.out.println(String.format("Question: %s\nAnswer Rank: %d\n", key, lastRanks.get(key)));
		}

		return lParams;
	}

	private static int successCount(Map<QuestionGraph, Integer> currRanks){
		int count = 0;

		for(QuestionGraph key : currRanks.keySet()){
			if(currRanks.get(key) <= MAX_RANK){
				count++;
			}
		}

		return count;
	}

	private static GlobalGraph updateGlobalGraph(TrainingStruct struct, Parameters params, int topRank){
		Ranker ranker = new Ranker(params);
		GlobalGraph newGGraph = new GlobalGraph(),
				gGraph = struct.getGGraph();
		QuestionGraph qGraph = struct.getQGraph();
		RankResult result = ranker.rankSentences(qGraph, gGraph);

		for(SentenceGraph sGraph : gGraph.getSentences()){
			int rank = result.getRank(sGraph);

			if(rank <= topRank){
				newGGraph.add(sGraph);
			}
		}

		return newGGraph;
	}

	private static LevelParameters calculateParametersForList(List<TrainingStruct> trainingStructs){
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
			System.out.println(String.format("Rank of answer for question '%s':\n%d", qGraph, rank));
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

	private static Map<Integer, String> initPaths(int level, Map<Integer, List<String>> answerMap){
		Map<Integer, String> paths  = new HashMap<Integer, String>();

		for(Integer key : answerMap.keySet()){
			paths.put(key, String.format("%slevel%d/rm%d-%d.ser", basePath, level, level, key));
		}

		return paths;
	}

	private static Map<String, List<TrainingStruct>> groupQuestionsByType(int exclude){
		Map<String, List<TrainingStruct>> trainingStructs = new HashMap<String, List<TrainingStruct>>();
		List<Integer> trainingSets = new ArrayList<Integer>();

		for(int i = 2; i < 6; i++){
			if(i == exclude){
				continue;
			}

			trainingSets.add(i);
		}

		Map<Integer, Map<Integer, List<String>>> answerMaps = Parser.parseAnswerFiles(trainingSets);

		for(Integer level : trainingSets){
			Map<Integer, List<String>> answerMap = answerMaps.get(level);
			Map<Integer, String> paths = initPaths(level, answerMap);

			for(Integer key : paths.keySet()){
				List<String> answers = answerMap.get(key);
				String path = paths.get(key);

				Parser.readSetFromFile(path);
				QuestionGraph[] qGraphs = Parser.getQuestionGraphs();
				GlobalGraph gGraph = Parser.getGlobalGraph();

				for(int j = 0; j < qGraphs.length; j++){
					QuestionGraph qGraph = qGraphs[j];

					if(qGraph.getSentence().contains("Why")){
						continue;
					}
					else if(qGraph.getSentence().contains("How")){
						continue;
					}

					String answer = answers.get(j);

					if(qGraph.getAnswerType() == null){
						updateAnswerType(qGraph);
					}

					if(!trainingStructs.containsKey(qGraph.getAnswerType())){
						trainingStructs.put(qGraph.getAnswerType(), new ArrayList<TrainingStruct>());
						questionTypeParams.put(qGraph.getAnswerType(), new Parameters());
					}

					trainingStructs.get(qGraph.getAnswerType()).add(new TrainingStruct(qGraph, answer, gGraph));
				}
			}
		}

		return trainingStructs;
	}

	private static void updateAnswerType(QuestionGraph qGraph){
		if(qGraph.getSentence().contains("Who")){
			qGraph.setAnswerType("person");
		}
		else if(qGraph.getSentence().contains("When")){
			qGraph.setAnswerType("time");
		}
		else if(qGraph.getSentence().contains("What")){
			qGraph.setAnswerType("object");
		}
		else{
			System.out.println(String.format("Answer type null for '%s': ", qGraph));
		}
	}
}
