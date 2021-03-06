import java.io.*;

/*
 * Topic
 *
 * A JircBot module that reads and writes the topic.
 *
 * Author: Eli Spiro (elispiro@gmail.com)
 */

class Topic {
	
	// the BufferedWriter and Reader that communicate with the IRC server. These are passed on construction.
	static BufferedWriter writer;
	static BufferedReader reader;
	
	// the channel name, passed on construction
	String channel;
	
	Message tempMessage;
	
	// the command prefix
	String command;
	
	public Topic(String command, BufferedWriter writer, BufferedReader reader, String channel) {
		
		this.command = command;
		
		System.err.println("Loading Topic module...");
		
		this.writer = writer;
		this.reader = reader;
		this.channel = channel;
		
		System.err.println();
	}
	
	public String feed(Message message) throws Exception {
		
		String ret = "";
		
	 	if (message.getType().equals(Message.Type.PRIVMSG) &&
	 			message.getContent().toLowerCase().startsWith(command + "topic ") &&
	 			message.getContent().length() > 7 ) {
			writer.write("TOPIC " + channel + " :" + message.getContent().substring(7) + "\r\n");
			writer.flush();
		}
		else if (message.getType().equals(Message.Type.PRIVMSG) &&
				message.getContent().toLowerCase().equals(command + "topic")) {
			writer.write("TOPIC " + channel + "\r\n");
			writer.flush();
			String temp = reader.readLine();
			ret = "Topic is: " + temp.substring(temp.substring(1).indexOf(':') + 2);
		}
		
		return ret;
	}
}