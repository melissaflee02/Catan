package mypkg;

public class Tile {
	private String resource;
	private String identifier;
	private int num;
	private int probability;
	
	public Tile(String resource, String identifier) {
		this.resource = resource;
		this.identifier = identifier;
		if (resource.equals("Desert")) {
			num = 0;
		}
		else {
			num = -1;
		}
	}

	public void setNum(int num) {
		this.num = num;
	}

	public void setProbability(int num) {
		probability = num;
	}

	public String getResource() {
		return resource;
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public int getNum() {
		return num;
	}

	public int getProbability() {
		return probability;
	}

	public String getProbabilityString() {
		String result = "";
		for (int i = 0; i < probability; i++) {
			result += ". ";
		}
		return result;
	}

	public String toString() {
		return resource + ":" + num;
	}
}
