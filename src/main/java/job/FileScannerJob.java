package job;

import main.Config;
import main.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

import org.apache.commons.io.FilenameUtils;

public class FileScannerJob extends RecursiveTask<Map<String, Integer>> implements ScanningJob {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2322492884625916260L;
	private long sizeLimit;
	private String keywords;
	private List<File> files;
	private String query;

	public FileScannerJob(String path, long sizeLimit, String keywords, String query) throws Exception {
		this.sizeLimit = sizeLimit;
		this.keywords = keywords;
		files = Utils.getFilesFromDir(new File(path));
		this.query = query;
	}

	public FileScannerJob(long sizeLimit, String keywords, List<File> files) {
		this.sizeLimit = sizeLimit;
		this.keywords = keywords;
		this.files = files;
	}

	@Override
	public ScanType getType() {
		return ScanType.FILE;
	}

	@Override
	public String getQuery() {
		return query;
	}

	@Override
	protected Map<String, Integer> compute() {

		HashMap<String, Integer> result = new HashMap<>();
		List<File> thisTaskList = toByteLimit(this.sizeLimit, this.files);
		if (thisTaskList.size() != 0) {
			FileScannerJob remaining = new FileScannerJob(this.sizeLimit, this.keywords, this.files);
			remaining.fork();
			for (File file : thisTaskList) {
				try {
					String ext = FilenameUtils.getExtension(file.getAbsolutePath());
					if (!ext.equals("txt")) {
						System.err.println("File " + file.getName() + " nije tipa .txt i ne moze se procitati");
						continue;
					}
					System.out.println("Starting file scan for file|" + file.getAbsolutePath());
					BufferedReader breader = new BufferedReader(new FileReader(file));

					String[] keys = Config.getInstance().keywords.split(",");
					while (breader.ready()) {
						String line = breader.readLine();
						String[] words = line.split(" ");
						for (String word : words) {
							Utils.expandResult(result, word, keys);
						}
					}

					breader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			HashMap<String, Integer> remainingResult = (HashMap<String, Integer>) remaining.join();
			return combine(result, remainingResult);
		}

		return result;
	}

	private List<File> toByteLimit(long limit, List<File> list) {
		int index = 0;
		long sum = 0;
		LinkedList<File> newList = new LinkedList<>();
		for (File f : list) {
			sum += f.length();
			index++;
			newList.add(f);
			if (sum > limit) {
				break;
			}
		}
		list.subList(0, index).clear();
		return newList;
	}

	private HashMap<String, Integer> combine(HashMap<String, Integer> first, HashMap<String, Integer> second) {
		HashMap<String, Integer> merged = new HashMap<String, Integer>();
		merged.putAll(first);
		merged.putAll(second);
		return merged;
	}

}