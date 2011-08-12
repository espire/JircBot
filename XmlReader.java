/*
XmlReader
*/

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import java.util.ArrayList;

public class XmlReader {
	
	// the root XML node
	Element root;
	
	// given the name of a top-level element, get its text value.
	public String getElement(String tagName) {
		NodeList nl = root.getElementsByTagName(tagName);
		Element el = (Element)nl.item(0);
		return el.getFirstChild().getNodeValue();
	}
	
	// get an Array of the XML file, given the number of columns
	public String[][] toArray() {
		NodeList nodeList = root.getChildNodes();
		String[][] ret = new String[nodeList.getLength()][2];
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			ret[i][0] = nodeList.item(i).getNodeValue();
			ret[i][1] = nodeList.item(i).getFirstChild().getNodeValue();
		}
		return ret;
	}
	
	// get an ArrayList of the elements
	public ArrayList<Element> toList() {
		ArrayList<Element> list = new ArrayList<Element>();
		NodeList nodeList = root.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (nodeList.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
				list.add((Element)(nodeList.item(i)));
			}
		}
		return list;
	}
	
	// read the file in
	private void loadFile(String file) {
		try {
			File connectionInfoFile = new File(file);
			DocumentBuilderFactory connectionInfoFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder connectionInfoBuilder = connectionInfoFactory.newDocumentBuilder();
			Document connectionInfoDocument = connectionInfoBuilder.parse(connectionInfoFile);
			
			System.err.println("Reading XML file " + file + "...");
			root = connectionInfoDocument.getDocumentElement();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public XmlReader(String file) {
		loadFile(file);
	}
}