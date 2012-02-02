package com.jircbot;

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
	
	public Snark() {
		System.err.println("Loading Snark module:");
		XmlReader snarkXml = new XmlReader("snark.xml");
		list = snarkXml.toList();
		
		for (Element e : list) {
			System.out.print("Snark: " + e.getAttribute("in") + " -> " + e.getAttribute("out") + ", 1 out of " + e.getAttribute("chance") + " times");
			if (e.getAttribute("author").equals("*")) {
				System.out.println(" for anyone.");
			} else {
				System.out.println(" for " + e.getAttribute("author") + ".");
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
			if (content.contains(e.getAttribute("in"))
				&& (e.getAttribute("author").equals(author) || e.getAttribute("author").equals("*"))) {
				if (randomGenerator.nextInt(Integer.parseInt(e.getAttribute("chance"))) == 0) {
					responses.add(e.getAttribute("out"));
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