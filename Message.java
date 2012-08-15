import java.util.Calendar;
import java.text.SimpleDateFormat;

/*
 * Message
 *
 * A message from the IRC server. Parsed from raw server blargtext.
 *
 * Author: Eli Spiro (elispiro@gmail.com)
 */

public class Message {
	public static final String timeFormat = "HH:mm:ss";
	Calendar cal;
	SimpleDateFormat sdf;
	
	public enum Type {
		JOIN,
		PART,
		ACTION,
		NOTICE,
		PRIVMSG,
		NICK,
		UNKNOWN
	}

	private String timeStamp; // message time
	private String author; // message's sender
	private String user; // message's username
	private Type type; // what type of message. join, part, privmsg
	private String recipient; // message's destination
	private boolean action; // whether the message was a /me action
	private String content; // the message's body text
	private String raw; // the raw incoming line
	
	public Message(String line) {
		raw = line;
		
		try {
		
			// set the timestamp
			cal = Calendar.getInstance();
			sdf = new SimpleDateFormat(timeFormat);
			timeStamp = sdf.format(cal.getTime());
			
			// skip the leading :, author is up to the first !
			author = line.substring(1, line.indexOf('!'));
			
			// remove up to and including the !
			line = line.substring(line.indexOf('!') + 1);
			
			// username is up to the space
			user = line.substring(0, line.indexOf(' '));
			
			// remove up to and including the space
			line = line.substring(line.indexOf(' ') + 1);
			
			// type is up to the next space
			try {
				type = Type.valueOf(line.substring(0, line.indexOf(' ')));
			} catch (IllegalArgumentException e) {
				type = Type.UNKNOWN;
			}
			
			
			// remove up to and including the space
			line = line.substring(line.indexOf(' ') + 1);
			
			if (type.equals(Type.JOIN)) {
				// content (the channel joined) is from the leading : on
				content = line.substring(1);
			}
			else if (type.equals(Type.PART)) {
				// recipient (the channel parted) is the remainder of the message
				recipient = line;
			}
			else if (type.equals(Type.NICK)) {
				// new nick is from the leading : on
				content = line.substring(1);
			}
			
			else {
				// recipient is up to the space
				recipient = line.substring(0, line.indexOf(' '));
								
				// remove up to and including the space, then the :
				line = line.substring(line.indexOf(' ') + 2);
				
				if (line.startsWith(((char)1) + "ACTION")) {
					type = Type.ACTION;
					
					// remove up to and including the first space
					line = line.substring(line.indexOf(' ') + 1);
				}
				
				// content (the channel joined) is from the leading : on
				content = line;
			}
		} catch (Exception e) {
			// if the message crashes us, print it out for debugging!
			
			System.err.println("Failed to parse incoming message. The message was:");
			System.err.println(raw);
		}
	}
	
	public Type getType() {
		return type;
	}

	public String getAuthor() {
		return author;
	}

	public String getContent() {
		return content;
	}

	public String toString() {
		if (type.equals(Type.JOIN)) {
			return "[" + timeStamp + "] " + author + " (" + user + ") has joined " + content + ".";
		}
		else if (type.equals(Type.PART)) {
			return "[" + timeStamp + "] " + author + " has left " + recipient + ".";
		}
		else if (type.equals(Type.ACTION)) {
			return "[" + timeStamp + "] * " + author + " " + content;
		}
		else if (type.equals(Type.NOTICE)) {
			return "[" + timeStamp + "] NOTICE <" + author + "> " + content;
		}		
		else if (type.equals(Type.PRIVMSG)) {
			return "[" + timeStamp + "] <" + author + "> " + content;
		}
		else if (type.equals(Type.NICK)) {
			return "[" + timeStamp + "] * " + author + " is now known as " + content +".";
		}
		else {
			return "I don't know what to do with this message:\n" + raw;
		}
	}
}