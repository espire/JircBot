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
	
	ArrayList<Element> list;
	
	public Snark() {
		System.err.println("Loading Snark module:");
		XmlReader snarkXml = new XmlReader("snark.xml");
		list = snarkXml.toList();
		
		for (Element e : list) {
			System.out.println("Snark: " + e.getAttribute("in") + " -> " + e.getAttribute("out") + ", 1 out of " + e.getAttribute("chance") + " times.");
		}
		
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