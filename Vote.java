import java.util.Random;

/*
 * Vote
 *
 * A JircBot module that votes on things.
 *
 * Author: Eli Spiro (elispiro@gmail.com)
 */

class Vote {
	
	// Random number generator
	Random randomGenerator = new Random();
	
	// command prefix
	String command;
	
	public Vote(String command) {
		System.err.println("Loading Vote module...");
		System.err.println();
		
		this.command = command;
	}
	
	public String feed(Message message) {
		
		int percent = randomGenerator.nextInt(100) + 1;
		
		String ret = "";
		String question = "";
		
		if (message.getContent().toLowerCase().startsWith(command + "vote")) {
			if (message.getContent().length() > 6 &&
					message.getContent().toLowerCase().startsWith(command + "vote ")) {
				question = message.getContent().substring(6);
				while(question.charAt(question.length()-1) == '?') {
					question = question.substring(0,question.length()-2);
				}
				ret = question + "? Yes: " + percent + " No: " + (100 - percent);
			}
			else if (message.getContent().length() <= 6) {
				ret = "Yes: " + percent + " No: " + (100 - percent);
			}
			
			if (percent == 50) {
				ret += "~";
			}
		}
		
		return ret;
	}
}