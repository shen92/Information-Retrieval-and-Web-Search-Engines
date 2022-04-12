import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;

import edu.uci.ics.crawler4j.crawler.*;
import edu.uci.ics.crawler4j.fetcher.*;
import edu.uci.ics.crawler4j.robotstxt.*;

public class Controller {

	public static void main(String[] args) throws Exception {

		CrawlConfig config = new CrawlConfig();

		config.setCrawlStorageFolder(Configs.CRAWLER_STORAGE_PATH);
		config.setMaxPagesToFetch(Configs.MAX_PAGES_TO_FETCH);
		config.setMaxDepthOfCrawling(Configs.MAX_DEPTH_OF_CRAWLING);
		// config.setPolitenessDelay(politenessDelay);
		config.setIncludeBinaryContentInCrawling(Configs.INCLUDE_BINARY_CONTENT_IN_CRAWLING);

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

		controller.addSeed(Configs.SEED_URL);
		controller.start(MyCrawler.class, Configs.NUM_CRAWLERS);

		List<Object> crawlersLocalData = controller.getCrawlersLocalData();
		CrawlData crawlData = new CrawlData();

		for (Object item : crawlersLocalData) {
			CrawlData data = (CrawlData) item;
			crawlData.fetchedURLs.addAll(data.fetchedURLs);
			crawlData.visitedURLs.addAll(data.visitedURLs);
			crawlData.discoveredURLs.addAll(data.discoveredURLs);
		}

		File outputDirectory = new File(Configs.OUTPUT_PATH);
		if (!outputDirectory.exists()) {
			outputDirectory.mkdir();
		}
		// If you require it to make the entire directory path including parents,

		// Task 1 results
		File newFile = new File(Configs.OUTPUT_PATH + "fetch_" + Configs.SEED_SITE_NAME + ".csv");
		newFile.delete();
		newFile.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(newFile, true));
		bw.append("Fetched URL,Status Code\n");

		for (FetchedURL fetchedURL : crawlData.fetchedURLs) {
			bw.append(fetchedURL.url + "," + fetchedURL.statusCode + "\n");
		}
		bw.close();

		// Task 2 results
		newFile = new File(Configs.OUTPUT_PATH + "visit_" + Configs.SEED_SITE_NAME + ".csv");
		newFile.delete();
		newFile.createNewFile();
		bw = new BufferedWriter(new FileWriter(newFile, true));
		bw.write("Downloaded URL,Size (Bytes),Num of outlinks,Content-Type\n");

		for (VisitedURL visitedURL : crawlData.visitedURLs) {
			bw.append(
					visitedURL.url + "," + visitedURL.size + "," + visitedURL.numOutlinks + "," + visitedURL.contentType + "\n");
		}
		bw.close();

		// Task 3 results
		newFile = new File(Configs.OUTPUT_PATH + "urls_" + Configs.SEED_SITE_NAME + ".csv");
		newFile.delete();
		newFile.createNewFile();
		bw = new BufferedWriter(new FileWriter(newFile, true));
		bw.write("URL,Residence Indicator\n");

		for (DiscoveredURL discoveredURL : crawlData.discoveredURLs) {
			bw.append(discoveredURL.url + "," + discoveredURL.indicator + "\n");
		}
		bw.close();

		int numAttemptedFetches = crawlData.fetchedURLs.size();

		HashMap<Integer, Integer> statusCodesMap = new HashMap<Integer, Integer>();

		for (FetchedURL fetchedURL : crawlData.fetchedURLs) {
			int statusCode = fetchedURL.statusCode;
			statusCodesMap.put(statusCode, statusCodesMap.getOrDefault(statusCode, 0) + 1);
		}

		int numSucceededFetches = statusCodesMap.get(200);
		int numAbortedOrFailedFetches = numAttemptedFetches - numSucceededFetches;

		int totalURLsExtracted = crawlData.discoveredURLs.size();
		int numUniqueURLsWithinNewsSite = 0;

		HashSet<String> uniqueDiscoveredURLsSet = new HashSet<String>();

