package mypkg;

import java.util.*;
import java.io.*;

public class Player {
	private String name;
	private String color;
	private int points;
	private int armySize;
	private ArrayList<Settlement> settlements;
	private ArrayList<Resource> resources;
	private ArrayList<String> devCards;
	private ArrayList<String> roads;
	private ArrayList<String> accessibleLocations;

	public Player(String name, String color) {
		this.name = name;
		this.color = color;
		points = 0;
		armySize = 0;
		settlements = new ArrayList<>();
		resources = new ArrayList<>();
		devCards = new ArrayList<>();
		roads = new ArrayList<>();
		accessibleLocations = new ArrayList<>();
	}	

	public void collectResources(int num, String knightLocation) {
		for (Settlement settlement : settlements) {
			for (Tile tile : settlement.getNeighborTiles()) {
				if (!tile.getIdentifier().equals(knightLocation) && tile.getNum() == num) {
					Resource temp = new Resource(tile.getResource());
					resources.add(temp);
					if (settlement.getType().equals("City")) {
						resources.add(new Resource(tile.getResource()));
					}
				}
			}
		}
	}

	public void collectResources(Settlement settlement) {
		for (Tile tile : settlement.getNeighborTiles()) {
			if (!tile.getResource().equals("Desert")) {
				Resource temp = new Resource(tile.getResource());
				resources.add(temp);
			}
		}
	}

	public void addResources(String input) {
		resources.clear();
		String[] names = input.split("\\.");
		for (String name : names) {
			if (name.length() > 1) {
				Resource temp = new Resource(name);
				resources.add(temp);
			}
		}
	}

	public void addResourcesByQuantity(String input) {
		String[] quantities = input.split(",");
		String[] resourcesReference = {"Brick", "Grain", "Lumber", "Ore", "Wool"};
		for (int i = 0; i < quantities.length; i++) {
			for (int j = 0; j < Integer.parseInt(quantities[i]); j++) {
				resources.add(new Resource(resourcesReference[i%5]));
			}
		}
	}

	public void addResource(String resource) {
		resources.add(new Resource(resource));
	}

	public void removeSingleResource(String resourceName) {
		resources.remove(new Resource(resourceName));
	}

	public int removeAllResource(String resourceName) {
		int count = 0;
		for (Resource resource : resources) {
			if (resource.getType().equals(resourceName)) {
				count++;
			}
		}
		for (int i = 0; i < count; i++) {
			resources.remove(new Resource(resourceName));
		}
		return count;
	}

	public boolean removeResourcesByQuantity(String input) {
		String[] quantities = input.split(",");
		if (!hasReqResources(Integer.parseInt(quantities[0]),Integer.parseInt(quantities[1]),Integer.parseInt(quantities[2]),Integer.parseInt(quantities[3]),Integer.parseInt(quantities[4]))) {
			return false;
		}
		String[] resourcesReference = {"Brick", "Grain", "Lumber", "Ore", "Wool"};
		for (int i = 0; i < quantities.length; i++) {
			for (int j = 0; j < Integer.parseInt(quantities[i]); j++) {
				resources.remove(new Resource(resourcesReference[i%5]));
			}
		}
		return true;
	}

	public boolean addNewSettlement(String name, ArrayList<Location> locations, int worth) {
		// check if player has required resources
		if (settlements.size() >= 2) {
			if (!hasReqResources(1,1,1,0,1)) {
				return false;
			}

			resources.remove(new Resource("Brick"));
			resources.remove(new Resource("Lumber"));
			resources.remove(new Resource("Grain"));
			resources.remove(new Resource("Wool"));
		} 

		for (Location location : locations) {
			if (location.getName().equals(name)) {
				Settlement temp = new Settlement(location, worth);
				settlements.add(temp);
				if (settlements.size() == 2) {
					collectResources(temp);
				}
			}
		}
		points++;
		return true;
	}

	public void addExistingSettlement(String name, ArrayList<Location> locations, int worth) {
		for (Location location : locations) {
			if (location.getName().equals(name)) {
				Settlement temp = new Settlement(location, worth);
				settlements.add(temp);
			}
		}
	}

	public boolean upgradeToCity(String location) {
		if (!hasReqResources(0,2,0,3,0)) {
			return false;
		}
		
		resources.remove(new Resource("Grain"));
		resources.remove(new Resource("Grain"));
		resources.remove(new Resource("Ore"));
		resources.remove(new Resource("Ore"));
		resources.remove(new Resource("Ore"));

		for (Settlement settlement : settlements) {
			if (settlement.getLocationName().equals(location)) {
				settlement.upgradeToCity();
				points++;
				return true;
			}
		}
		return false;
	}

	public boolean hasReqResources(int brick, int grain, int lumber, int ore, int wool) {
		int brickCount, grainCount, lumberCount, oreCount, woolCount;
		brickCount = 0; grainCount = 0; lumberCount = 0; oreCount = 0; woolCount = 0;
		for (Resource resource : resources) {
			if (resource.getType().toLowerCase().equals("brick")) {
				brickCount++;
			} else if (resource.getType().toLowerCase().equals("grain")) {
				grainCount++;
			} else if (resource.getType().toLowerCase().equals("lumber")) {
				lumberCount++;
			} else if (resource.getType().toLowerCase().equals("ore")) {
				oreCount++;
			} else if (resource.getType().toLowerCase().equals("wool")) {
				woolCount++;
			}
		}
		return (brickCount >= brick) && (grainCount >= grain) && (lumberCount >= lumber) && (oreCount >= ore) && (woolCount >= wool);
	}

