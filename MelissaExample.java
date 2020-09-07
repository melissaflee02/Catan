import java.io.IOException;
import java.io.PrintWriter;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.*;
import java.io.File;

import mypkg.*;

public class MelissaExample extends HttpServlet {

	static final String readMeLocation = "/Applications/MAMP/tomcat/webapps/examples/WEB-INF/classes/readMe.txt";
	static final String catanTemplateLocation = "/Applications/MAMP/tomcat/webapps/examples/WEB-INF/classes/catan_template.txt";
	static final String playerPagesLocation = "/Applications/MAMP/tomcat/webapps/examples/servlets/player_pages";
	static final String playerPagesURL = "/examples/servlets/player_pages";

	static ArrayList<Tile> tiles;
	static ArrayList<Player> players;
	static ArrayList<Location> locations;
	static ArrayList<String> devCards;
	static String knightLocation;
	static String longestRoadHolder, largestArmyHolder;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean action = false; // action = true --> redirect to player pages
		boolean moveKnight = false, stealResource = false, roadBuilding1 = false, roadBuilding2 = false, yearOfPlenty = false, monopoly = false, trading = false, discardResources = false;
		boolean turnPart2 = false;
		String url = request.getParameter("url");
		String playerName = null;
		if (url!=null){playerName = url.substring(url.indexOf("player_pages/player_")+20,url.indexOf(".html"));}
		PrintWriter out = response.getWriter();

		// from MelissaExample
		if (request.getParameter("whosPlaying") != null) {
			if (!request.getParameter("whosPlaying").equals("selectPlayer")) {
				url = playerPagesURL + "/player_" + request.getParameter("whosPlaying") + ".html";
				action = true;
			}
		}

		// from CatanLogin
		else if (request.getParameter("next") != null) {
			updateNewPlayers(response, request.getParameter("newLine"));
			createNewGame(response);
			Collections.shuffle(players);
		}

		// from player_pages
		// longest road
		else if (request.getParameter("longestRoad") != null) {
			int longestRoad = 0;
			for (Player player : players) {
				if (player.getName().equals(longestRoadHolder)) {
					longestRoad = player.getRoads().size();
				}
			}

			for (Player player : players) {
				if (player.getName().equals(playerName)) {
					if ((longestRoadHolder.equals("null") && player.getRoads().size() >= 5) || (!longestRoadHolder.equals("null") && player.getRoads().size() > longestRoad)) {
						longestRoadHolder = player.getName();
						player.setPoints(player.getPoints()+2);
						addUpdate(response, playerName + " has claimed longest road.", false);
					} else {
						addUpdate(response, playerName + " does not have sufficient roads to claim longest road.", false);
					}
				}
			}
			turnPart2 = true;
			action = true;
		}

		// from player_pages
		// largest army
		else if (request.getParameter("largestArmy") != null) {
			int largestArmy = 0;
			for (Player player : players) {
				if (player.getName().equals(largestArmyHolder)) {
					largestArmy = player.getArmySize();
				}
			}
			
			for (Player player : players) {
				if (player.getName().equals(playerName)) {
					if ((largestArmyHolder.equals("null") && player.getArmySize() >= 3) || (!largestArmyHolder.equals("null") && player.getArmySize() > largestArmy)) {
						largestArmyHolder = player.getName();
						player.setPoints(player.getPoints()+2);
						addUpdate(response, playerName + " has claimed largest army.", false);
					} else {
						addUpdate(response, playerName + " does not have sufficient knights to claim largest army.", false);
					}
				}
			}
			turnPart2 = true;
			action = true;
		}

		// from player_pages
		// check winner
		else if (request.getParameter("checkWinner") != null) {
			for (Player player : players) {
				if (player.getName().equals(playerName)) {
					if (player.isWinner()) {
						addUpdate(response, "<strong>ALERT!</strong> " + playerName + " has won the game!", false);
					} else {
						addUpdate(response, "Sorry, " + playerName + " does not have sufficient points to win this game.", false);
					}
				}
			}
			turnPart2 = true;
			action = true;
		}

		// from player_pages
		// discarding resources
		else if (request.getParameter("discardingResources") != null) {
			for (Player player : players) {
				if (player.getName().equals(playerName)) {
					int num = player.getResources().size();
					for (int i = 1; i <= num; i++) {
						if (request.getParameter("resource"+i) != null) {
							player.removeSingleResource(request.getParameter("resource" + i));
						}
					}
				}
				if (player.getResources().size() > 7) {
					addUpdate(response, "<strong>WARNING!</strong> " + player.getName() + ", please continue discarding resources.", false);
					discardResources = true;
				}
			}
			if (!discardResources){moveKnight = true;}
			action = true;
		}

		// from player_pages
		// moving knight
		else if (request.getParameter("knightLocation") != null) {
			if (request.getParameter("knightLocation").equals("selectLocation")) {
				addUpdate(response, "<strong>WARNING!</strong> Please select a location to place the knight.", false);
				moveKnight = true;
			} else {
				knightLocation = request.getParameter("knightLocation");
				addUpdate(response, "The knight has been moved to location " + knightLocation + ".", false);
				stealResource = true;
			}
			turnPart2 = true;
			action = true;
		}

		else if (request.getParameter("playerToStealFrom") != null) {
			if (request.getParameter("playerToStealFrom").equals("selectPlayer")) {
				addUpdate(response, playerName + " has chosen not to steal from another player.", false);
			} else {
				String randomResource = "randomResource";
				for (Player player : players) {
					if (player.getName().equals(request.getParameter("playerToStealFrom"))) {
						randomResource = player.getResources().get((int)(Math.random()*player.getResources().size())).getType();
						player.removeSingleResource(randomResource);
					}
				}
				for (Player player : players) {
					if (player.getName().equals(playerName)) {
						player.addResource(randomResource);
					}
				}
			addUpdate(response, playerName + " has stolen a resource from " + request.getParameter("playerToStealFrom") + ".", false);
			}
			turnPart2 = true;
			action = true;
		}

		// from player_pages
		// road building
		else if (request.getParameter("loc1") != null && request.getParameter("loc2") != null) {
			if (request.getParameter("loc1").equals("selectLocation") || request.getParameter("loc2").equals("selectLocation")) {
				addUpdate(response, "<strong>WARNING!</strong> Please select two locations to build a road between.", false);
				roadBuilding1 = true;
			} else {
				makeRoad(response, playerName, request.getParameter("loc1"), request.getParameter("loc2"), false);
				roadBuilding2 = true;
			}
			turnPart2 = true;
			action = true;
		}
		else if (request.getParameter("loc3") != null && request.getParameter("loc4") != null) {
			if (request.getParameter("loc3").equals("selectLocation") || request.getParameter("loc4").equals("selectLocation")) {
				addUpdate(response, "<strong>WARNING!</strong> Please select two locations to build a road between.", false);
				roadBuilding2 = true;
			} else {	
				makeRoad(response, playerName, request.getParameter("loc3"), request.getParameter("loc4"), false);
			}
			turnPart2 = true;
			action = true;
		}