		for (DiscoveredURL discoveredURL : crawlData.discoveredURLs) {
			if (!uniqueDiscoveredURLsSet.contains(discoveredURL.url)) {
				if (discoveredURL.indicator == "OK") {
					numUniqueURLsWithinNewsSite++;
				}
				uniqueDiscoveredURLsSet.add(discoveredURL.url);
			}
		}

		int numUniqueURLsExtracted = uniqueDiscoveredURLsSet.size();
		int numUniqueURLsOutsideNewsSite = numUniqueURLsExtracted - numUniqueURLsWithinNewsSite;

		int numOneK = 0;
		int numTenK = 0;
		int numHundredK = 0;
		int numOneM = 0;
		int numOtherSize = 0;

		HashMap<String, Integer> contentTypesMap = new HashMap<String, Integer>();

		for (VisitedURL visitedURL : crawlData.visitedURLs) {
			if (visitedURL.size < 1024) {
				numOneK++;
			} else if (visitedURL.size < 1024 * 10) {
				numTenK++;
			} else if (visitedURL.size < 1024 * 100) {
				numHundredK++;
			} else if (visitedURL.size < 1024 * 1024) {
				numOneM++;
			} else {
				numOtherSize++;
			}

			String contentType = visitedURL.contentType;
			contentTypesMap.put(contentType, contentTypesMap.getOrDefault(contentType, 0) + 1);
		}

		newFile = new File(Configs.OUTPUT_PATH + "CrawlReport_" + Configs.SEED_SITE_NAME + ".txt");
		newFile.delete();
		newFile.createNewFile();
		bw = new BufferedWriter(new FileWriter(newFile, true));

		bw.write("Name: " + Configs.NAME + "\n");
		bw.write("USC ID: " + Configs.USC_ID + "\n");
		bw.write("News site crawled: " + Configs.SEED_SITE_NAME + ".com\n");
		bw.write("Number of threads: " + Configs.NUM_CRAWLERS + "\n\n");

		bw.write("Fetch Statistics\n");
		bw.write("================\n");
		bw.write("# fetches attempted: " + numAttemptedFetches + "\n");
		bw.write("# fetches succeeded: " + numSucceededFetches + "\n");
		bw.write("# fetches failed or aborted: " + numAbortedOrFailedFetches + "\n\n");

		bw.write("Outgoing URLs:\n");
		bw.write("==============\n");
		bw.write("Total URLs extracted: " + totalURLsExtracted + "\n");
		bw.write("# unique URLs extracted: " + numUniqueURLsExtracted + "\n");
		bw.write("# unique URLs within News Site: " + numUniqueURLsWithinNewsSite + "\n");
		bw.write("# unique URLs outside News Site: " + numUniqueURLsOutsideNewsSite + "\n\n");

		bw.write("Status Codes:\n");
		bw.write("===========\n");
		List<Integer> statusCodesList = new ArrayList<>(statusCodesMap.keySet());
		Collections.sort(statusCodesList);
		for (int statusCode : statusCodesList) {
			bw.write(statusCode + " " + HttpStatus.getStatusText(statusCode) + ": " + statusCodesMap.get(statusCode) + "\n");
		}
		bw.write("\n");

		bw.write("File Sizes:\n");
		bw.write("===========\n");
		bw.write("< 1KB: " + numOneK + "\n");
		bw.write("1KB ~ <10KB: " + numTenK + "\n");
		bw.write("10KB ~ <100KB: " + numHundredK + "\n");
		bw.write("100KB ~ <1MB: " + numOneM + "\n");
		bw.write(">= 1MB: " + numOtherSize + "\n\n");

		bw.write("Content Types:\n");
		bw.write("==============\n");

		for (String type : contentTypesMap.keySet()) {
			bw.write(type + ": " + contentTypesMap.get(type) + "\n");
		}
		bw.close();

		for (int key : statusCodesMap.keySet()) {
			System.out.println(key + " " + HttpStatus.getStatusText(key) + ": " + statusCodesMap.get(key));
		}
	}

}
