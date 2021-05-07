package retriver;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ResultRetriverJob implements Callable<Map<String, Integer>> {

	private String domen;
	private ConcurrentHashMap<String, BlockingQueue<Future<Map<String, Integer>>>> map;

	public ResultRetriverJob(String domen, ConcurrentHashMap<String, BlockingQueue<Future<Map<String, Integer>>>> map) {
		this.domen = domen;
		this.map = map;
	}

	public Map<String, Integer> getResult() {
		Map<String, Integer> selects = new HashMap<>();

		for (Map.Entry<String, BlockingQueue<Future<Map<String, Integer>>>> entry : this.map.entrySet()) {
			String key = entry.getKey();
//			System.out.println(key);
			if (key.startsWith("https://" + domen)) {
				try {
					entry.getValue().peek().get().forEach((k, v) -> {
						selects.merge(k, v, (v1, v2) -> v1 + v2);
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
		if (selects.isEmpty()) {
			return null;
		}
		return selects;
	}

	@Override
	public Map<String, Integer> call() throws Exception {
		return getResult();

	}
//	aw https://www.grammarly.com/blog/articles/
//	get web|www.grammarly.com
}
