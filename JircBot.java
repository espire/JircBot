import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Stack;
import java.util.Random;
import java.lang.String;

// importing the xml file
//import java.io.File; already have it!
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

// For web page title get, to be added eventually
/*import java.io.DataInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;*/

public class JircBot {

	// The server to connect to and our details.
	static String server;
	static String login;
	static String password;
	static String nick;
	static String name;

	// The channel which the bot will join.
	static String channel;
	static String joinMessage;

	// Timestamp format
	public static final String timeFormat = "HH:mm:ss";

	// I/O streams
	static BufferedWriter writer;
	static BufferedReader reader;
	static Socket socket;

	// Message transcript
	static Stack<String> transcript;
	static Stack<String> tempScript;
	static Stack<String> lastFive;

	// Random number generator
	static Random prng = new Random();

	// Did we snark to this line already?
	static boolean snarked = false;

	// snark: string, string, string, int -> might say something
	// Randomly reply to a given keyword
	public static void snark(String line, String lookFor, String snark, int chance) throws Exception {
		if(!snarked) {
			if(body(line).toLowerCase().contains(lookFor)) {
				if(prng.nextInt(chance) == 0) {
					say(snark);
					snarked = true;
				}
			}
		}
	}

	// Message the channel and flush the buffer.
	public static void say(String message) throws Exception {
		writer.write("PRIVMSG " + channel + " :" + message + "\r\n");
		writer.flush();
	}

	// Get the sender of a raw line
	public static String sender(String line) {
		return line.substring(1,line.indexOf('!'));
	}

	// Get the body of a raw line
	public static String body(String line) {
		return line.substring((line.indexOf(':', 1) + 1), line.length());
	}

	// Clean up a raw line and add time
	public static String clean(String line) {
		return "[" + time() + "] <" + sender(line) + "> " + body(line);
	}

