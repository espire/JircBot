import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Link
 *
 * A JircBot module that prints the title of a linked web page.
 * Only the first URL is explored (if any), and its title is printed in the
 * format:
 * title | url_root
 *
 * Author: Eli Spiro (elispiro@gmail.com)
 */
class Link implements JircModule {
	
	final String URL_REGEX =
			"(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

	final String ROOT_REGEX ="(https?)://[-a-zA-Z0-9.]*";
	final Pattern ROOT_PATTERN = Pattern.compile(ROOT_REGEX);

	final String TITLE_OPEN = "<title>";
	final String TITLE_CLOSE = "</title>";
	final String TITLE_REGEX = "<title>.*</title>";
	final Pattern TITLE_PATTERN = Pattern.compile(TITLE_REGEX);

	Link(String command) {
		System.err.println("Loading Link module...\n");
	}

 	public String feed(Message message) {
		try {
			Matcher matcher = URL_PATTERN.matcher(message.getContent());
			if (matcher.find()) {
				return this.fetchTitle(new URL(matcher.group()));
			}
		} catch (Exception e) {
			// Just ignore the line
		}

		return "";
	}

	private String fetchTitle(URL url) {
		String title = "";

		try {
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(url.openStream())
			);

			String pageContent = "";
			String line;
			while((line = reader.readLine()) != null) {
				pageContent += line;
			}

			Matcher titleMatcher = TITLE_PATTERN.matcher(pageContent);
			Matcher rootMatcher = ROOT_PATTERN.matcher(url.toString());

			if (titleMatcher.find()) {
				title = titleMatcher.group();
				title = title.replaceAll(TITLE_OPEN, "");
				title = title.replaceAll(TITLE_CLOSE, "");

				if (rootMatcher.find()) {
					title += (" | " + rootMatcher.group());
				}
			}

			reader.close();

		} catch (Exception e) {
			// Just ignore this URL
		}

		return title;
	}
}