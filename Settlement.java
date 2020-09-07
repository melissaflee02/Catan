package mypkg;

import java.util.*;

public class Settlement {
	private Location location;
	private String type;
	private int worth;

	Settlement(Location location, int worth) {
		this.location = location;
		this.worth = worth;
		if (worth == 1) {
			type = "House";
		} else {
			type = "City";
		}
	}

	public void upgradeToCity() {
		type = "City";
		worth = 2;
	}
	

	public ArrayList<Tile> getNeighborTiles() {
		return location.getNeighbors();
	}

	public String getLocationName() {
		return location.getName();
	}

	public String getType() {
		return type;
	}

	public int getWorth() {
		return worth;
	}

	public String toString() {
		return location.getName() + "." + type + "." + worth;
	}

	public String toFormattedString() {
		return type + "@" + location.getName();
	}
}
