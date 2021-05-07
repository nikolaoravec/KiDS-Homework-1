package pool;

import job.FileScannerJob;
import job.ScanningJob;
import retriver.ResultRetriverPool;

import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class FileScannerPool {

	private ForkJoinPool pool;
	private ResultRetriverPool result;

	public FileScannerPool(ResultRetriverPool result) {
		this.pool = ForkJoinPool.commonPool();
		this.result = result;
	}

	public void submitScannJob(ScanningJob job) {
		RecursiveTask<Map<String, Integer>> jobRunnable = (FileScannerJob) job;
		// fut - Result is HashMap iz FileScannerJob-a
		Future<Map<String, Integer>> fut = pool.submit(jobRunnable);
		// job.getQuery() - ime corpus-a | fut
		result.addFileResult(job.getQuery(), fut);
	}

	public void shutdown() {
		pool.shutdown();
	}

}
