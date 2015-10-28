import java.util.ArrayList;

public abstract class Graph {
	protected class Node implements Comparable<Node>{
		public Node parent;
		public String pos, relToParent, name;
		public ArrayList<Node> children;
		public int sentence;
		
		public Node(String name, String pos, int sentence, String relToParent){
			this.name = name;
			this.pos = pos;
			this.sentence = sentence;
			this.relToParent = relToParent;
			children = new ArrayList<Node>();
		}
		
		public void addChild(Node childNode){
			children.add(childNode);
		}
		
		public Node hasDescendent(String descName){
			Node retVal = null;
			
			for(Node child : children){
				if(child.name.equals(descName)){
					return child;
				}
				else{
					retVal = child.hasDescendent(descName);
					
					if(retVal != null){
						return retVal;
					}
				}
			}
			
			return retVal;
		}
		
		public boolean hasChildren(){
			return children.size() > 0;
		}
		
		@Override
		public String toString(){
			/*
			 * Indentation not correct!!!!
			 */
			/*StringBuilder sb = new StringBuilder();
			sb.append(String.format("(%s: (POS-%s, REL-%s))", name, pos, relToParent));
			
			for(Node child : children){
				sb.append(String.format("\n\t%s", child));
			}
			
			return sb.toString();*/
			
			return name;
		}

		@Override
		public int compareTo(Node o) {
			return name.compareTo(o.name);
		}
	}

}
