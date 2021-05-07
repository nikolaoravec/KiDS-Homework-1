package job;

import main.Config;
import main.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class WebScannerJob implements ScanningJob, Callable<Map<String, Integer>> {

	private int hopovi;
	private String url;
	private BlockingQueue<ScanningJob> jobQueue;
	private String query;
	private ConcurrentHashMap<String, Boolean> jobState;
	private String keywords;

	public WebScannerJob(int hopovi, String url, BlockingQueue<ScanningJob> jobQueue) throws URISyntaxException {
		this.hopovi = hopovi;
		this.url = url;
		this.jobQueue = jobQueue;
		this.query = url;
		this.jobState = new ConcurrentHashMap<>();
		this.keywords = Config.getInstance().keywords;
	}

	private WebScannerJob(int hopovi, String url, BlockingQueue<ScanningJob> jobQueue, String query,
			ConcurrentHashMap<String, Boolean> jobState) {
		this.hopovi = hopovi;
		this.url = url;
		this.jobQueue = jobQueue;
		this.query = query;
		this.jobState = jobState;
		this.keywords = Config.getInstance().keywords;
	}

	@Override
	public Map<String, Integer> call() {

		HashMap<String, Integer> results = new HashMap<>();
		// System.out.println("Start " + this.url);
		Document html;
		try {
			html = Jsoup.connect(url).timeout(20000).get();
			Elements links = html.select("a[href]");

			if (this.hopovi != 0) {
				links.forEach(link -> jobState.computeIfAbsent(link.attr("abs:href"), key -> {
					try {
						jobQueue.put(new WebScannerJob(this.hopovi - 1, link.attr("abs:href"), this.jobQueue,
								link.attr("abs:href"), this.jobState));
					} catch (InterruptedException e) {
						e.printStackTrace();
						return false;
					}
					return true;
				}));
			}

			Element body = html.body();
			String[] keywods = this.keywords.split(",");

			Arrays.asList(body.text().split(" ")).forEach((word) -> Utils.expandResult(results, word, keywods));

			System.out.println("Starting web scan for web|" + url);

			// System.out.println("Finished " + this.url);
		} catch (IOException e) {
			// e.printStackTrace();
			System.err.println("Greska pri dohvatanju resursa");
		}

		// System.out.println("results" + results);

		return results;
	}

	public void setJobState(ConcurrentHashMap<String, Boolean> jobState) {
		this.jobState = jobState;
	}

	public ScanType getType() {
		return ScanType.WEB;
	}

	public String getQuery() {
		return this.query;
	}
}
