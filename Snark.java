import org.w3c.dom.Element;
import java.util.ArrayList;
import java.util.Random;

class Snark {
	
	String nick;
	
	// Random number generator
	Random randomGenerator = new Random();
	
	ArrayList<Element> list;
	
	public Snark(String nick) {
		XmlReader snarkXml = new XmlReader("snark.xml");
		list = snarkXml.toList();
		this.nick = nick;
		
		for(Element e : list) {
			System.out.println("Snark enabled: " + e.getAttribute("in") + " -> " + e.getAttribute("out") + ", 1 in " + e.getAttribute("chance") + " times.");
		}
	}
	
	public String feed(Message message) {
		
		if(message.type.equals("PRIVMSG") && !message.author.equals(nick)) { // is that second part necessary?
			ArrayList<String> responses = new ArrayList<String>();
			String content = message.content.toLowerCase();
			
			for(Element e : list) {
				if (content.contains(e.getAttribute("in"))) {
					if (randomGenerator.nextInt(Integer.parseInt(e.getAttribute("chance"))) == 0) {
						responses.add(e.getAttribute("out"));
					}
				}
			}
			
			if (!responses.isEmpty()) {
				return responses.get(randomGenerator.nextInt(responses.size()));
			}
		}
		
		return "";
	}
}