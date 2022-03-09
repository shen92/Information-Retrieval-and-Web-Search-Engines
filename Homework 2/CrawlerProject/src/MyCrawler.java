import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.*;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawler extends WebCrawler {
	CrawlData crawlData;

	private static final Pattern DOC_EXTENSIONS = Pattern.compile(".*(\\.(html?|php|pdf|docx?))$");
	private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(".*(\\.(jpe?g|ico|png|bmp|svg|gif|webp|tiff))$");

	public MyCrawler() {
		crawlData = new CrawlData();
	}

	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = formatURL(url.getURL());

		if (href.startsWith(Configs.SEED_SITE_DOMAIN)) {
			crawlData.addDiscoveredUrls(url.getURL(), "OK");
		} else {
			crawlData.addDiscoveredUrls(url.getURL(), "N_OK");
			return false;
		}

		return !hasExtension(url) || hasRequiredExtension(url);
	}

	private String formatURL(String url) {
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}

		return url.toLowerCase().replace(",", "_").replaceFirst("^(https?://)?(www.)?", "");
	}

	private boolean hasExtension(WebURL url) {
		String path = url.getPath();
		String filename = path.substring(path.lastIndexOf("/") + 1);
		return filename.contains(".");
	}

	private boolean hasRequiredExtension(WebURL url) {
		String path = url.getPath().toLowerCase();
		return DOC_EXTENSIONS.matcher(path).matches() || IMAGE_EXTENSIONS.matcher(path).matches();
	}

	@Override
	public void handlePageStatusCode(WebURL url, int statusCode, String statusDescription) {
		crawlData.addFetchedUrls(url.getURL(), statusCode);
	}

	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();
		String contentType = page.getContentType().toLowerCase().split(";")[0];

		if (isRequiredContentType(contentType)) {
			int pageSize = page.getContentData().length;
			int numOutlinks = page.getParseData().getOutgoingUrls().size();
			crawlData.addVisitedUrls(url, pageSize, numOutlinks, contentType);
		}

	}

	private boolean isRequiredContentType(String contentType) {
		return contentType.startsWith("image") || contentType.equals("application/pdf")
				|| contentType.equals("application/document")
				|| contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
				|| contentType.equals("application/msword") || contentType.equals("text/html");
	}

	@Override
	public Object getMyLocalData() {
		return crawlData;
	}
}
