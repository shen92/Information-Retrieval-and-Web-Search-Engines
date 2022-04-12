import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class LinkExtract {
	final static String SRC_FOLDER = "/Users/syjack1997/Desktop/HW4/";

	public static void main(String[] args) throws Exception {

		File crawledFiles = new File(SRC_FOLDER + "latimes/");
		File urltoHtmlMap = new File(SRC_FOLDER + "URLtoHTML_latimes_news.csv");

		HashMap<String, String> fileUrlMap = new HashMap<>(); // {filename: url}
		HashMap<String, String> urlFileMap = new HashMap<>(); // {url: filename}

		Scanner scanner = new Scanner(urltoHtmlMap);
		while (scanner.hasNext()) {
			String[] tokens = scanner.next().split(","); // [filename, url]
			fileUrlMap.put(tokens[0], tokens[1]);
			urlFileMap.put(tokens[1], tokens[0]);
		}
		scanner.close();

		Set<String> edgesSet = new HashSet<>();
		for (File file : crawledFiles.listFiles()) {
			Document doc = Jsoup.parse(file, "UTF-8", fileUrlMap.get(file.getName()));
			Elements links = doc.select("a[href]"); // a with href
			for (Element link : links) {
				String url = link.attr("abs:href").trim();
				if (urlFileMap.containsKey(url))
					edgesSet.add(file.getName() + " " + urlFileMap.get(url));
			}
		}

		FileWriter fileWriter = new FileWriter("edgelist.txt");
		// edge: fileA->fileB
		for (String edge : edgesSet) {
			fileWriter.write(edge);
			fileWriter.write("\n");
		}
		fileWriter.close();
	}

}
