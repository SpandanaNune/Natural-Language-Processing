package ranker;

import graph.GlobalGraph;
import graph.QuestionGraph;
import graph.SentenceGraph;
import params.LevelParameters;
import params.Parameters;

public class LevelRanker {
	private LevelParameters levelParams;

	public LevelRanker(LevelParameters params){
		setLevelParams(params);
	}

	public LevelRankResult rankSentences(GlobalGraph gGraph, QuestionGraph qGraph){
		LevelRankResult retVal = new LevelRankResult();
		int count = 0, numKeys = levelParams.getLevelParams().keySet().size();
		Parameters params;
		Ranker ranker;
		RankResult result;
		

		for(Integer key : levelParams.getLevelParams().keySet()){
			params = levelParams.get(key);
			ranker = new Ranker(params);
			result = ranker.rankSentences(qGraph, gGraph);

			result.calculateRankedGraphsBySentence();

			GlobalGraph newGGraph = updateGlobalGraph(gGraph, result, key);
			RankResult updatedResult = ranker.rankSentences(qGraph, gGraphDiff(newGGraph, gGraph));
			//RankResult updatedResult = updateResult(gGraph, result, key);

			retVal.addRankResult(key, updatedResult);
			gGraph = newGGraph;
			
			count++;
			if(count == numKeys){
				RankResult finalResult = ranker.rankSentences(qGraph, newGGraph);
				retVal.addRankResult(key + 1, finalResult);
			}
		}

		return retVal;
	}

	private GlobalGraph gGraphDiff(GlobalGraph newGraph, GlobalGraph oldGraph){
		GlobalGraph retVal = new GlobalGraph();

		for(SentenceGraph graph : oldGraph.getSentences()){
			boolean found = false;

			for(SentenceGraph sGraph : newGraph.getSentences()){
				if(graph.equals(sGraph)){
					found = true;
					break;
				}
			}

			if(!found){
				retVal.add(graph);
			}
		}

		return retVal;
	}

	private GlobalGraph updateGlobalGraph(GlobalGraph currGGraph, RankResult result, int level){
		GlobalGraph newGGraph = new GlobalGraph();
		double redFactor = levelParams.getReductionFactorForLevel(level);
		int maxRank = (int) Math.ceil(currGGraph.getSentences().size() * redFactor) - 1;

		for(SentenceGraph sGraph : currGGraph.getSentences()){
			int rank = result.getRank(sGraph);

			if(rank <= maxRank){
				newGGraph.add(sGraph);
			}
			else{
				result.removeGraph(sGraph);
			}
		}

		return newGGraph;
	}

	public LevelParameters getLevelParams() {
		return levelParams;
	}

	public void setLevelParams(LevelParameters levelParams) {
		this.levelParams = levelParams;
	}
}
