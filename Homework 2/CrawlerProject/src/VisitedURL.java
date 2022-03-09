
public class VisitedURL {
	String url;
	int size;
	int numOutlinks;
	String contentType;

	public VisitedURL(String url, int size, int numOutlinks, String contentType) {
		this.url = url;
		this.size = size;
		this.numOutlinks = numOutlinks;
		this.contentType = contentType;
	}

}
