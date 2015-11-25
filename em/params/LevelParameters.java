package params;

import java.util.HashMap;
import java.util.Map;

public class LevelParameters {
	private Map<Integer, Parameters> levelParams;
	
	public LevelParameters(){
		setLevelParams(new HashMap<Integer, Parameters>());
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
	
	public void setParamsForLevel(int level, Parameters params){
		if(!levelParams.containsKey(level)){
			levelParams.put(level, params);
		}
		else{
			levelParams.replace(level, params);
		}
	}
	
	//Getters & Setters
	
	public Map<Integer, Parameters> getLevelParams() {
		return levelParams;
	}

	public void setLevelParams(Map<Integer, Parameters> levelParams) {
		this.levelParams = levelParams;
	}
}
