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

public class CatanLogin extends HttpServlet {
	static final String catanLoginTemplateLocation = "/Applications/MAMP/tomcat/webapps/examples/WEB-INF/classes/catan_login_template.txt";

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String newGame = request.getParameter("newGame");
		ArrayList<String> parameters = new ArrayList<>();
		for (int i = 1; i <= 6; i++) {
			parameters.add(request.getParameter("playerName" + i));
			parameters.add(request.getParameter("playerColor" + i));
		}
		if (newGame != null) {
			displayPlayers(response, parameters);
		}
	}

	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		displayLogin(response);
		doPost(request, response);
	}

	public Scanner readFile(String fileName) {
		try {
			return new Scanner(new File(fileName));
		} catch (FileNotFoundException e) {
			System.out.println("Invalid file name.");
		}
		return null;
	}

	public void displayLogin(HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();

		Scanner input = readFile(catanLoginTemplateLocation);
		while (input.hasNextLine()) {
			String line = input.nextLine();
			if (line.contains("*NEW PLAYERS")) {
				line = "Player Name:<span class=\"tab\">Color:<br>";
				for (int i = 1; i <= 6; i++) {
					line += i + ". <input type=\"text\" id=\"playerName" + i + "\" size=\"18\" name=\"playerName" + i + "\"/><select name=\"playerColor" + i + "\"><option value=\"SelectColor\">Select Color</option><option value=\"Red\">Red</option><option value=\"Blue\">Blue</option><option value=\"White\">White</option><option value=\"Orange\">Orange</option><option value=\"Green\">Green</option><option value=\"Brown\">Brown</option></select><br>";
				}
			}
			out.print(line + "\n");
		} 
	}

	public void displayPlayers(HttpServletResponse response, ArrayList<String> parameters) throws IOException {
		PrintWriter out = response.getWriter();
		out.print("<br><center><table style=\"border:1px solid black\"><tr style=\"width:100%\"><th style=\"width:25%\"><h3 style=\"font-family:Helvetica;font-size:18px\">Registered Players:</h3></th>");

		ArrayList<String> unique = new ArrayList<>();
		for (String parameter : parameters) {
			if (parameter.length() != 0 && !parameter.equals("SelectColor")) {
				if (unique.contains(parameter)) {
					out.print("<th style=\"color:crimson\">WARNING: The entry " + parameter + " has been entered more than once. Please correct this mistake and resubmit.</th></tr></table></center>");
					return;
				}
				unique.add(parameter);
			}
		}
		String newLine = "";
		String output = "";
		for (int i = 0; i < parameters.size()-1;i=i+2) {
			if (parameters.get(i).length() != 0 && !parameters.get(i+1).equals("SelectColor")) {
				String backgroundColor, textColor;
				if (parameters.get(i+1).toLowerCase().equals("white")) {
					backgroundColor = "darkgrey";
					textColor = "black";
				} else if (parameters.get(i+1).toLowerCase().equals("brown")) {
					backgroundColor = "saddlebrown";
					textColor = "white";
				} else {
					backgroundColor = "dark" + parameters.get(i+1);
					textColor = "white";
				}
				output += "<th style=\"color:" + textColor + ";background:" + backgroundColor + "\">" + parameters.get(i) + "<br>" + parameters.get(i+1) + "</th>";
				newLine += parameters.get(i) + "," + parameters.get(i+1) + ":";
			}
		}
		if (newLine.length() > 2) {
			newLine = newLine.substring(0,newLine.length()-1);
		}
		out.print(output + "</tr></table></center>");
		out.print("<br><h3 style=\"font-family:Helvetica;font-size:16px;text-align:right;padding-right:10px\">Ready to Play?</h3><form action=\"MelissaExample\" method=\"POST\" style=\"text-align:right;padding-right:10px\"><input type=\"hidden\" name=\"newLine\" value=" + newLine + "><strong>I'm Ready! </strong><input type=\"submit\" name=\"next\" value=\">>Let's Go!>>\"/></form>");
	}
}
