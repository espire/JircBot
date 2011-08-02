import java.util.Calendar;
import java.text.SimpleDateFormat;

public class Message {
	public static final String timeFormat = "HH:mm:ss";
	Calendar cal;
	SimpleDateFormat sdf;
	
	public String timeStamp;
	public String author;
	public String content;
	
	public Message(String line) {
		cal = Calendar.getInstance();
		sdf = new SimpleDateFormat(timeFormat);
		timeStamp = sdf.format(cal.getTime());
		author = line.substring(1,line.indexOf('!'));
		content = line.substring((line.indexOf(':', 1) + 1), line.length());
	}
	
	public String toString() {
		return "[" + timeStamp + "] <" + author + "> " + content;
	}
}