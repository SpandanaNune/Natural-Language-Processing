package params;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.CommonFunctions;

public class Parameters implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2363198832959282087L;
	private Map<String, Double> parameters;
	private static String[] PARAM_KEYS = {
			"qaAnswerTypeFound", "qaAnswerTypeNotFound",
			"sentenceDiffFactor", "sentenceLessNodes", "sentenceMoreNodes", "sentenceSameNumNodes", "sentenceNumMatchingRoots",
			"nodeDiffFactor", "nodeLessNodes", "nodeMoreNodes", "nodeSameNumNodes", "nodeCommonChildren", "nodeCommonRelations",
			"nodeSameSubclass", "nodeDiffSubclass", "nodeSameInstance", "nodeDiffInstance", "nodeLowerLevel", "nodeHigherLevel",
			"nodeSameLevel"
	};
	private static List<String> KEYS = new ArrayList<String>(Arrays.asList(PARAM_KEYS));

	public Parameters(){
		parameters = new HashMap<String, Double>();

		for(String key : PARAM_KEYS){
			parameters.put(key, 0.1);
		}
	}
	
	public Parameters(Map<String, Double> parameters){
		setParameters(parameters);
	}

	@Override
	public boolean equals(Object o){
		Parameters oParams = (Parameters) o;

		for(String key : Parameters.getKeys()){
			if(parameters.get(key).doubleValue() != oParams.get(key).doubleValue()){
				return false;
			}
		}
		
		return true;
	}

	@Override
	public String toString(){
		return parameters.toString();
	}
	
	@Override
	public Parameters clone(){
		return new Parameters(CommonFunctions.cloneMap(parameters));
	}

	//Getters & Setters

	public Map<String, Double> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Double> parameters) {
		this.parameters = parameters;
	}
	
	public void setParameter(String key, Double value){
		this.parameters.replace(key, value);
	}
	
	public Double get(String key){
		return parameters.get(key);
	}
	
	public void replace(String key, Double value){
		parameters.replace(key, value);
	}
	
	public static List<String> getKeys(){
		return KEYS;
	}
}