	// Get the time
	public static String time() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
		return sdf.format(cal.getTime());
	}

	public static String getElementText(Element ele, String tagName) {
		NodeList nl = ele.getElementsByTagName(tagName);
		Element el = (Element)nl.item(0);
		return el.getFirstChild().getNodeValue();
	}

	public static void loadConnection() {
		// Let's read an Ex Em El!
		try {
			File connectionInfoFile = new File("jircbot.xml");
			DocumentBuilderFactory connectionInfoFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder connectionInfoBuilder = connectionInfoFactory.newDocumentBuilder();
			Document connectionInfoDocument = connectionInfoBuilder.parse(connectionInfoFile);
			connectionInfoDocument.getDocumentElement().normalize();
			
			System.out.println("Reading configuration file jircbot.xml...");
			Node connectionNode = connectionInfoDocument.getDocumentElement();
			NodeList connectionNodeList = connectionNode.getChildNodes();
			
			server = getElementText((Element)connectionNode, "server");
			login = getElementText((Element)connectionNode, "login");
			password = getElementText((Element)connectionNode, "password");
			nick = getElementText((Element)connectionNode, "nick");
			name = getElementText((Element)connectionNode, "name");
			channel = getElementText((Element)connectionNode, "channel");
			joinMessage = getElementText((Element)connectionNode, "joinMessage");
			
			System.out.println("Server: " + server);
			System.out.println("Login: " + login);
			System.out.println("Password: " + password);
			System.out.println("Nick: " + nick);
			System.out.println("Name: " + name);
			System.out.println("Channel: " + channel);
			System.out.println("Join message: " + joinMessage);
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		
		loadConnection();

		// Create the transcripts
		transcript = new Stack<String>();
		tempScript = new Stack<String>();
		lastFive = new Stack<String>();

		// Temporary variables for manipulation
		String words = "";
		int percent = 0;
		int index = 0;

		// Program loop begins here. If we get disconnected, it will return to the top.
		while(true) {

			// Connect directly to the IRC server.
			socket = new Socket(server, 6667);
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Log on to the server.
			writer.write("NICK " + nick + "\r\n");
			writer.write("USER " + login + " 0 * : " + name + "\r\n");
			writer.flush();

			// Read lines from the server until it ends the MOTD.
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.indexOf("376") >= 0) {
					// We are now logged in.
					break;
				}
				// The nick is already in use
				else if (line.indexOf("433") >= 0) {
					System.out.println("Nickname is already in use.");
					return;
				}
			}

			// Identify with NickServ
			writer.write("PRIVMSG NickServ IDENTIFY "+ password +"\r\n");
			writer.flush();

			// Join the channel.
			writer.write("JOIN " + channel + "\r\n");
			writer.flush();

			try {
				Thread.sleep(500);
			}
			catch (Exception e) {}

			say(joinMessage);

			// Keep reading lines from the server.
			while ((line = reader.readLine( )) != null) {

				// We haven't snarked yet
				snarked = false;

				// Print the raw line received by the bot.
				System.out.println(line);

				if (line.toLowerCase().startsWith(":" + nick)) {}
				else if (line.toLowerCase().startsWith(":nickserv!")) {}
				else if (line.toLowerCase().startsWith(":chanserv!")) {}

				else if (line.toLowerCase( ).startsWith("ping ")) {
					// We must respond to PINGs to avoid being disconnected.
					writer.write("PONG " + line.substring(5) + "\r\n");
					writer.flush();
				}
				else {


					if (body(line.toLowerCase()).startsWith("!lst")) {

						tempScript.clear();
						tempScript.addAll(transcript);
						if(tempScript.empty()) {
							say("Nothing to list.");
						}
						else {
							for(int i=0;i<5 && !tempScript.isEmpty(); i++) {
								if(i>0) {
									words = "  " + words;
								}
								words = tempScript.pop() + words;
							}
							say(words);
							words = "";
						}
					}
					else if(body(line.toLowerCase()).startsWith("!moar")) {
						if(tempScript.empty()) {
							say("No moar.");
						}
						else {
							for(int i=0;i<5 && !tempScript.isEmpty(); i++) {
								if(i>0) {
									words = "  " + words;
								}
								words = tempScript.pop() + words;
							}
							say(words);
							words = "";
						}
					}
					else if(body(line.toLowerCase()).startsWith("!topic ")) {
						writer.write("TOPIC " + channel + " :" + body(line).substring(7) + "\r\n");
						writer.flush();
						System.out.println("TOPIC " + channel + " :" + body(line).substring(7));
					}
					else if(body(line.toLowerCase()).equals("!topic")) {
						writer.write("TOPIC " + channel + "\r\n");
						writer.flush();
						words = reader.readLine();
						System.out.println(words);
						say("Topic is: " + body(words));
						words = reader.readLine();
						System.out.println(words);
						words = "";
					}
					else if(body(line.toLowerCase()).startsWith("!vote")) {
						percent = prng.nextInt(100) + 1;
						if(body(line).length() > 6) {
							words = body(line).substring(6);
							say(words + "? Yes: " + percent + " No: " + (100-percent));
							words = "";
							}
						else {
							say("Yes: " + percent + " No: " + (100-percent));
						}
						if(percent == 50) {
							say("~");
						}
					}

					else {
						snark(line, "<3", "in bed!", 2);
						snark(line, "love", "in bed!", 2);
						snark(line, " want ", "in bed!", 12);
						snark(line, " can ", "in bed!", 12);
						snark(line, " like", "in bed!", 12);
						snark(line, " should", "in bed!", 12);
						snark(line, " use ", "in bed!", 12);
						snark(line, " try ", "in bed!", 12);
						snark(line, ":o", ":O", 3);
					}

					if(line.toLowerCase().contains("privmsg")) {
						transcript.push(clean(line));
					}
					if(line.equals(
					":NickServ!services@opera.com NOTICE " + login + " :Password accepted -- you are now recognized.")) {
						transcript.clear();
					}

				} // else
			} // while read
		} // while true
	} // main
} //class