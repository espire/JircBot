package com.jircbot;

import java.io.*;
import java.net.*;
import java.util.Stack;
import java.util.Random;
import java.lang.String;

/*
 * JircBot
 *
 * An IRC client that does precisely what it means to.
 *
 * Author: Eli Spiro (elispiro@gmail.com)
 */

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

	// Random number generator
	static Random randomGenerator = new Random();

	static Snark snarker;
	static Vote vote;
	static Transcript transcript;
	static Topic topic;

	// Message the channel and flush the buffer.
	public static void say(String line) throws Exception {
		if (!line.equals("")) {
			writer.write("PRIVMSG " + channel + " :" + line + "\r\n");
			writer.flush();
		}
	}

	public static void main(String[] args) throws Exception {
	
		// load in the main configuration XML file
		conf = new XmlReader("jircbot.xml");
		System.out.println();

		// save all the delicious configuration info from the XML file
		server = conf.getElement("server");
		login = conf.getElement("login");
		password = conf.getElement("password");
		nick = conf.getElement("nick");
		name = conf.getElement("name");
		channel = conf.getElement("channel");
		joinMessage = conf.getElement("joinMessage");

		// Temporary variables for manipulation
		Message tempMessage;
		String words = "";
		int percent = 0;
		int index = 0;

		// OUTER LOOP
		// Program loop begins here. If we get disconnected, it will return to the top.
		while (true) {

			// Connect directly to the IRC server.
			System.err.println("Establishing a connection with the server...");
			socket = new Socket(server, 6667);
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Initialize modules.
			snarker = new Snark();
			vote = new Vote();
			transcript = new Transcript();
			topic = new Topic(writer, reader, channel);

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
			mainLoop: while ((line = reader.readLine()) != null) {

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
					
					//  MODULES
					if ((message.type.equals("PRIVMSG") || message.type.equals("ACTION")) && !message.author.equals(nick)) {
						if(message.content.toLowerCase().equals("!reload")) {
							System.err.println();
							break mainLoop;
						}
						say(vote.feed(message));
						say(snarker.feed(message));
						say(transcript.feed(message));
						say(topic.feed(message));
					}
					
				} // else
			} // while read
			
			// close the socket and wait 5 seconds for the server to acknowledge that the connection is closed before we try to connect again
			// shorter wait times resulted in a broken pipe exception
			socket.close();
			Thread.sleep(5000);
			
		} // while true
	} // main
} //class