		// from player_pages
		// year of plenty
		else if (request.getParameter("resource1") != null && request.getParameter("resource2") != null) {
			if (request.getParameter("resource1").equals("selectResource") || request.getParameter("resource2").equals("selectResource")) {
				addUpdate(response, "<strong>WARNING!</strong> Please select two resources to collect.", false);
				yearOfPlenty = true;
			} else {
				for (Player player : players) {
					if (player.getName().equals(playerName)) {
						player.addResource(request.getParameter("resource1"));
						player.addResource(request.getParameter("resource2"));
					}
				}
			}
			roadBuilding2 = true;
			turnPart2 = true;
			action = true;
		}

		// from player_pages
		// monopoly
		else if (request.getParameter("resourceM") != null) {
			if (request.getParameter("resourceM").equals("selectResource")) {
				addUpdate(response, "<strong>WARNING!</strong> Please select a resource to monopolize.", false);
				monopoly = true;
			} else {
				monopoly(playerName, request.getParameter("resourceM"));
			}
			turnPart2 = true;
			action = true;
		}

		// from player_pages
		// roll dice
		else if (request.getParameter("rollDice") != null) {
			moveKnight = discardResources = rollDice(response, playerName);
			turnPart2 = true;
			action = true;
		}

		// from player_pages
		// drawing roads
		else if (request.getParameter("location1") != null && request.getParameter("location2") != null) {
			if (request.getParameter("location1").equals("selectLocation") || request.getParameter("location2").equals("selectLocation")) {
				addUpdate(response, "<strong>WARNING!</strong> Please select two locations to build a road between.", false);
			} else {
				makeRoad(response, playerName, request.getParameter("location1"), request.getParameter("location2"), true);
			}
			turnPart2 = true;
			action = true;
		}

		// from player_pages
		// placing a settlement
		else if (request.getParameter("location") != null) {
			if (request.getParameter("location").equals("selectLocation")) {
				addUpdate(response, "<strong>WARNING!</strong> Please select a location to place your settlement.", false);
			} else {
				String location = request.getParameter("location");
				assignSettlement(playerName, location, response);
			}
			if (request.getParameter("endTurn")==null){turnPart2 = true;}
			action = true;
		}

		// from player_pages
		// upgrading to a city
		else if (request.getParameter("settlement") != null) {
			if (request.getParameter("settlement").equals("selectSettlement")) {
				addUpdate(response, "<strong>WARNING!</strong> Please select a settlement location to upgrade to a city.", false);
			} else {
				String location = request.getParameter("settlement");
				upgradeToCity(playerName, location, response);
			}
			turnPart2 = true;
			action = true;
		}

		// from player_pages
		// purchasing a dev card
		else if (request.getParameter("getDevCard") != null) {
			getDevCard(response, playerName);
			turnPart2 = true;
			action = true;
		}

		// from player_pages
		// playing a dev card
		else if (request.getParameter("devCard") != null) {
			if (request.getParameter("devCard").equals("selectDevCard")) {
				addUpdate(response, "<strong>WARNING!</strong> Please select a development card.", false);
			} else {
				String devCard = request.getParameter("devCard");
				if (request.getParameter("devCard").equals("K")) {
					moveKnight = true;
				} else if (devCard.equals("YP")) {
					yearOfPlenty = true;
				} else if (devCard.equals("M")) {
					monopoly = true;
				} else if (devCard.equals("RB")) {
					roadBuilding1 = true;
				}
				playDevCard(response, playerName, devCard);
			}
			if (request.getParameter("devCards1") == null) {
				turnPart2 = true;
			}
			action = true;
		}

		// from player_pages
		// trading resources
		String player2Name = null, tradingString = null;
		if (request.getParameter("playerToTradeWith") != null) {
			if (request.getParameter("playerToTradeWith").equals("selectPlayer")) {
				addUpdate(response, "<strong>WARNING!</strong> Please select a player to trade with.", false);
			} else {
				String player1Name = url.substring(url.indexOf("player_pages/player_")+20,url.indexOf(".html"));
				player2Name = request.getParameter("playerToTradeWith");
				tradingString = player1Name;
				for (int i = 1; i <= 10; i++) {
					if (i == 6) {
						tradingString += "," + player2Name;
					}
					tradingString += "," + request.getParameter("quantity" + i);
				}
				addUpdate(response, player1Name + " has initiated a trade with " + player2Name + ".", false);
				trading = true;
			}
			turnPart2 = true;
			action = true;
		}

		if (request.getParameter("acceptTrade") != null) {
			tradingString = request.getParameter("tradingString").replace("/","");
			String parts[] = tradingString.split(",");
			boolean canTrade = true;
			
			for (Player player : players) {
				if (player.getName().equals(parts[0])) {
					if (!player.hasReqResources(Integer.parseInt(parts[7]),Integer.parseInt(parts[8]),Integer.parseInt(parts[9]),Integer.parseInt(parts[10]),Integer.parseInt(parts[11]))) {
						addUpdate(response, "<strong>WARNING!</strong> " + parts[0] + " does not have the required resources to make this trade.", false);
						canTrade = false;
					}
				} else if (player.getName().equals(parts[6])) {
					if (!player.hasReqResources(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),Integer.parseInt(parts[3]),Integer.parseInt(parts[4]),Integer.parseInt(parts[5]))) {
						addUpdate(response, "<strong>WARNING!</strong> " + parts[6] + " does not have the required resources to make this trade.", false);
						canTrade = false;
					}
				}
			}

