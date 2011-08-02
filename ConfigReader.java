import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

public class ConfigReader {
	
	// Connection details. These will be loaded in from jircbot.xml.
	public static String server;
	public static String login;
	public static String password;
	public static String nick;
	public static String name;
	public static String channel;
	public static String joinMessage;
	
	public static String getElementText(Element ele, String tagName) {
		NodeList nl = ele.getElementsByTagName(tagName);
		Element el = (Element)nl.item(0);
		return el.getFirstChild().getNodeValue();
	}
	
	private static void loadConnection(String file) {
		try {
			File connectionInfoFile = new File("jircbot.xml");
			DocumentBuilderFactory connectionInfoFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder connectionInfoBuilder = connectionInfoFactory.newDocumentBuilder();
			Document connectionInfoDocument = connectionInfoBuilder.parse(connectionInfoFile);
			connectionInfoDocument.getDocumentElement().normalize();
		
			System.out.println("Reading configuration file jircbot.xml...");
			Node connectionNode = connectionInfoDocument.getDocumentElement();
			NodeList connectionNodeList = connectionNode.getChildNodes();
		
			server = getElementText((Element)connectionNode, "server");
			login = getElementText((Element)connectionNode, "login");
			password = getElementText((Element)connectionNode, "password");
			nick = getElementText((Element)connectionNode, "nick");
			name = getElementText((Element)connectionNode, "name");
			channel = getElementText((Element)connectionNode, "channel");
			joinMessage = getElementText((Element)connectionNode, "joinMessage");
		
			System.out.println("Server: " + server);
			System.out.println("Login: " + login);
			System.out.println("Password: " + password);
			System.out.println("Nick: " + nick);
			System.out.println("Name: " + name);
			System.out.println("Channel: " + channel);
			System.out.println("Join message: " + joinMessage);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public ConfigReader(String file) {
		loadConnection(file);
	}
}