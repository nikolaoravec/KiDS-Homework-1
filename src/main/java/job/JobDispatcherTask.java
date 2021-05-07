package job;

import pool.FileScannerPool;
import pool.WebScannerPool;

import java.util.concurrent.BlockingQueue;

public class JobDispatcherTask implements Runnable {

	private BlockingQueue<ScanningJob> jobovi;
	private WebScannerPool webScannerPool;
	private FileScannerPool fileScannerPool;
	public boolean running;

	public JobDispatcherTask(BlockingQueue<ScanningJob> jobovi, WebScannerPool webScannerPool,
			FileScannerPool fileScannerPool) {
		this.jobovi = jobovi;
		this.webScannerPool = webScannerPool;
		this.fileScannerPool = fileScannerPool;
		this.running = true;
	}

	@Override
	public void run() {
		while (running) {
			ScanningJob job;
			job = jobovi.poll();
			if (job == null) {
				continue;
			}
			switch (job.getType()) {
			case FILE:
				fileScannerPool.submitScannJob(job);
				break;
			case WEB:
				webScannerPool.submitScannJob(job);
				break;
			default:
				break;
			}

		}
	}

	public void shutDown() {
		this.running = false;
	}
}