	public boolean addDevCard(String card) {
		if (!hasReqResources(0,1,0,1,1)) {
			return false;
		}
		resources.remove(new Resource("Grain"));
		resources.remove(new Resource("Ore"));
		resources.remove(new Resource("Wool"));
		devCards.add(card);
		return true;
	}

	public void addDevCardList(String list) {
		String[] cards = list.split("\\.");
		for (String card : cards) {
			devCards.add(card);
		}
	}

	public void playDevCard(String card) {
		if (card.equals("VP")) {
			points++;
		} else if (card.equals("K")) {
			armySize++;
		}
		devCards.remove(card);
	}

	public boolean addRoad(String road, boolean checkReqResources) {
		if (checkReqResources && !hasReqResources(1,0,1,0,0)) {
			return false;
		}
		resources.remove(new Resource("Brick"));
		resources.remove(new Resource("Lumber"));
		roads.add(road);
		return true;
	}

	public void addRoadsList(String list) {
		String[] points = list.split("\\.");
		for (int i = 0; i+1 < points.length; i=i+2) {
			roads.add(points[i] + "." + points[i+1]);
		}
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public void setArmySize(int num) {
		armySize = num;
	}

	public String getName() {
		return name;
	}

	public String getColor() {
		return color;
	}

	public int getPoints() {
		return points;
	}

	public ArrayList<Resource> getResources() {
		sortResources();
		return resources;
	} 

	public String getResourcesList() {
		String list = "";
		for (int i = 0; i < resources.size(); i++) {
			if (i != resources.size()-1) {
				list += resources.get(i) + ".";
			} else {
				list += resources.get(i);
			}
		}
		return list;
	}

	public ArrayList<Settlement> getSettlements() {
		return settlements;
	}

	public String getSettlementsList() {
		String list = "";
		for (int i = 0; i < settlements.size(); i++) {
			if (i != settlements.size()-1) {
				list += settlements.get(i) + ".";
			} else {
				list += settlements.get(i);
			}
		}
		return list;
	}

	public ArrayList<String> getDevCards() {
		while (devCards.contains("")) {
			devCards.remove("");
		}
		return devCards;
	}

	public String getDevCardsList() {
		String list = "";
		for (int i = 0; i < devCards.size(); i++) {
			if (i != devCards.size()-1) {
				list += devCards.get(i) + ".";
			} else {
				list += devCards.get(i);
			}
		}
		return list;
	}

	public ArrayList<String> getRoads() {
		return roads;
	}

	public String getRoadsList() {
		String list = "";
		for (int i = 0; i < roads.size(); i++) {
			if (i != roads.size()-1) {
				list += roads.get(i) + ".";
			} else {
				list += roads.get(i);
			}
		}
		return list;
	}

	public ArrayList<String> getAccessibleLocations() {
		ArrayList<String> accessibleLocations = new ArrayList<>();
		for (Settlement settlement : settlements) {
			accessibleLocations.add(settlement.getLocationName());
		}

		for (String road : roads) {
			int index = road.indexOf(".");
			if (!accessibleLocations.contains(road.substring(0,index))) {
				accessibleLocations.add(road.substring(0,index));
			}
			if (!accessibleLocations.contains(road.substring(index+1))) {
				accessibleLocations.add(road.substring(index+1));
			}
		}
		Collections.sort(accessibleLocations);
		return accessibleLocations;
	}

	public int getArmySize() {
		return armySize;
	}

	public boolean isWinner() {
		int count = 0;
		for (String card : devCards) {
			if (card.equals("VP")) {
				count++;
			}
		}
		if (points+count >= 10) {
			return true;
		}
		return false;
	}

	public String toString() {
		return "NAME: " + name + "\n  RESOURCES: " + resources + "\n  SETTLEMENTS: " + settlements + "\n  POINTS: " + points;
	}

	public String toFormattedString() {
		sortResources();
		String output = "Player Name: " + name + "<br>Color: " + color;
		//+ "<br>Resources: ";
		/*for (int i = 0; i < resources.size(); i++) {
			if (i != resources.size()-1) {
				output += resources.get(i) + ", ";
			} else {
				output += resources.get(i);
			}
		}*/
		output += "<br>Settlements: ";
		for (int i = 0; i < settlements.size(); i++) {
			if (i != settlements.size()-1) {
				output += settlements.get(i).toFormattedString() + ", ";
			} else {
				output += settlements.get(i).toFormattedString();
			}
		}
		output += "<br>Points: " + points;
		// + "<br>Development Cards: ";
		/*for (int i = 0; i < devCards.size(); i++) {
			if (i != devCards.size()-1) {
				output += devCards.get(i) + ", ";
			} else {
				output += devCards.get(i);
			}
		}*/
		output += "<br>Roads: ";
		for (int i = 0; i < roads.size(); i++) {
			if (i != roads.size()-1) {
				output += roads.get(i) + ", ";
			} else {
				output += roads.get(i);
			}
		}
		return output;
	}

	public void sortResources() {
		boolean done;
		do {
			done = true;
			for (int i = 0; i < resources.size()-1; i++) {
				if (resources.get(i).toString().compareTo(resources.get(i+1).toString()) > 0) {
					Resource temp = resources.get(i);
					resources.set(i, resources.get(i+1));
					resources.set(i+1, temp);
					done = false;
				}
			}
		} while (!done);
	}
}
