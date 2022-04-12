import java.util.ArrayList;

public class CrawlData {


	ArrayList<FetchedURL> fetchedURLs;
	ArrayList<VisitedURL> visitedURLs;
	ArrayList<DiscoveredURL> discoveredURLs;

	public CrawlData() {
		fetchedURLs = new ArrayList<FetchedURL>();
		visitedURLs = new ArrayList<VisitedURL>();
		discoveredURLs = new ArrayList<DiscoveredURL>();
	}

	public void addFetchedUrls(String url, int statusCode) {
		this.fetchedURLs.add(new FetchedURL(url, statusCode));
	}

	public void addVisitedUrls(String url, int size, int numOutlinks, String contentType) {
		this.visitedURLs.add(new VisitedURL(url, size, numOutlinks, contentType));
	}

	public void addDiscoveredUrls(String url, String indicator) {
		this.discoveredURLs.add(new DiscoveredURL(url, indicator));
	}
}
