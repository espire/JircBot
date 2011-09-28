import java.util.Random;

class Vote {
	
	// Random number generator
	Random randomGenerator = new Random();
	
	public Vote() {
		System.err.println("Loading Vote module...");
		System.err.println();
	}
	
	public String feed(Message message) {
		
		int percent = randomGenerator.nextInt(100) + 1;
		
		String ret = "";
		String question = "";
		
		if (message.content.toLowerCase().startsWith("!vote")) {
			if (message.content.length() > 6 && message.content.toLowerCase().startsWith("!vote ")) {
				question = message.content.substring(6);
				while(question.charAt(question.length()-1) == '?') {
					question = question.substring(0,question.length()-2);
				}
				ret = question + "? Yes: " + percent + " No: " + (100 - percent);
			}
			else if (message.content.length() <= 6) {
				ret = "Yes: " + percent + " No: " + (100 - percent);
			}
			
			if (percent == 50) {
				ret += "~";
			}
		}
		
		return ret;
	}
}