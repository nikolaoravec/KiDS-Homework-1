package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private static Config instance = null;

    public String file_corpus_prefix;
    public long dir_crawler_sleep_time;
    public String keywords;
    public long file_scanning_size_limit;
    public int hop_count;
    public long url_refresh_time;

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private Config() {
        Properties p = new Properties();
        try {
            File f = new File("app.properties");
            FileInputStream fin = new FileInputStream(f);
            p.load(fin);

            file_corpus_prefix = p.getProperty("file_corpus_prefix");
            dir_crawler_sleep_time = Long.parseLong(p.getProperty("dir_crawler_sleep_time"));
            keywords = p.getProperty("keywords");
            file_scanning_size_limit = Long.parseLong(p.getProperty("file_scanning_size_limit"));
            hop_count = Integer.parseInt(p.getProperty("hop_count"));
            url_refresh_time = Long.parseLong(p.getProperty("url_refresh_time"));
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
