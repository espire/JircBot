import org.w3c.dom.Element;
import java.util.ArrayList;
import java.util.Random;

/*
 * Snark
 *
 * A context-sensitive snarking module for JircBot.
 *
 * Author: Eli Spiro (elispiro@gmail.com)
 */

class Snark {
	
	// Random number generator
	Random randomGenerator = new Random();
	
	// the list of words to snark
	ArrayList<Element> list;
	
	// the command prefix
	String command;
	
	public Snark(String command) {
		
		this.command = command;
		
		System.err.println("Loading Snark module:");
		XmlReader snarkXml = new XmlReader("snark.xml");
		list = snarkXml.toList();
		
		for (Element e : list) {
			if (e.getTagName().equals("snark")) {
				System.out.print("Snark: ");
			}
			else if (e.getTagName().equals("replace")) {
				System.out.print("Replace: ");
			}
			else if (e.getTagName().equals("prefix")) {
				System.out.println("Prefix: ");
			}
			
			System.out.print(e.getAttribute("in") + " -> " + e.getAttribute("out") + ", ");
			
			if (e.getAttribute("chance").equals("")) {
				System.out.print("every time");
			} else {
				System.out.print("1 out of " + e.getAttribute("chance") + " times ");
			}
			
			if (e.getAttribute("author").equals("")) {
				System.out.println("for anyone.");
			} else {
				System.out.println("for " + e.getAttribute("author") + ".");
			}
		}
		
		System.err.println();
	}
	
	public String feed(Message message) {
		
		ArrayList<String> responses = new ArrayList<String>();
		String content = message.content.toLowerCase();
		String author = message.author.toLowerCase();
		
		// go through the list of snarks and give each one a chance to snark
		for (Element e : list) {
			if (e.getAttribute("author").equals(author) || e.getAttribute("author").equals("")) {
				if (e.getAttribute("chance").equals("") || randomGenerator.nextInt(Integer.parseInt(e.getAttribute("chance"))) == 0) {
					if (e.getTagName().equals("snark") && content.contains(e.getAttribute("in"))) {
						responses.add(e.getAttribute("out"));
					}
					else if (e.getTagName().equals("replace") && content.contains(e.getAttribute("in"))) {
						responses.add(content.replace(e.getAttribute("in"), e.getAttribute("out")));
					}
					else if (e.getTagName().equals("prefix") && content.startsWith(e.getAttribute("in"))) {
						responses.add(content.replaceFirst(e.getAttribute("in"), e.getAttribute("out")));
					}
				}
			}
		}

		// if more than one snark responded, choose a random one
		if (!responses.isEmpty()) {
			return responses.get(randomGenerator.nextInt(responses.size()));
		}
		
		return "";
	}
}