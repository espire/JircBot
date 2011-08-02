import java.io.*;
import java.net.*;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
import java.util.Stack;
import java.util.Random;
import java.lang.String;

// For web page title get, to be added eventually
/*import java.io.DataInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;*/

public class JircBot {

	// Connection details. These will be loaded in from the configuration XML file.
	static String server;
	static String login;
	static String password;
	static String nick;
	static String name;
	static String channel;
	static String joinMessage;
	
	static ConfigReader conf;

	// Timestamp format
	public static final String timeFormat = "HH:mm:ss";

	// I/O streams
	static BufferedWriter writer;
	static BufferedReader reader;
	static Socket socket;

	// Message transcript
	static Stack<Message> transcript;
	static Stack<Message> tempScript;
	static Stack<Message> lastFive;

	// Random number generator
	static Random randomGenerator = new Random();

	// Did we snark to this line already?
	static boolean snarked = false;

	// snark: string, string, string, int -> might say something
	// Randomly reply to a given keyword
	public static void snark(Message message, String lookFor, String snark, int chance) throws Exception {
		if(!snarked) {
			if(message.content.toLowerCase().contains(lookFor)) {
				if(randomGenerator.nextInt(chance) == 0) {
					say(snark);
					snarked = true;
				}
			}
		}
	}

	// Message the channel and flush the buffer.
	public static void say(String line) throws Exception {
		writer.write("PRIVMSG " + channel + " :" + line + "\r\n");
		writer.flush();
	}

	public static void main(String[] args) throws Exception {
	
		conf = new ConfigReader("jircbot.xml");

		server = conf.server;
		login = conf.login;
		password = conf.password;
		nick = conf.nick;
		name = conf.name;
		channel = conf.channel;
		joinMessage = conf.joinMessage;

		// Create the transcripts
		transcript = new Stack<Message>();
		tempScript = new Stack<Message>();
		lastFive = new Stack<Message>();

		// Temporary variables for manipulation
		Message tempMessage;
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
				
				System.out.println(line);
				
				// We haven't snarked yet
				snarked = false;

				if (line.toLowerCase().startsWith(":" + nick)) {}
				else if (line.toLowerCase().startsWith(":" + server)) {}
				else if (line.toLowerCase().startsWith(":chanserv!")) {}
				else if (line.toLowerCase().startsWith(":chanserv!")) {}

				else if (line.toLowerCase().startsWith("ping ")) {
					// We must respond to PINGs to avoid being disconnected.
					writer.write("PONG " + line.substring(5) + "\r\n");
					writer.flush();
				}
				else {
					// create a Message object from the line
					Message message = new Message(line);
					// Print the raw line received by the bot.
					//System.out.println(message);
					
					if (message.content.toLowerCase().startsWith("!lst")) {

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
					else if(message.content.toLowerCase().startsWith("!moar")) {
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
					else if(message.content.toLowerCase().startsWith("!topic ")) {
						writer.write("TOPIC " + channel + " :" + message.content.substring(7) + "\r\n");
						writer.flush();
						System.out.println("TOPIC " + channel + " :" + message.content.substring(7));
					}
					else if(message.content.toLowerCase().equals("!topic")) {
						writer.write("TOPIC " + channel + "\r\n");
						writer.flush();
						tempMessage = new Message(reader.readLine());
						System.out.println(tempMessage);
						say("Topic is: " + tempMessage.content);
						tempMessage = new Message(reader.readLine());
						System.out.println(words);
					}
					else if(message.content.toLowerCase().startsWith("!vote ")) {
						percent = randomGenerator.nextInt(100) + 1;
						if(message.content.length() > 6) {
							words = message.content.substring(6);
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
						snark(message, "<3", "in bed!", 2);
						snark(message, "love", "in bed!", 2);
						snark(message, " want ", "in bed!", 12);
						snark(message, " can ", "in bed!", 12);
						snark(message, " like", "in bed!", 12);
						snark(message, " should", "in bed!", 12);
						snark(message, " use ", "in bed!", 12);
						snark(message, " try ", "in bed!", 12);
						snark(message, ":o", ":O", 3);
					}

					if(line.toLowerCase().contains("privmsg")) {
						transcript.push(message);
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