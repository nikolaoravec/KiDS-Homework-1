package main;

import crawler.CrawlerTask;
import crawler.DirectoryCrawler;
import job.JobDispatcherTask;
import job.ScanningJob;
import job.WebScannerJob;
import pool.FileScannerPool;
import pool.WebScannerPool;
import retriver.ResultRetriverPool;

import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) {
        Scanner cliIn = new Scanner(System.in);

        ResultRetriverPool results = new ResultRetriverPool();

        BlockingQueue<ScanningJob> jobs = new LinkedBlockingQueue<>();
        ExecutorService mainThreadPool = Executors.newFixedThreadPool(1);

        //Smesta job-ove u queue
        DirectoryCrawler dicr = new DirectoryCrawler(jobs);

        WebScannerPool webScannerPool = new WebScannerPool(results);
        FileScannerPool fileScannerPool = new FileScannerPool(results);

        // Rasporedjuje taskove u svojoj run metodi
        JobDispatcherTask tr = new JobDispatcherTask(jobs, webScannerPool, fileScannerPool);
        mainThreadPool.submit(tr);


        String in = cliIn.nextLine();
        while (!in.equals("stop")) {

            if (in.startsWith("ad")) {
                String[] params = in.split(" ");
                if (params.length != 2) {
					System.err.println("Pogresni parametri, pobaj ad _putanja_");
					in = cliIn.nextLine();
					continue;
				}
                dicr.submitJob(params[1]);
            } else if (in.startsWith("aw")) {
                String[] params = in.split(" ");
                if (params.length != 2) {
					System.err.println("Pogresni parametri, pobaj aw _URL_");
					in = cliIn.nextLine();
					continue;
				}
                try {
                    jobs.put(new WebScannerJob(Config.getInstance().hop_count, params[1], jobs));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    System.out.println("Url nije validan");
                }
            } else if (in.startsWith("get file|")) {
                String[] params = in.split("\\|");
                if (params.length != 2) {
					System.err.println("Pogresni parametri, pobaj get file|naziv_fajla");
					in = cliIn.nextLine();
					continue;
				}
                System.out.println(results.getFileResults(params[1]));
            } else if (in.startsWith("query file|")) {
                String[] params = in.split("\\|");
                if (params.length != 2) {
					System.err.println("Pogresni parametri, pobaj get file|naziv_fajla");
					in = cliIn.nextLine();
					continue;
				}
                System.out.println(results.queryFileResults(params[1]));
            } else if (in.startsWith("file|summary")) {
                results.sumAllFileResults();
            } else if (in.startsWith("web|summary")) {
                results.sumAllWebResults();
            } else if (in.startsWith("cfs")) {
                System.out.println(results.deleteFileResults());
            } else if (in.startsWith("cws")) {
                System.out.println(results.deleteWebResults());
            } else if (in.startsWith("get web|")) {
                String[] params = in.split("\\|");
                if (params.length != 2) {
					System.err.println("Pogresni parametri, pobaj get web|naziv_domen-a");
					in = cliIn.nextLine();
					continue;
				}
                System.out.println(results.getWebResults(params[1]));
            } else if (in.startsWith("query web|")) {
                String[] params = in.split("\\|");
                if (params.length != 2) {
					System.err.println("Pogresni parametri, pobaj get web|naziv_domen-a");
					in = cliIn.nextLine();
					continue;
				}
                System.out.println(results.queryWebResults(params[1]));
            }else {
				System.err.println("Nepostojeca komanda (Probaj aw, ad, get itd...)");
			}
            in = cliIn.nextLine();
        }

        System.out.println("Stopping...");
        results.shutdown();
        webScannerPool.shutdown();
        fileScannerPool.shutdown();
        dicr.shutdown();
        mainThreadPool.shutdown();
        tr.running = false;
        CrawlerTask.running = false;
        cliIn.close();
    }


}
