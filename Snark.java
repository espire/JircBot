import org.w3c.dom.Element;
import java.util.ArrayList;
import java.util.Random;

class Snark {
	
	// Random number generator
	Random randomGenerator = new Random();
	
	public Vote() {
		System.err.println("Loading Vote module:");
		
		System.err.println();
	}
	
	public String feed(Message message) {
		
		ArrayList<String> responses = new ArrayList<String>();
		String content = message.content.toLowerCase();
		
		for (Element e : list) {
			if (content.contains(e.getAttribute("in"))) {
				if (randomGenerator.nextInt(Integer.parseInt(e.getAttribute("chance"))) == 0) {
					responses.add(e.getAttribute("out"));
				}
			}
		}

		if (!responses.isEmpty()) {
			return responses.get(randomGenerator.nextInt(responses.size()));
		}
		
		return "";
	}
}