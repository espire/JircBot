import org.w3c.dom.Element;
import java.util.ArrayList;
import java.util.Random;

class Vote {
	
	String nick;
	
	// Random number generator
	Random randomGenerator = new Random();
	
	ArrayList<Element> list;
	
	public Snark(String nick) {
		System.err.println("Loading Snark module:");
		XmlReader snarkXml = new XmlReader("snark.xml");
		list = snarkXml.toList();
		this.nick = nick;
		
		for (Element e : list) {
			System.out.println("Snark: " + e.getAttribute("in") + " -> " + e.getAttribute("out") + ", 1 out of " + e.getAttribute("chance") + " times.");
		}
		
		System.err.println();
	}
	
	public String feed(Message message) {
		
		percent = randomGenerator.nextInt(100) + 1;
		
		String ret;
		
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
		else {
			ret = "";
		}
		
		return ret;
	}
}