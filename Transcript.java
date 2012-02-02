import java.util.Stack;

/*
 * Transcript
 *
 * A JircBot module that remembers the conversation and spits it back.
 *
 * Author: Eli Spiro (elispiro@gmail.com)
 */

class Transcript {

	static Stack<Message> transcript; // main transcript
	static Stack<Message> tempScript; // temporary transcript for lst module
	
	public Transcript() {
		System.err.println("Loading Transcript module...");
		
		// Create the transcripts
		transcript = new Stack<Message>();
		tempScript = new Stack<Message>();
		
		System.err.println();
	}
	
	public String feed(Message message) {
		
		String ret = "";
		
		if (message.type.equals("PRIVMSG") && message.content.toLowerCase().startsWith("@lst")) {
			tempScript.clear();
			tempScript.addAll(transcript);
			if (tempScript.empty()) {
				ret = "Nothing to list.";
			}
			else {
				for (int i = 0; i < 5 && !tempScript.isEmpty(); i++) {
					if (i > 0) {
						ret = ",  " + ret;
					}
					ret = tempScript.pop() + ret;
				}
			}
		}
		else if (message.type.equals("PRIVMSG") && message.content.toLowerCase().startsWith("@moar")) {
			if (tempScript.empty()) {
				ret = "No moar.";
			}
			else {
				for (int i=0;i<5 && !tempScript.isEmpty(); i++) {
					if (i>0) {
						ret = ",  " + ret;
					}
					ret = tempScript.pop() + ret;
				}
			}
		}
		
		if (message.type.equals("ACTION") || message.type.equals("PRIVMSG")) {
			transcript.push(message);
		}
		if (message.content.contains("Password accepted -- you are now recognized.")) {
			transcript.clear();
		}
		
		return ret;
	}
}