			if (canTrade) {
				for (Player player : players) {
					if (player.getName().equals(parts[0])) {
						player.removeResourcesByQuantity(tradingString.substring(tradingString.indexOf(parts[6])+parts[6].length()+1));
						player.addResourcesByQuantity(tradingString.substring(tradingString.indexOf(parts[0])+parts[0].length()+1,tradingString.indexOf("," + parts[6])));
					} else if (player.getName().equals(parts[6])) {
						player.removeResourcesByQuantity(tradingString.substring(tradingString.indexOf(parts[0])+parts[0].length()+1,tradingString.indexOf("," + parts[6])));
						player.addResourcesByQuantity(tradingString.substring(tradingString.indexOf(parts[6])+parts[6].length()+1));
					}
				}
				addUpdate(response, parts[6] + " has accepted the trade with " + parts[0] + ".", false);
			}
			turnPart2 = true;
			action = true;
		}

		else if (request.getParameter("rejectTrade") != null) {
			tradingString = request.getParameter("tradingString").replace("/","");
			String parts[] = tradingString.split(",");
			addUpdate(response, parts[6] + " has rejected the trade with " + parts[0] + ".", false);
			turnPart2 = true;
			action = true;
		}

		// from player_pages
		// exchanging resources
		else if (request.getParameter("4cards") != null && request.getParameter("1card") != null) {
			if (request.getParameter("4cards").equals("selectResource") || request.getParameter("1card").equals("selectResource")) {
				addUpdate(response, "<strong>WARNING!</strong> Please select two resource types to exchange.", false);
			} else {
				ArrayList<String> resourcesReference = new ArrayList<String>(Arrays.asList("Brick","Grain","Lumber","Ore","Wool"));
				for (Player player : players) {
					if (player.getName().equals(playerName)) {
						ArrayList<Integer> list = new ArrayList<>();
						for (String resource : resourcesReference) {
							if (resource.equals(request.getParameter("4cards"))) {
								list.add(4);
							} else {
								list.add(0);
							}
						}
						if (!player.hasReqResources(list.get(0),list.get(1),list.get(2),list.get(3),list.get(4))) {
							addUpdate(response, "<strong>WARNING!</strong> " + playerName + " does not have the required resources to make this exchange.", false);
						} else {
							player.removeResourcesByQuantity(list.get(0)+","+list.get(1)+","+list.get(2)+","+list.get(3)+","+list.get(4)+",");
							player.addResource(request.getParameter("1card"));
							addUpdate(response, playerName + " has exchanged 4 " + request.getParameter("4cards") + " for 1 " + request.getParameter("1card"), false);
						}
					}
				}
			}
			turnPart2 = true;
			action = true;
		}

		writeBoard(response);

		// from player_pages
		// ending turn
		if (request.getParameter("endTurn") != null) {
			addUpdate(response, playerName + " has ended their turn.", false);
			updatePlayerTurn(response);
			action = true;
		}
		
		uploadBoard(response);
		writePlayerPages(response, players.get(0).getName(), moveKnight, stealResource, roadBuilding1, roadBuilding2, yearOfPlenty, monopoly, trading, player2Name, tradingString, discardResources, turnPart2);

		if (action) {
			redirectPage(response, url);
		}
	}

	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doPost(request, response);
		uploadBoard(response);
		showLogin(response);
	}

	public void redirectPage(HttpServletResponse response, String url) throws IOException{
		if (url.substring(url.length()-1).equals("/")) {
			response.sendRedirect(url.substring(0,url.length()-1));
		} else {
			response.sendRedirect(url);
		}	
	}

	public void writePlayerPages(HttpServletResponse response, String currentPlayer, boolean moveKnight, boolean stealResource, boolean roadBuilding1, boolean roadBuilding2, boolean yearOfPlenty, boolean monopoly, boolean trading, String player2Name, String tradingString, boolean discardResources, boolean turnPart2) throws IOException {
		File dir = new File(playerPagesLocation);
		for (File file : dir.listFiles()) {
			file.delete();
		}
		
		String output;
		for (Player player : players) {
			try {
				// PLAYER RESOURCES
				String playerResourcesList = "<table style=\"width:100%\"><tr>";
				ArrayList<String> resourcesReference = new ArrayList<String>(Arrays.asList("Brick", "Grain", "Lumber", "Ore", "Wool"));
				ArrayList<String> colorsReference = new ArrayList<String>(Arrays.asList("red", "orange", "brown", "gray", "green"));
				int counter = 1;
				for (Resource resource : player.getResources()) {
					playerResourcesList += "<th style=\"border:1px solid black;color:" + colorsReference.get(resourcesReference.indexOf(resource.getType())) + "\"><img src=\"/examples/servlets/images/resource_" + resource.getType().toLowerCase() + ".png\" width=\"50\"><br>" + resource.getType().toUpperCase() + "</th>";
					if (counter%5==0) {
						playerResourcesList += "</tr><tr>";
					}
					counter++;
				}
				playerResourcesList += "</tr></table>";
				// PLAYER DEV CARDS
				String playerDevCardsList = "<table style=\"width:100%\"><tr>";
				for (String card : player.getDevCards()) {
					playerDevCardsList += "<th style=\"border:1px solid black;color:black\"><img src=\"/examples/servlets/images/dc_" + card.toLowerCase() + ".png\" width=\"75\"><br>" + card + "</th>";
				}
				playerDevCardsList += "</tr></table>";

				// DISCARDING RESOURCES
				String discardResourcesList = "<table style=\"width:400px\"><tr><form action=\"MelissaExample\" method=\"POST\"><th style=\"padding:10px;background-color:red;color:black\">Select Half of Your Resources to Discard:<br>";
				counter = 1;
				for (Resource resource : player.getResources()) {
					discardResourcesList += "<input type=\"checkbox\" name=\"resource" + counter + "\" value=\"" + resource.getType() + "\">" + resource.getType() + "  ";
					if (counter%4==0) {
						discardResourcesList += "<br>";
					}
					counter++;
				}
				discardResourcesList += "<br><input type=\"submit\" name=\"discardingResources\" value=\"Submit\"><input type=\"hidden\" name=\"url\" value=URL/></th></tr></table>";

				String playersToStealFromList = "<table><tr><form action=\"MelissaExample\" method=\"POST\"><th style=\"padding:10px;background-color:red;color:black;width:350px\">Select the Player to Steal From: <select name=\"playerToStealFrom\"><option value=\"selectPlayer\">Select Player</option>";
				for (Player temp : players) {
					boolean added = false;
					if (temp.getName().equals(currentPlayer) || temp.getResources().size() == 0) {
						continue;
					}
					for (Settlement settlement : temp.getSettlements()) {
						for (Tile tile : settlement.getNeighborTiles()) {
							if (tile.getIdentifier().equals(knightLocation)) {
								if (!added) {
									playersToStealFromList += "<option value=\"" + temp.getName() + "\">" + temp.getName() + "</option>";
									added = true;
								}
							}
						}
					}
				}
				playersToStealFromList += "</select><br><input type=\"submit\" value=\"Submit\"/><input type=\"hidden\" name=\"url\" value=URL/></form></th></tr></table>";

				output = createBoard(response, tiles, players);
				String url = playerPagesLocation + "/player_" + player.getName() + ".html";
				FileWriter myWriter = new FileWriter(url);
				if (discardResources && player.getResources().size() > 7) {
					output = output.replace("*DISCARD RESOURCES", discardResourcesList);
				} else if (player.getName().equals(currentPlayer) && moveKnight) {
					output = output.replace("*MOVE KNIGHT", "<table><tr><form action=\"MelissaExample\" method=\"POST\"><th style=\"padding:10px;background-color:red;color:black\">Please Enter the New Knight Location: <br><select name=\"knightLocation\"><option value=\"selectLocation\">Select Location</option><option value=\"0\">0</option><option value=\"1\">1</option><option value=\"2\">2</option><option value=\"3\">3</option><option value=\"4\">4</option><option value=\"5\">5</option><option value=\"6\">6</option><option value=\"7\">7</option><option value=\"8\">8</option><option value=\"9\">9</option><option value=\"10\">10</option><option value=\"11\">11</option><option value=\"12\">12</option><option value=\"13\">13</option><option value=\"14\">14</option><option value=\"15\">15</option><option value=\"16\">16</option><option value=\"17\">17</option><option value=\"18\">18</option></select><input type=\"submit\" value=\"Submit\"/><br><input type=\"hidden\" name=\"url\" value=URL/></form></th></tr></table>");
				} else if (player.getName().equals(currentPlayer) && stealResource) {
					output = output.replace("*MOVE KNIGHT", playersToStealFromList);
				}else if (player.getName().equals(currentPlayer) && roadBuilding1) {
					output = output.replace("*ROAD BUILDING", "<table><tr><form action=\"MelissaExample\" method=\"POST\"><th style=\"padding:10px;background-color:red;color:black\">Place Road 1 From: <select name=\"loc1\"><option value=\"selectLocation\">Select Location</option>*ACCESSIBLE LOCATIONS 1</select>To:<select name=\"loc2\"><option value=\"selectLocation\">Select Location</option>*ACCESSIBLE LOCATIONS 2</select><input type=\"submit\" value=\"Submit\"/><br><input type=\"hidden\" name=\"url\" value=URL/></form></th></tr></table>");
				} else if (player.getName().equals(currentPlayer) && roadBuilding2) {
					output = output.replace("*ROAD BUILDING", "<table><tr><form action=\"MelissaExample\" method=\"POST\"><th style=\"padding:10px;background-color:red;color:black\">Place Road 2 From: <select name=\"loc3\"><option value=\"selectLocation\">Select Location</option>*ACCESSIBLE LOCATIONS 1</select>To:<select name=\"loc4\"><option value=\"selectLocation\">Select Location</option>*ACCESSIBLE LOCATIONS 2</select><input type=\"submit\" value=\"Submit\"/><br><input type=\"hidden\" name=\"url\" value=URL/></form></th></tr></table>");
				} else if (player.getName().equals(currentPlayer) && yearOfPlenty) {
					output = output.replace("*YEAR OF PLENTY", "<table><tr><th style=\"padding:10px;background-color:red;color:black\"><form action=\"MelissaExample\" method=\"POST\">Please Choose Two Resources to Add: <br><select name=\"resource1\"><option value=\"selectResource\">Select Resource</option><option value=\"Brick\">Brick</option><option value=\"Lumber\">Lumber</option><option value=\"Ore\">Ore</option><option value=\"Grain\">Grain</option><option value=\"Wool\">Wool</option></select><select name=\"resource2\"><option value=\"selectResource\">Select Resource</option><option value=\"Brick\">Brick</option><option value=\"Lumber\">Lumber</option><option value=\"Ore\">Ore</option><option value=\"Grain\">Grain</option><option value=\"Wool\">Wool</option></select><input type=\"submit\" value=\"Submit\"/><br><input type=\"hidden\" name=\"url\" value=URL/></form></th></tr></table>");
				} else if (player.getName().equals(currentPlayer) && monopoly) {
					output = output.replace("*MONOPOLY", "<table><tr><th style=\"padding:10px;background-color:red;color:black\"><form action=\"MelissaExample\" method=\"POST\">Please Choose the Resource to Monopolize: <br><select name=\"resourceM\"><option value=\"selectResource\">Select Resource</option><option value=\"Brick\">Brick</option><option value=\"Lumber\">Lumber</option>option value=\"Ore\">Ore</option><option value=\"Grain\">Grain</option><option value=\"Wool\">Wool</option></select><input type=\"submit\" value=\"Submit\"/><br><input type=\"hidden\" name=\"url\" value=URL/></form></th></tr></table>");
				} else if (trading && player.getName().equals(player2Name)) {
					String parts[] = tradingString.split(",");
					String resources[] = {"Wool", "Brick", "Grain", "Lumber", "Ore"};
					String player1Resources = "";
					String player2Resources = "";
					for (int i = 1; i <= 11; i++) {
						if (i >= 1 && i <= 5) {
							player1Resources += parts[i] + " " + resources[i%5];
							if(i!=5){player1Resources+=", ";}
						} else if (i == 6) {
							continue;
						} else {
							player2Resources += parts[i] + " " + resources[(i-1)%5];
							if(i!=11){player2Resources+=", ";}
						}
					}

					output = output.replace("*TRADE RESOURCES", "<table style=\"width:500px\"><tr><th style=\"padding:10px;background-color:red;color:black\">" + parts[0] + " Wants to Trade With " + parts[6] + "!</th></tr><tr><th style=\"padding:10px;background-color:red;color:white\"><form action=\"MelissaExample\" method=\"POST\">"+ parts[0] + " wants " + player1Resources + "<br><br>" + parts[0] + " is offering " + player2Resources + "<br><br><input type=\"submit\" name=\"acceptTrade\" value=\"I Accept the Offer!\"/><input type=\"submit\" name=\"rejectTrade\" value=\"I Reject the Offer.\"/><input type=\"hidden\" name=\"url\" value=URL/><input type=\"hidden\" name=\"tradingString\" value=" + tradingString + "/></form></th></tr></table>");
				}

				output = output.replace("*KNIGHT LOCATION", "Knight Location: " + knightLocation);
				output = output.replace("*LONGEST ROAD", "Longest Road Holder: " + longestRoadHolder);
				output = output.replace("*LARGEST ARMY", "Largest Army Holder: " + largestArmyHolder);
				output = output.replace("*PLAYER RESOURCES", "<p>My Resources:</p>" + playerResourcesList);
				output = output.replace("*PLAYER DEV CARDS", "<p>My Development Cards:</p>" + playerDevCardsList);
				output = output.replaceAll("MelissaExample", "http://localhost:8080/examples/servlets/servlet/MelissaExample");
				output = output.replaceAll("URL", playerPagesURL + "/player_" + player.getName() + ".html");
			
				// TESTING
				if (player.getName().equals(players.get(0).getName())) {
					if (player.getSettlements().size() < 2) {
						output = getChoosingSettlements(response, currentPlayer,output);
					} else if (turnPart2) {
						output = getPlayerTurnPart2(response, currentPlayer, output);
					} else {
						output = getPlayerTurnPart1(response, currentPlayer, output);
					}
				} else {
					output = output.substring(0,output.indexOf("<!--*CHOOSING SETTLEMENTS-->")) + output.substring(output.indexOf("<!--*END OF TURN PART 2-->"));
				}

				myWriter.write(output.replace("Welcome to Catan!", "Welcome to Catan, " + player.getName() + "!"));
				myWriter.close();
			} catch (IOException e) {
				System.out.println("writePlayerPage: an error occurred.");
				e.printStackTrace();
			}
		}
	}

	public String getChoosingSettlements(HttpServletResponse response, String currentPlayer, String output) throws IOException {
		ArrayList<String> takenLocations = new ArrayList<>();
		for (Player player : players) {
			for (Settlement settlement : player.getSettlements()) {
				takenLocations.add(settlement.getLocationName());
				takenLocations.addAll(getAccessibleLocations2(settlement.getLocationName()));
			}
		}
		addUpdate(response, "takenLocations-" + takenLocations, false);
		for (Player player : players) {
			if (player.getName().equals(currentPlayer)) {
				String locationsList = "";
				for (Location location : locations) {
					if (!takenLocations.contains(location.getName())) {
						locationsList += "<option value=\"" + location.getName() + "\">" + location.getName() + "</option>" + "\n";
					}
				}
				output = output.replace("*LOCATIONS", locationsList);
			}
		}
		return output.substring(0,output.indexOf("<!--*END OF CHOOSING SETTLEMENTS-->"))  + output.substring(output.indexOf("<!--*END OF TURN PART 2-->"));
	}

	public String getPlayerTurnPart1(HttpServletResponse response, String currentPlayer, String output) throws IOException {
		for (Player player : players) {
			if (player.getName().equals(currentPlayer)) {
				String devCards1List = "";
				for (String card : player.getDevCards()) {
					if (card.equals("K")) {
						devCards1List += "<option value=\"" + card + "\">" + card + "</option>\n";
					}
				}
				output = output.replace("*DEV CARDS 1", devCards1List);
			}
		}
		return output.substring(0,output.indexOf("<!--*CHOOSING SETTLEMENTS-->")) + output.substring(output.indexOf("<!--*END OF CHOOSING SETTLEMENTS-->"),output.indexOf("<!--*END OF TURN PART 1-->")) + output.substring(output.indexOf("<!--*END OF TURN PART 2-->"));
	}

	public String getPlayerTurnPart2(HttpServletResponse response, String currentPlayer, String output) throws IOException {
		for (Player player : players) {
			if (player.getName().equals(currentPlayer)) {
				String accessibleLocations1List = "";
				String accessibleLocations2List = "";
				for (String location1 : player.getAccessibleLocations()) {
					accessibleLocations1List += "<option value=\"" + location1 + "\">" + location1 + "</option>\n";
					for (String location2 : getAccessibleLocations2(location1)) {
						accessibleLocations2List += "<option value=\"" + location2 + "\">" + location2 + "</option>\n";
					}
				}

				String settlementsList = "";
				for (Settlement settlement : player.getSettlements()) {
					if (settlement.getType().equals("House")) {
						settlementsList += "<option value=\"" + settlement.getLocationName() + "\">" + settlement.getLocationName() + "</option>\n";
					}
				}

				String devCards2List = "";
				for (String card : player.getDevCards()) {
					devCards2List += "<option value=\"" + card + "\">" + card + "</option>\n";
				}

				String playersToTradeWithList = "";
				for (Player temp : players) {
					if (!temp.getName().equals(currentPlayer)) {
						playersToTradeWithList += "<option value=\"" + temp.getName() + "\">" + temp.getName() + "</option>\n";
					}
				}

				output = output.replace("*ACCESSIBLE LOCATIONS 1", accessibleLocations1List);
				output = output.replace("*ACCESSIBLE LOCATIONS 2", accessibleLocations2List);
				output = output.replace("*SETTLEMENTS", settlementsList);
				output = output.replace("*DEV CARDS 2", devCards2List);
				output = output.replace("*PLAYERS TO TRADE WITH", playersToTradeWithList);
			}
		}
		return output.substring(0,output.indexOf("<!--*CHOOSING SETTLEMENTS-->")) + output.substring(output.indexOf("<!--*TURN PART 2-->"));
	}

	public void updatePlayerTurn(HttpServletResponse response) throws IOException {
			String playerTurn = "CURRENT PLAYER NAME";
			try {
				String output = "";
				Scanner input = readFile(readMeLocation);
				while (input.hasNextLine()) {
					String line = input.nextLine();
					if (line.length() > 3 && line.substring(0,3).equals("10:")) {
						String[] parts = line.split(":");
						line = "10:";
						for (int i = 2; i < parts.length; i++) {
							line += parts[i] + ":";
						}
						line += parts[1];
						playerTurn = line.substring(3,line.indexOf(","));
					}
					output += line + "\n";
				}
				FileWriter myWriter = new FileWriter(readMeLocation);
				myWriter.write(output);
				myWriter.close();
			} catch (IOException e) {
				System.out.println("updateNewPlayers: an error occurred.");
				e.printStackTrace();
			}
			addUpdate(response, "It is now " + playerTurn + "'s turn.", false);
	}
	
	public void showLogin(HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		String list = "";
		for (Player player : players) {
			list += "<option value=\"" + player.getName() + "\">" + player.getName() + "</option>" + "\n";
		}
		out.println("<html><form action=\"MelissaExample\" method=\"POST\">Who's Playing Today? <select name=\"whosPlaying\"><option value=\"selectPlayer\">Select Player</option>" + list + "</select> <input type=\"submit\" value=\"Submit\"/><br></form><p style=\"color:red\"><strong>NOTE: A random ordering will be determined once game begins.</strong></p></html>");
	}

	public void uploadBoard(HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();

		tiles = new ArrayList<Tile>();
		players = new ArrayList<Player>();
		devCards = new ArrayList<String>();
		Scanner input = readFile(readMeLocation);
		while (input.hasNextLine()) {
			String line = input.nextLine();
			// TILES
			int counter = 0;
			if (line.length() > 1 && line.substring(0,2).equals("9:")) {
				String[] parts = line.split(":");
				for (int i = 1; i < parts.length; i++) {
					String[] nums = parts[i].split(",");
					Tile temp = new Tile(nums[0], "" + counter);
					temp.setNum(Integer.parseInt(nums[1]));
					temp.setProbability(Integer.parseInt(nums[2]));
					tiles.add(temp);
					counter++;
				}
				locations = getLocations(tiles);
			}
			// PLAYERS
			if (line.length() > 2 && line.substring(0,3).equals("10:")) {
				String[] parts = line.split(":");
				for (int i = 1; i < parts.length; i++) {
					String[] details = parts[i].split(",");
					Player temp = new Player(details[0],details[1]);
					temp.addResources(details[2]);
					String[] houseDetails = details[3].split("\\.");
					if (houseDetails.length > 1) {
						for (int j = 0; j+2 < houseDetails.length; j=j+3) {
							temp.addExistingSettlement(houseDetails[j], locations, Integer.parseInt(houseDetails[j+2]));
						}
					}
					temp.setPoints(Integer.parseInt(details[4]));
					if (details.length > 5) {
						temp.addDevCardList(details[5]);
					}
					if (details.length > 6) {
						temp.addRoadsList(details[6]);
					}
					if (details.length > 7) {
						temp.setArmySize(Integer.parseInt(details[7]));
					}
					players.add(temp);
				}
			}

			// DEVELOPMENT CARDS
			if (line.length() > 2 && line.substring(0,3).equals("11:")) {
				String[] parts = line.split(":");
				for (int i = 1; i < parts.length; i++) {
					devCards.add(parts[i]);
				}
			}
			// KNIGHT LOCATION
			if (line.length() > 2 && line.substring(0,3).equals("12:")) {
				int index = line.indexOf(":");
				knightLocation = line.substring(index+1);
			}
			// LONGEST ROAD
			if (line.length() > 2 && line.substring(0,3).equals("13:")) {
				int index = line.indexOf(":");
				longestRoadHolder = line.substring(index+1);
			}
			// LARGEST ARMY
			if (line.length() > 2 && line.substring(0,3).equals("14:")) {
				int index = line.indexOf(":");
				largestArmyHolder = line.substring(index+1);
			}
		}
	}

	public void createNewGame(HttpServletResponse response) throws IOException {
		clearReadMe(response);
		addUpdate(response, "A new game has started.", true);
		tiles = createTiles();
		players = getPlayers();
		locations = getLocations(tiles);
		devCards = getDevCards();
		knightLocation = "-1";
		Collections.shuffle(tiles);
		Collections.shuffle(devCards);
		assignNums(tiles);
		writeBoard(response);
	}

	public void clearReadMe(HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		String output = "";
		Scanner input = readFile(readMeLocation);
		while (input.hasNextLine()) {
			String line = input.nextLine();
			output += line + "\n";
			if (line.contains("**START WRITING BELOW HERE**")) {
				break;
			}
		}
		try {
			FileWriter myWriter = new FileWriter(readMeLocation);
			myWriter.write(output);
			myWriter.close();
		} catch (IOException e) {
			System.out.println("clearReadMe: an error occured.");
			e.printStackTrace();
		}
	}

	public Scanner readFile(String fileName) {
		try {
			return new Scanner(new File(fileName));
		} catch (FileNotFoundException e) {
			System.out.println("Invalid file name.");
		}
		return null;
	}

	public ArrayList<Tile> createTiles() {
		Scanner input = readFile(readMeLocation);
		ArrayList<Tile> tiles = new ArrayList<>();
		int index = 0;
		while (input.hasNextLine()) {
			String line = input.nextLine();
			if (line.length() > 1 &&line.substring(0,2).equals("1:")) {
				String parts[] = line.split(":");
				for (int i = 1; i < parts.length-1; i=i+2) {
					for (int j = 0; j < Integer.parseInt(parts[i]); j++) {
						Tile temp = new Tile(parts[i+1], "" + index);
						tiles.add(temp);
						index++;
					}
				}
			}
		}
		return tiles;
	}

	public ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<>();
		Scanner input = readFile(readMeLocation);
		String accessibleLocations = "";
		while (input.hasNextLine()) {
			String line = input.nextLine();
			if (line.length() > 1 && line.substring(0,2).equals("7:")) {
				String parts[] = line.split(":");
				for (int i = 1; i < parts.length; i++) {
					int index = parts[i].indexOf(",");
					Player temp = new Player(parts[i].substring(0,index), parts[i].substring(index+1));
					players.add(temp);
				}
			}
		}
		return players;
	}

	public ArrayList<Location> getLocations(ArrayList<Tile> tiles) {
		ArrayList<Location> locations = new ArrayList<>();
		Scanner input = readFile(readMeLocation);
		while (input.hasNextLine()) {
			String line = input.nextLine();
			if (line.length() > 1 && line.substring(0,2).equals("3:")) {
				String parts[] = line.split(":");
				for (int i = 1; i < parts.length; i++) {
					int index = parts[i].indexOf(",");
					Location temp = new Location(parts[i].substring(0,index), parts[i].substring(index+1), tiles);
					locations.add(temp);
				}
			}
		}
		return locations;
	}

	public ArrayList<String> getDevCards() {
		ArrayList<String> devCards = new ArrayList<>();
		Scanner input = readFile(readMeLocation);
		while (input.hasNextLine()) {
			String line = input.nextLine();
			if (line.length() > 1 && line.substring(0,2).equals("6:")) {
				String parts[] = line.split(":");
				for (int i = 1; i < parts.length; i++) {
					int index = parts[i].indexOf(",");
					for (int j = 0; j < Integer.parseInt(parts[i].substring(index+1)); j++) {
						devCards.add(parts[i].substring(0, index));
					}
				}
			}
		}
		return devCards;
	}

	public ArrayList<String> getAccessibleLocations2(String location1) {
		ArrayList<String> accessibleLocations = new ArrayList<>();
		Scanner input = readFile(readMeLocation);
		while (input.hasNextLine()) {
			String line = input.nextLine();
			if (line.length() > 1 && line.substring(0,2).equals("4:")) {
				int index = line.indexOf(location1+",");
				String[] temp = line.substring(index+3,line.indexOf(":",index+1)).split("\\.");
				for (String location : temp) {
					accessibleLocations.add(location);
				}
			}
		}
		return accessibleLocations;
	}

	public void assignNums(ArrayList<Tile> tiles) {
		Scanner input = readFile(readMeLocation);
		while (input.hasNextLine()) {
			String line = input.nextLine();
			if (line.length() > 1 && line.substring(0,2).equals("2:")) {
				String parts[] = line.split(":");
				int tracker = 1;
				for (int i = 0; i < tiles.size(); i++) {
					if (tiles.get(i).getResource().equals("Desert")) {
						continue;
					}
					String nums[] = parts[tracker].split(",");
					tiles.get(i).setNum(Integer.parseInt(nums[0]));
					tiles.get(i).setProbability(Integer.parseInt(nums[1]));
					tracker++;
				}
			}
		}
	}

	public String createBoard(HttpServletResponse response, ArrayList<Tile> tiles, ArrayList<Player> players) throws IOException {
		String output = "";
		PrintWriter out = response.getWriter();
		Scanner input = readFile(catanTemplateLocation);
		boolean startSettlements = false;
		while (input.hasNextLine()) {
			String line = input.nextLine();
			if (line.contains("*PLAYER TURN")) {
				line = "It is currently <mark>" + players.get(0).getName() + "'s</mark> turn.";
			}
			if (line.contains("<th>*")) {
				while (line.contains("<th>*")) {
					int location = line.indexOf("<th>*");
					int index = Integer.parseInt(line.substring(location+5, location+7));
					if (tiles.get(index).getIdentifier().equals(knightLocation)) {
						line = line.substring(0, location) + "<th class=\"" + tiles.get(index).getResource().toLowerCase() + "\" style=\"color:red\"><del>" + tiles.get(index).getIdentifier() + ". " + tiles.get(index).getResource() + "</del><br>" + tiles.get(index).getProbabilityString() + "<br>" + tiles.get(index).getNum() + line.substring(location + 7);
					} else {
						line = line.substring(0, location) + "<th class=\"" + tiles.get(index).getResource().toLowerCase() + "\">" + tiles.get(index).getIdentifier() + ". " + tiles.get(index).getResource() + "<br>" + tiles.get(index).getProbabilityString() + "<br>" + tiles.get(index).getNum() + line.substring(location + 7);
					}
				}
			}
			if (line.contains("*NAMES")) {
				line = "";
				for (Player player : players) {
					line += "<option value=\"" + player.getName() + "\">" + player.getName() + "</option>" + "\n";
				}
			}
			if (line.contains("*LOCATIONS")) {
				line = "";
				for (Location location : locations) {
					line += "<option value=\"" + location.getName() + "\">" + location.getName() + "</option>" + "\n";
				}
			}
			if (line.contains("*STATISTICS")) {
				line = "";
				String backgroundColor, textColor;
				for (Player player : players) {
					if (player.getColor().toLowerCase().equals("white")) {
						backgroundColor = "darkgrey";
						textColor = "black";
					} else if (player.getColor().toLowerCase().equals("brown")) {
						backgroundColor = "saddlebrown";
						textColor = "white";
					}else {
						backgroundColor = "dark" + player.getColor();
						textColor = "white";
					}
					line += "<p style=\"padding-left:10px;padding-right:5px;color:" + textColor + ";background:" + backgroundColor + "\"><br>" + player.toFormattedString() + "<br><br></p>";
				}
			}
			if (line.contains("*START SETTLEMENTS HERE")) {
				startSettlements = true;
			} else if (line.contains("*END SETTLEMENTS HERE")) {
				startSettlements = false;
			}
			if (startSettlements) {
				for (Player player : players) {
					String color = player.getColor().toLowerCase();
					for (Settlement settlement : player.getSettlements()) {
						String type = settlement.getType().toLowerCase();
						if (line.contains("*" + settlement.getLocationName())) {
							int index = line.indexOf("*" + settlement.getLocationName());
							if (line.substring(index+3,index+4).equals("1")) {
								line = line.substring(0,index-12) + ";background-position:bottom\" class=\"" + type + "_" + color + "\">" + settlement.getLocationName() + line.substring(index+4);
							} else if (line.substring(index+3,index+4).equals("2")) {
								line = line.substring(0,index-12) + ";background-position:top\" class=\"" + type + "_" + color + "\">" + settlement.getLocationName() + line.substring(index+4);
							}
						}
					}
				}
			}
			if (line.contains("*UPDATES")) {
				line = getUpdates();
			} else if (line.contains("*ROADS")) {
				line += "\n" + getRoads(response);
			}
			output += line + "\n";
		}
		return output;
	}

	public void writeBoard(HttpServletResponse response) throws IOException {
		clearReadMe(response);
		// WRITING BOARD
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(readMeLocation, true));
			// TILES
			bw.write("9:");
			for (int i = 0; i < tiles.size(); i++) {
				bw.write(tiles.get(i).getResource() + "," + tiles.get(i).getNum() + "," + tiles.get(i).getProbability());
				if (i != tiles.size()-1) {
					bw.write(":");
				}
			}

			// PLAYERS
			bw.write("\n10:");
			for (int i = 0; i < players.size(); i++) {
				bw.write(players.get(i).getName() + "," + players.get(i).getColor() + "," + players.get(i).getResourcesList() + "," + players.get(i).getSettlementsList() + "," + players.get(i).getPoints() + "," + players.get(i).getDevCardsList() + "," + players.get(i).getRoadsList() + "," + players.get(i).getArmySize());
				if (i != players.size()-1) {
					bw.write(":");
				}
			}

			// DEVELOPMENT CARDS
			bw.write("\n11:");
			for (int i = 0; i < devCards.size(); i++) {
				bw.write(devCards.get(i));
				if (i != devCards.size()-1) {
					bw.write(":");
				}
			}

			// KNIGHT LOCATION
			bw.write("\n12:" + knightLocation);
			
			// LONGEST ROAD
			bw.write("\n13:" + longestRoadHolder);

			// LARGEST ARMY
			bw.write("\n14:" + largestArmyHolder);

			bw.close();
		} catch (IOException e) {
			System.out.println("writeBoard: an error occured.");
			e.printStackTrace();
		}
	}

	public void updateNewPlayers(HttpServletResponse response, String newLine) throws IOException {
		try {
			String output = "";
			Scanner input = readFile(readMeLocation);
			while (input.hasNextLine()) {
				String line = input.nextLine();
				if (line.length() > 1 && line.substring(0,2).equals("7:") && !newLine.equals("null")) {
					line = "7:" + newLine;
				}
				output += line + "\n";
			}
			FileWriter myWriter = new FileWriter(readMeLocation);
			myWriter.write(output);
			myWriter.close();
		} catch (IOException e) {
			System.out.println("updateNewPlayers: an error occurred.");
			e.printStackTrace();
		}
	}

	public void makeRoad(HttpServletResponse response, String playerName, String locX, String locY, boolean checkReqResources) throws IOException {
		if (!getAccessibleLocations2(locX).contains(locY)) {
			addUpdate(response, "<strong>WARNING!</strong> Location " + locX + " does not have access to location " + locY + ".", false);
			return;
		}
		for (Player player : players) {
			if (player.getName().equals(playerName)) {
				boolean roadAdded = player.addRoad(locX + "." + locY, checkReqResources);
				if (!roadAdded) {
					addUpdate(response, "<strong>WARNING!</strong> " + playerName + " does not have the required resources to build a road from location " + locX + " to location " + locY + ".",false);
					return;
				}
				break;
			}
		}
		addUpdate(response, playerName + " has placed a road from location " + locX + " to location " + locY + ".",false);
	}

	public void assignSettlement(String playerName, String location, HttpServletResponse response) throws IOException {
		if (location.equals("selectLocation")) {
			return;
		}
		// check if settlement is taken
		for (Player player : players) {
			for (Settlement settlement : player.getSettlements()) {
				if (settlement.getLocationName().equals(location)) {
					addUpdate(response, "<strong>WARNING!</strong> Location " + location + " has already been taken.", false);
					return;
				}
			}
		}

		// add settlement
		for (Player player : players) {
			if (player.getName().equals(playerName)) {
				boolean added = player.addNewSettlement(location, locations, 1);
				if (added) {
					addUpdate(response, player.getName() + " has placed a settlement at location " + location + ".", false);
				} else {
					addUpdate(response, "<strong>WARNING!</strong> " + player.getName() + " does not have the required resources to build a settlement at location " + location + ".", false);
				}
			}
		}	
	}

	public void upgradeToCity(String playerName, String location, HttpServletResponse response) throws IOException {
		for (Player player : players) {
			if (player.getName().equals(playerName)) {
				boolean upgraded = player.upgradeToCity(location);
				if (upgraded) {
					addUpdate(response, player.getName() + " has upgraded to a city at location " + location + ".", false);
				} else {
					addUpdate(response, "<strong>WARNING!</strong> " + player.getName() + " does not have the required resources to upgrade to a city at location " + location + ".", false);
				}
			}
		}
	}

	public void getDevCard(HttpServletResponse response, String playerName) throws IOException {
		if (devCards.size() == 0) {
			addUpdate(response, "<strong>WARNING!</strong> There are currently no development cards available for purchase.", false);
			return;
		}
		for (Player player : players) {
			if (player.getName().equals(playerName)) {
				boolean added = player.addDevCard(devCards.get(0));
				if (added) {
					addUpdate(response, playerName + " has purchased a development card.", false);
					devCards.remove(0);
				} else {
					addUpdate(response, "<strong>WARNING!</strong> " + player.getName() + " does not have the required resources to purchase a development card.", false);
				}
			}
		}
	}

	public void playDevCard(HttpServletResponse response, String playerName, String devCard) throws IOException {
		for (Player player : players) {
			if (player.getName().equals(playerName)) {
				player.playDevCard(devCard);
			}
		}
		addUpdate(response, playerName + " has played a " + devCard + " development card.", false);
	}

	public void monopoly(String playerName, String resource) {
		int count = 0;
		for (Player player : players) {
			if (player.getName().equals(playerName)) {
				for (Player otherPlayer : players) {
					if (!otherPlayer.getName().equals(playerName)) {
						count += otherPlayer.removeAllResource(resource);
					}
				}
				for (int i = 0; i < count; i++) {
					player.addResource(resource);
				}
				return;
			}
		}
	}
	
	public boolean rollDice(HttpServletResponse response, String playerName) throws IOException {
		int die1 = (int)(Math.random()*6)+1;
		int die2 = (int)(Math.random()*6)+1;
		int num = die1 + die2;

		for (Player player : players) {
			player.collectResources(num, knightLocation);
		}
		addUpdate(response, playerName + " has rolled " + num + ".", false);
		if (num == 7) {
			return true;
		}
		return false;
	}

	public void addUpdate(HttpServletResponse response, String update, boolean clear) throws IOException {
		// writing update to readMe.txt
		try {
			Scanner input = readFile(readMeLocation);
			String output = "";
			while (input.hasNextLine()) {
				String line = input.nextLine();
				if (line.length() != 0 && line.substring(0,2).equals("8:")) {
					if (clear) {
						line = "8:" + update;
					} else if (line.length() == 2) {
						line += update;
					} else if (line.length() > 2) {
						line += ":" + update;
					}
				}
				output += line + "\n";
			}
			FileWriter myWriter = new FileWriter(readMeLocation);
			myWriter.write(output);
			myWriter.close();
		} catch (IOException e) {
			System.out.println("addUpdate: an error occurred.");
			e.printStackTrace();
		}
	}

	public String getUpdates() {
		Scanner input = readFile(readMeLocation);
		String updates = "";
		while (input.hasNextLine()) {
			String line = input.nextLine();
			if (line.length() > 2 && line.substring(0,2).equals("8:")) {
				String[] parts = line.split(":");
				if (parts.length <= 5) {
					for (int i = parts.length-1; i > 0; i--) {
						updates += parts[i] + "<br>";
					}
				} else {
					for (int i = parts.length-1; i > parts.length-5; i--) {
						updates += parts[i] + "<br>";
					}
				}
				break;
			}
		}
		
		return "<div class=\"topnav\"><table style=\"width:100%;height:70px\"><tr><th style=\"padding-left:10px;padding-top:5px;padding-bottom:5px;color:black;font-size:20px;width:25%\">Recent Updates:</th><th style=\"padding-left:10px;padding-right:5px;color:black;font-size:15px;width:100%\">" + updates + "</th></tr></table></div>";
	}

	public String getRoads(HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();

		Scanner input = readFile(readMeLocation);
		String roads = "";
		String coordinates = "";
		while (input.hasNextLine()) {
			String line = input.nextLine();
			if (line.length() > 2 && line.substring(0,2).equals("5:")) {
				coordinates = line.substring(2);
				break;
			}
		}
		for (Player player : players) {
			String[] locations = player.getRoadsList().split("\\.");
			for (int i = 0; i+1 < locations.length; i = i+2) {
				String coor1 = coordinates.substring(coordinates.indexOf(locations[i])+3, coordinates.indexOf(":", coordinates.indexOf(locations[i])));
				String coor2 = coordinates.substring(coordinates.indexOf(locations[i+1])+3, coordinates.indexOf(":", coordinates.indexOf(locations[i+1])));
				roads += "ctx.beginPath();ctx.strokeStyle=\"" + player.getColor() + "\";ctx.moveTo(" + coor1 + ");ctx.lineTo(" + coor2 + ");ctx.lineWidth=7;ctx.stroke();";
			}
		}
		return roads;
	}
}
