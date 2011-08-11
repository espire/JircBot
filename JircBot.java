import java.io.*;
import java.net.*;
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
	
	static XmlReader conf;

	// I/O streams
	static BufferedWriter writer;
	static BufferedReader reader;
	static Socket socket;

	// Message transcripts
	static Stack<Message> transcript; // main transcript
	static Stack<Message> tempScript; // temporary transcript for lst module

	// Random number generator
	static Random randomGenerator = new Random();

	static Snark snarker;

	// Message the channel and flush the buffer.
	public static void say(String line) throws Exception {
		if (!line.equals("")) {
			writer.write("PRIVMSG " + channel + " :" + line + "\r\n");
			writer.flush();
		}
	}

	public static void main(String[] args) throws Exception {
	
		conf = new XmlReader("jircbot.xml");

		server = conf.getElement("server");
		login = conf.getElement("login");
		password = conf.getElement("password");
		nick = conf.getElement("nick");
		name = conf.getElement("name");
		channel = conf.getElement("channel");
		joinMessage = conf.getElement("joinMessage");
		
		snarker = new Snark(nick);

		// Create the transcripts
		transcript = new Stack<Message>();
		tempScript = new Stack<Message>();

		// Temporary variables for manipulation
		Message tempMessage;
		String words = "";
		int percent = 0;
		int index = 0;

		// OUTER LOOP
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
				// Quit if the nick is already in use
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

			// Delay for half a second, to make sure that the channel is joined before saying the Join Message.
			try {
				Thread.sleep(500);
			}
			catch (Exception e) {}

			say(joinMessage);

			// MAIN LOOP
			// Keep reading lines from the server.
			while ((line = reader.readLine()) != null) {

				if (line.toLowerCase().startsWith(":" + nick) || line.toLowerCase().startsWith(":" + server) || line.toLowerCase().startsWith(":chanserv!")) {}

				else if (line.toLowerCase().startsWith("ping ")) {
					System.out.print("|");
					for(int i = 0; i < randomGenerator.nextInt(16); i++) {
						System.out.print(" ");
					}
					System.out.print(".");
					for(int i = 0; i < randomGenerator.nextInt(16); i++) {
						System.out.print(" ");
					}
					System.out.println("ping!");
					
					// We must respond to PINGs to avoid being disconnected.
					writer.write("PONG " + line.substring(5) + "\r\n");
					writer.flush();
				}
				
				else {
					// create a Message object from the line
					Message message = new Message(line);
					
					// Print the (nicely parsed) line received by the bot.
					System.out.println(message);
					
					// LST MODULE
					if (message.type.equals("PRIVMSG") && message.content.toLowerCase().startsWith("!lst")) {
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
					else if(message.type.equals("PRIVMSG") && message.content.toLowerCase().startsWith("!moar")) {
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
					
					// TOPIC MODULE
					else if(message.type.equals("PRIVMSG") && message.content.toLowerCase().startsWith("!topic ")) {
						writer.write("TOPIC " + channel + " :" + message.content.substring(7) + "\r\n");
						writer.flush();
						System.out.println("TOPIC " + channel + " :" + message.content.substring(7));
					}
					else if(message.type.equals("PRIVMSG") && message.content.toLowerCase().equals("!topic")) {
						writer.write("TOPIC " + channel + "\r\n");
						writer.flush();
						tempMessage = new Message(reader.readLine());
						System.out.println(tempMessage);
						say("Topic is: " + tempMessage.content);
						tempMessage = new Message(reader.readLine());
						System.out.println(words);
					}
					
					// VOTE MODULE
					else if(message.type.equals("PRIVMSG") && message.content.toLowerCase().startsWith("!vote ")) {
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

					// SNARK MODULE
					else {
						say(snarker.feed(message));
					}
					
					// TRANSCRIPT MODULE
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