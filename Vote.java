import org.w3c.dom.Element;
import java.util.ArrayList;
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
		
		if (message.content.toLowerCase().startsWith("!vote")) {
			if (message.content.length() > 6 && message.content.toLowerCase().startsWith("!vote ")) {
				ret = message.content.substring(6) + "? Yes: " + percent + " No: " + (100 - percent);
			}
			else if (message.content.length() == 5) {
				ret = "Yes: " + percent + " No: " + (100 - percent);
			}
			
			if (percent == 50) {
				ret += "~";
			}
		}
		
		return ret;
	}
}