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

public class MultiLevelQuestionTypeEM {
	private static Map<String, Parameters> questionTypeParams = new HashMap<String, Parameters>();
	private static List<QuestionGraph> topRanks = new ArrayList<QuestionGraph>();
	private static String basePath = "all-remedia-processed/";
	private static final int MAX_RANK = 0;
	
	public static void main(String[] args) throws IOException {
		Map<String, LevelParameters> paramList = new HashMap<String, LevelParameters>();
		Map<String, List<TrainingStruct>> trainingStructs = groupQuestionsByType();
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

		List<String> validationPaths = initValidationPaths();
		List<List<String>> validationAnswers = initValidationAnswerLists();
		int idx = 0, total = 0, success = 0;

		for(String path : validationPaths){
			Parser.readSetFromFile(path);

			List<String> answers = validationAnswers.get(idx);
			idx++;
			QuestionGraph[] qGraphs = Parser.getQuestionGraphs();
			GlobalGraph gGraph = Parser.getGlobalGraph();
			System.out.println();

			for(int i = 0; i < qGraphs.length; i++){
				QuestionGraph qGraph = qGraphs[i];
				String answer = answers.get(i);
				LevelParameters params = null;

				if(qGraph.getSentence().contains("Why") || qGraph.getSentence().contains("How")){
					continue;
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
		
		writeLevelParams(paramList);
	}

	private static void writeLevelParams(Map<String, LevelParameters> paramList) throws IOException {
		File outFile = new File("serializedParamList.ser");
		
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

		paths.add(basePath + "level2/rm2-13.ser");
		paths.add(basePath + "level2/rm2-15.ser");
		paths.add(String.format("%slevel3/rm3-1.ser", basePath));
		paths.add(String.format("%slevel3/rm3-10.ser", basePath));

		return paths;
	}

	private static List<List<String>> initValidationAnswerLists(){
		List<List<String>> answerLists = new ArrayList<List<String>>();

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

		answerLists.add(rm213Answers);
		answerLists.add(rm215Answers);
		answerLists.add(answers31);
		answerLists.add(answers310);

		return answerLists;
	}

	private static List<String> initPaths(){
		List<String> paths  = new ArrayList<String>();

		paths.add(basePath + "level2/rm2-1.ser");
		paths.add(basePath + "level2/rm2-2.ser");
		paths.add(basePath + "level2/rm2-3.ser");
		paths.add(basePath + "level2/rm2-4.ser");
		paths.add(basePath + "level2/rm2-5.ser");
		paths.add(basePath + "level2/rm2-6.ser");
		paths.add(basePath + "level2/rm2-7.ser");
		paths.add(basePath + "level2/rm2-8.ser");

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
		
		List<String> rm24Answers = new ArrayList<String>();
		rm24Answers.add("A young boy may have found the bones of a new kind of dinosaur .");
		rm24Answers.add("He said they must be from a dinosaur .");
		rm24Answers.add("The teeth and bones may have been on the ground for thousands or millions of years .");
		rm24Answers.add("They will be put in a safe place in a museum .");
		rm24Answers.add("WHY QUESTION");

		List<String> rm25Answers = new ArrayList<String>();
		rm25Answers.add("A man named Mister Sholes made his first typewriter 16 years ago .");
		rm25Answers.add("The machine is called a typewriter .");
		rm25Answers.add("A man named Mister Sholes made his first typewriter 16 years ago .");
		rm25Answers.add("-LRB- MILWAUKEE , WISCONSIN , June ,1873 -RRB- .");
		rm25Answers.add("WHY QUESTION");
		
		List<String> rm26Answers = new ArrayList<String>();
		rm26Answers.add("But a group of school children have cleaned up Pigeon Creek .");
		rm26Answers.add("But a group of school children have cleaned up Pigeon Creek .");
		rm26Answers.add("In 1983 , the creek was so dirty that the fish had all died .");
		rm26Answers.add("First they cleaned out the bottom of the creek , called the creek bed .");
		rm26Answers.add("WHY QUESTION");
		
		List<String> rm27Answers = new ArrayList<String>();
		rm27Answers.add("He says he lived alone on an island for four years and four months .");
		rm27Answers.add("He ate plums , crayfish , peppers , and turnips .");
		rm27Answers.add("Alex was rescued on February 12 .");
		rm27Answers.add("He lived in a cave .");
		rm27Answers.add("WHY QUESTION");

		List<String> rm28Answers = new ArrayList<String>();
		rm28Answers.add("You might be just the right age by then to be an astronaut .");
		rm28Answers.add("It will point laser beams at the surface and shoot .");
		rm28Answers.add("-LRB- Somewhere in outer space , March , 1989 -RRB- .");
		rm28Answers.add("If you were on this spacecraft , you would see strange sights on Mars .");
		rm28Answers.add("WHY QUESTION");


		answerLists.add(rm21Answers);
		answerLists.add(rm22Answers);
		answerLists.add(rm23Answers);
		answerLists.add(rm24Answers);
		answerLists.add(rm25Answers);
		answerLists.add(rm26Answers);
		answerLists.add(rm27Answers);
		answerLists.add(rm28Answers);

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
