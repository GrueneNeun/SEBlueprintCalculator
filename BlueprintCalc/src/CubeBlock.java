import java.util.HashMap;

public class CubeBlock {
	private String SubtypeId;
	private String TypeId;
	private HashMap<String, Integer> Components = new HashMap<String, Integer>();
	
	
	public String getTypeId() {
		return TypeId;
	}

	public void setTypeId(String typeId) {
		TypeId = typeId;
	}

	public void setSubtypeId(String id) {
		this.SubtypeId = id;
	}
	
	public String getSubtypeId() {
		return SubtypeId;
	}
	
	public HashMap<String, Integer> getComponents() {
		return Components;
	}
	
	public void setComponent(String name, int count) {
		if (Components.containsKey(name)) count += Components.get(name);
		Components.put(name, count);
	}
}
