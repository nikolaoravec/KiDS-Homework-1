package crawler;

import job.FileScannerJob;
import job.ScanningJob;
import main.Config;
import main.Utils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class CrawlerTask implements Runnable {

	public static boolean running = true;
	private String direktorijum;
	private ConcurrentHashMap<String, Long> status;
	private String prefix;
	private long timeout;
	private BlockingQueue<ScanningJob> jobQueue;

	public CrawlerTask(String direktorijum, String prefix, ConcurrentHashMap<String, Long> map, long timeout,
			BlockingQueue<ScanningJob> jobQueue) {
		this.direktorijum = direktorijum;
		status = map;
		this.prefix = prefix;
		this.timeout = timeout;
		this.jobQueue = jobQueue;
	}

	@Override
	public void run() {
		File rootDirectory = new File(direktorijum);
		if (!rootDirectory.isDirectory()) {
			System.err.println("Nije direktorijum: " + direktorijum);
			return;
		}
		System.out.println("Adding dir " + rootDirectory.getAbsolutePath());

		Queue<File> toSearch = new LinkedList<>();

		while (running) {
			toSearch.add(rootDirectory);
			while (!toSearch.isEmpty()) {
				File current = toSearch.poll();
				if (current.isDirectory()) {
					if (Utils.isKorpus(current.getAbsolutePath(), prefix)) {
						try {
							List<File> files = Utils.getFilesFromDir(current);
							files.forEach((file) -> {
								syncFile(file, this.status, false);
							});
							syncFile(current, this.status, true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					try {
						toSearch.addAll(Utils.getDirsfromDir(current));
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}

			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	private void syncFile(File file, ConcurrentHashMap<String, Long> status, boolean createJob) {

		//Ako postoji value vezan za dati key (Ako je uctiano vec)
		status.computeIfPresent(file.getAbsolutePath(), (key, value) -> {

//			System.out.println(key + " + " + value);

			if (value != file.lastModified()) {

				key = key.replace("\\", "/");
				String[] arr = key.split("/");

				StringBuilder dir = new StringBuilder();

				for (int i = 0; i < arr.length - 1; i++) {
					dir.append(arr[i]).append(File.separator);
				}

				dir = new StringBuilder(dir.substring(0, dir.length() - 1));
				createJob(new File(dir.toString()));

				return file.lastModified();
			}
			return value;
		});

		status.computeIfAbsent(file.getAbsolutePath(), (key) -> {
			if (createJob) {
				createJob(file);
			}
			return file.lastModified();
		});
	}

	private String corpus(String path) {
		int index = path.indexOf(Config.getInstance().file_corpus_prefix);
		return path.substring(index, path.length());
	}

	private void createJob(File file) {
		try {
			jobQueue.add(new FileScannerJob(file.getAbsolutePath(), Config.getInstance().file_scanning_size_limit,
					Config.getInstance().keywords, corpus(file.getAbsolutePath())));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
