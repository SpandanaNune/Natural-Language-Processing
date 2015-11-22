package graph;

import java.util.Map;

public class SimilarityScores implements Comparable<SimilarityScores>{
	private double totalScore;
	private Map<Node, Double> scores;

	public SimilarityScores(Map<Node, Double> similarityScores){
		totalScore = 0;
		setScores(similarityScores);

		for(Node n : similarityScores.keySet()){
			totalScore += similarityScores.get(n);
		}
	}

	//Comparable Interface
	
	@Override
	public int compareTo(SimilarityScores o) {
		return Double.compare(totalScore, o.totalScore);
	}

	//Getters & Setters

	public Map<Node, Double> getScores() {
		return scores;
	}

	public void setScores(Map<Node, Double> scores) {
		this.scores = scores;
	}	
	
	public double getTotalScore(){
		return this.totalScore;
	}
	
	public void setTotalScore(double totalScore){
		this.totalScore = totalScore;
	}
}