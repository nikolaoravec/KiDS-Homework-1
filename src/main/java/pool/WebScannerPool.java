package pool;

import job.ScanningJob;
import job.WebScannerJob;
import main.Config;
import retriver.ResultRetriverPool;

import java.util.Map;
import java.util.concurrent.*;

public class WebScannerPool {

	private ExecutorService pool;
	private ResultRetriverPool result;
	private ConcurrentHashMap<String, Boolean> jobState;
	private ScheduledExecutorService schedule;

	public WebScannerPool(ResultRetriverPool result) {
		this.jobState = new ConcurrentHashMap<>();
		this.pool = Executors.newCachedThreadPool();
		this.result = result;
		this.schedule = Executors.newScheduledThreadPool(1);

		this.schedule.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				jobState.clear();
				System.out.println("Obrisani url-ovi");
			}
		}, Config.getInstance().url_refresh_time, Config.getInstance().url_refresh_time, TimeUnit.MILLISECONDS);
	}

	public void submitScannJob(ScanningJob job) {
		WebScannerJob task = (WebScannerJob) job;
		task.setJobState(this.jobState);
		Future<Map<String, Integer>> fut = pool.submit(task);
		result.addWebResult(job.getQuery(), fut);
	}

	// aw https://www.grammarly.com/blog/articles/
	// get web|www.grammarly.com

	public void shutdown() {
		this.pool.shutdown();
		this.schedule.shutdown();
	}

}
