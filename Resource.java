package mypkg;

public class Resource {
	private String type;

	Resource(String type) {
		this.type = type;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Resource) {
			return obj.toString().equals(type);
		}
		return false;
	}

	public String getType() {
		return type;
	}

	public String toString() {
		return type;
	}
}
