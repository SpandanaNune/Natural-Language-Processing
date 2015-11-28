package ranker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graph.GlobalGraph;
import graph.SentenceGraph;

public class LevelRankResult {
	private List<Integer> sortedKeys;
	private Map<Integer, RankResult> resultsByLevel;
	
	public LevelRankResult(){
		resultsByLevel = new HashMap<Integer, RankResult>();
		sortedKeys = new ArrayList<Integer>();
	}

	public int getRank(String answer) {
		int offset = 0;

		for(Integer key : resultsByLevel.keySet()){
			RankResult result = resultsByLevel.get(key);
			result.calculateRankedGraphsBySentence();
			offset += result.getRankedGraphsBySentence().size();
		}
		
		for(int i = 0; i < sortedKeys.size(); i++){
			Integer key = sortedKeys.get(i);
			RankResult result = resultsByLevel.get(key);
			offset -= result.getRankedGraphsBySentence().size();
			int rank = result.getRank(answer);
			
			if(rank != Integer.MAX_VALUE){
				rank += offset;
				return rank;
			}
		}
		
		return Integer.MAX_VALUE;
	}

	public Map<Integer, RankResult> getResultsByLevel() {
		return resultsByLevel;
	}

	public void setResultsByLevel(Map<Integer, RankResult> resultsByLevel) {
		this.resultsByLevel = resultsByLevel;
	}

	public void addRankResult(Integer key, RankResult result) {
		result.calculateRankedGraphsBySentence();
		if(resultsByLevel.containsKey(key)){
			resultsByLevel.replace(key, result);
		}
		else{
			resultsByLevel.put(key, result);
			sortedKeys.add(key);
			Collections.sort(sortedKeys);
		}
	
	}

	public void printRanks(GlobalGraph gGraph, int maxRank) {
		Map<Integer, List<SentenceGraph>> sortedRanks = new HashMap<Integer, List<SentenceGraph>>();
		List<Integer> sortedKeys = new ArrayList<Integer>();
		
		for(SentenceGraph sGraph : gGraph.getSentences()){
			int rank = getRank(sGraph.getSentence());
			
			if(!sortedRanks.containsKey(rank)){
				sortedRanks.put(rank, new ArrayList<SentenceGraph>());
				sortedKeys.add(rank);
			}
			
			sortedRanks.get(rank).add(sGraph);
		}
		
		Collections.sort(sortedKeys);
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < sortedKeys.size(); i++){
			int rank = sortedKeys.get(i);
			
			if(rank > maxRank){
				continue;
			}

			sb.append(String.format("Rank %d:\n", rank));
			
			for(SentenceGraph sGraph : sortedRanks.get(rank)){
				sb.append(String.format("\t%s\n", sGraph));
			}
			
			sb.append("\n");
		}
		
		System.out.println(sb);
	}
}
