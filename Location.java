package mypkg;

import java.util.*;

public class Location {
	private String name;
	private ArrayList<Tile> neighbors;

	public Location(String name, String nearby, ArrayList<Tile> tiles) {
		this.name = name;
		neighbors = new ArrayList<>();
		String[] parts = nearby.split(",");
		for (String part : parts) {
			neighbors.add(tiles.get(Integer.parseInt(part)));
		}
	}

	public ArrayList<Tile> getNeighborsWithNum(int num) {
		ArrayList<Tile> tiles = new ArrayList<>();
		for (Tile neighbor : neighbors) {
			if (neighbor.getNum() == num) {
				tiles.add(neighbor);
			}
		}
		return tiles;
	}
	
	public ArrayList<Tile> getNeighbors() {
		return neighbors;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return "NAME: " + name + ", NEIGHBORS: " + neighbors;
	}
}
