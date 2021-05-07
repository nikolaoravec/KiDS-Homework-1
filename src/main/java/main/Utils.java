package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;



public class Utils {

	public static boolean isKorpus(String path, String prefix) {
		final String[] split = path.split(Pattern.quote(File.separator));
		String lastDir = split[split.length - 1];
		return lastDir.startsWith(prefix);
	}

	public static List<File> getFilesFromDir(File directory) throws Exception {

		if (!directory.isDirectory()) {
			throw new Exception("Nije direktorijum");
		}
		List<File> files = new ArrayList<File>();
		for (File file : directory.listFiles()) {
			if (!file.isDirectory()) {
					files.add(file);
			}
		}
		return files;
	}

	public static List<File> getDirsfromDir(File directory) throws Exception {
		if (!directory.isDirectory()) {
			throw new Exception("Nije direktorijum");
		}

		List<File> dirs = new ArrayList<File>();
		for (File dir : directory.listFiles()) {
			if (dir.isDirectory()) {
				dirs.add(dir);
			}
		}

		return dirs;
	}

	public static void expandResult(HashMap<String, Integer> result, String word, String[] keys) {
		boolean contained = false;

		if (word.endsWith(".") || word.endsWith(",") || word.endsWith("!") || word.endsWith("?")) {
			word = word.substring(0, word.length() - 1);
		}
		if (word.startsWith(".") || word.startsWith(",") || word.startsWith("!") || word.startsWith("?")) {
			word = word.substring(1, word.length());
		}

		for (String key : keys) {
			contained = word.equals(key);
			if (contained) {
				break;
			}
		}
		if (contained) {
			// Ako je ova rec ucitana kao key samo se broj povecava za 1
			if (result.containsKey(word)) {
				result.put(word, result.get(word) + 1);
				// System.out.println(result);
				// Ako ova rec jos uvek nije ucitana, upisuje se u hashMap-u i namesta se na
				// broj 1
			} else {
				// System.out.println("Novi kljuc: " + word);
				result.put(word, 1);
			}
		}
	}

}
