package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CommonFunctions {
	public static <T> List<T> cloneList(List<T> list){
		List<T> newList = new ArrayList<T>();
		
		for(T n : list){
			newList.add(n);
		}
		
		return newList;
	}

	public static <T1, T2> Map<T1, T2> cloneMap(Map<T1, T2> map){
		Map<T1, T2> newMap = new HashMap<T1, T2>();
		
		for(T1 k : map.keySet()){
			newMap.put(k, map.get(k));
		}

		return newMap;
	}	

	public static int distance(String s1, String s2){
		String a = s1.toLowerCase();
		String b = s2.toLowerCase();

		int [] costs = new int [b.length() + 1];

		for (int j = 0; j < costs.length; j++){
			costs[j] = j;
		}

		for (int i = 1; i <= a.length(); i++) {
			costs[0] = i;
			int nw = i - 1;
			for (int j = 1; j <= b.length(); j++) {
				int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
				nw = costs[j];
				costs[j] = cj;
			}
		}

		return costs[b.length()];
	}
}
