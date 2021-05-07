package retriver;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.*;

import main.Config;

public class ResultRetriverPool {

	private ConcurrentHashMap<String, Future<Map<String, Integer>>> fileResults;
	private ConcurrentHashMap<String, Future<Map<String, Integer>>> fileResultsSummary;
	private ConcurrentHashMap<String, BlockingQueue<Future<Map<String, Integer>>>> webResults;
	private ConcurrentHashMap<String, Future<Map<String, Integer>>> webResultsSummary;

	private ExecutorService service;
	private ScheduledExecutorService schedule;

	private ConcurrentHashMap<String, Future<Map<String, Integer>>> readValue;

	public ResultRetriverPool() {
		service = Executors.newCachedThreadPool();
		schedule = Executors.newScheduledThreadPool(1);
		fileResults = new ConcurrentHashMap<>();
		webResults = new ConcurrentHashMap<>();
		webResultsSummary = new ConcurrentHashMap<>();

		schedule.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				webResults.clear();
				webResultsSummary.clear();
				System.out.println("Obrisani url-ovi");
			}
		}, Config.getInstance().url_refresh_time, Config.getInstance().url_refresh_time, TimeUnit.MILLISECONDS);

	}

	public void addWebResult(String domen, Future<Map<String, Integer>> result) {

		webResults.computeIfAbsent(domen, (key) -> {

			BlockingQueue<Future<Map<String, Integer>>> queue = new LinkedBlockingQueue();
			try {
				queue.put(result);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return queue;
		});
	}

	public String getWebResults(String domen) {
		try {
			Future<Map<String, Integer>> result = service.submit(new ResultRetriverJob(domen, webResults));
			readValue.put(domen, result);
			return result.get().toString();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String queryWebResults(String domen) {
		if (readValue.containsKey(domen)) {
			Future<Map<String, Integer>> result = readValue.get(domen);
			if (result.isDone()) {
				try {
					return result.get().toString();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			} else {
				return "Posao nije gotov.";
			}
		}
		return "Ovaj domen ne postoji";
	}
	// aw https://www.grammarly.com/blog/articles/
	// get web|www.grammarly.com

	public void sumAllWebResults() {

		if (this.webResults.isEmpty()) {
			System.out.println("Nije jos odradjen ni jedan domen");
			return;
		}
		for (Map.Entry<String, BlockingQueue<Future<Map<String, Integer>>>> entry : webResults.entrySet()) {
			String key = entry.getKey();
			URI uri = null;
			try {
				uri = new URI(key);
			} catch (URISyntaxException e2) {
				e2.printStackTrace();
			}
			String domen = uri.getHost();
			if (domen == null) {
				continue;
			}
			webResultsSummary.computeIfAbsent(domen, (key_val) -> {
				Future<Map<String, Integer>> map = entry.getValue().peek();
				return map;

			});

			webResultsSummary.computeIfPresent(domen, (key_val, value) -> {
				try {
					entry.getValue().peek().get().forEach((k, v) -> {
						try {
							value.get().merge(k, v, (v1, v2) -> v1 + v2);
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						}
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				return value;

			});
		}
		printSumValue(webResultsSummary);

	}

	public String deleteWebResults() {
		try {
			if (this.webResultsSummary.isEmpty()) {
				return "Nema sta da se obrise.";
			}

			webResultsSummary.clear();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "Obrisani su svi rezultati!";
	}

	public void addFileResult(String corpus, Future<Map<String, Integer>> result) {
		// Mozda ne mora synchronized jer radi sa ConcurentHashMap-om
		synchronized (fileResults) {
			// Smesta vrednosti hashMape koja je dobijena u FileScannerJob-u key(corpus)
			// value(HashMap(keyword,brojPojavljivanja))
			fileResults.put(corpus, result);
		}

	}

	public String getFileResults(String file) {
		try {
			if (!this.fileResults.containsKey(file)) {
				return "Ne postoji posao za korpus";
			}
			return this.fileResults.get(file).get().toString();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String queryFileResults(String file) {
		if (this.fileResults.containsKey(file)) {
			Future<Map<String, Integer>> result = this.fileResults.get(file);
			if (this.fileResults.get(file).isDone()) {
				try {
					return result.get().toString();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				return "Posao nije gotov";
			}
		}
		return "Ovaj fajl ne postoji.";
	}

	public void sumAllFileResults() {

		if (this.fileResults.isEmpty()) {
			System.out.println("Nije jos odradjen ni jedan korpus");
		}

		fileResultsSummary = fileResults;

		printSumValue(fileResultsSummary);
	}

	private void printSumValue(ConcurrentHashMap<String, Future<Map<String, Integer>>> resultsSummary) {
		for (Map.Entry<String, Future<Map<String, Integer>>> entry : resultsSummary.entrySet()) {
			try {
				String key = entry.getKey();
				Map<String, Integer> value = entry.getValue().get();
				System.out.println(key + " : " + value.toString());
			} catch (InterruptedException | ExecutionException ignored) {
			}
		}
	}

	public String deleteFileResults() {
		try {
			if (this.fileResultsSummary.isEmpty()) {
				return "Nema sta da se obrise.";
			}

			fileResultsSummary.clear();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "Obrisani su svi rezultati!";
	}

	public void shutdown() {
		this.service.shutdown();
	}

}