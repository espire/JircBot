import java.io.*;

class Topic {
	
	// the BufferedWriter and Reader that communicate with the IRC server. These are passed on construction.
	static BufferedWriter writer;
	static BufferedReader reader;
	
	// the channel name, passed on construction
	String channel;
	
	Message tempMessage;
	
	public Topic(BufferedWriter writer, BufferedReader reader, String channel) {
		System.err.println("Loading Topic module...");
		
		this.writer = writer;
		this.reader = reader;
		this.channel = channel;
		
		System.err.println();
	}
	
	public String feed(Message message) throws Exception {
		
		String ret = "";
		
	 	if (message.type.equals("PRIVMSG") && message.content.toLowerCase().startsWith("!topic ") && message.content.length() > 7 ) {
			writer.write("TOPIC " + channel + " :" + message.content.substring(7) + "\r\n");
			writer.flush();
		}
		else if (message.type.equals("PRIVMSG") && message.content.toLowerCase().equals("!topic")) {
			writer.write("TOPIC " + channel + "\r\n");
			writer.flush();
			String temp = reader.readLine();
			ret = "Topic is: " + temp.substring(temp.substring(1).indexOf(':') + 2);
		}
		
		return ret;
	}
}