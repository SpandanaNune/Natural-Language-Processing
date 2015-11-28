package params;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class LevelParameters implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2559453830942833847L;
	private Map<Integer, Parameters> levelParams;
	private Map<Integer, Double> levelReductionFactors;
	
	public LevelParameters(){
		setLevelParams(new HashMap<Integer, Parameters>());
		setLevelReductionFactors(new HashMap<Integer, Double>());
	}
	
	public LevelParameters(Map<Integer, Parameters> levelParams){
		setLevelParams(levelParams);
	}

	public Parameters getParametersForLevel(int level){
		if(levelParams.containsKey(level)){
			return levelParams.get(level);
		}
		else{
			return new Parameters();
		}
	}
	
	public Double getReductionFactorForLevel(int level){
		if(levelReductionFactors.containsKey(level)){
			return levelReductionFactors.get(level);
		}
		else{
			return .75;
		}	
	}
	
	public void setParamsForLevel(int level, Parameters params, double reductionFactor){
		if(!levelParams.containsKey(level)){
			levelParams.put(level, params);
			levelReductionFactors.put(level, reductionFactor);
		}
		else{
			levelParams.replace(level, params);
			levelReductionFactors.replace(level, reductionFactor);
		}
	}
	
	//Getters & Setters
	
	public Map<Integer, Parameters> getLevelParams() {
		return levelParams;
	}

	public void setLevelParams(Map<Integer, Parameters> levelParams) {
		this.levelParams = levelParams;
	}

	public Map<Integer, Double> getLevelReductionFactors() {
		return levelReductionFactors;
	}

	public void setLevelReductionFactors(Map<Integer, Double> levelReductionFactor) {
		this.levelReductionFactors = levelReductionFactor;
	}

	public Parameters get(Integer key) {
		return levelParams.get(key);
	}
	
	@Override 
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		for(Integer key : levelParams.keySet()){
			sb.append(String.format("Params for Level %d:\n", key));
			sb.append(String.format("\tParam values: %s\n", levelParams.get(key)));
			sb.append(String.format("\tReduction Factor: %.2f\n", levelReductionFactors.get(key)));
		}
		
		return sb.toString();
	}
}
