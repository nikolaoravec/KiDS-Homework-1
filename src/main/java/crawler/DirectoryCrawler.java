package crawler;

import job.ScanningJob;
import main.Config;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DirectoryCrawler {

	private ExecutorService pool;
	private String prefix;
	private ConcurrentHashMap<String, Long> sharedState;
	private long timeout;

	private BlockingQueue<ScanningJob> jobovi;

	public DirectoryCrawler(BlockingQueue<ScanningJob> jobovi) {
		this.jobovi = jobovi;
		pool = Executors.newCachedThreadPool();
		prefix = Config.getInstance().file_corpus_prefix;
		sharedState = new ConcurrentHashMap<>();
		timeout = Config.getInstance().dir_crawler_sleep_time;
	}

	public void submitJob(String direktorijum) {
		CrawlerTask job = new CrawlerTask(direktorijum, prefix, sharedState, timeout, jobovi);
		pool.submit(job);
	}

	public void shutdown() {
		this.pool.shutdown();
	}

}
