import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: don
 * Date: 9/28/12
 * Time: 4:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class Search {

    static Set<String> output = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    static void githubSearch(String keyword) throws IOException, InterruptedException {
        /*
        First page when searching using github advance search
         */
        String query = String.format("https://github.com/search?langOverride=&language=&q=%s&repo=&start_value=1&type=Code", keyword);
        Document doc = Jsoup.connect(query).get();

        /*
        Last page number
         */
        int lastPage = Integer.parseInt(doc.select(".pager_link").last().text());

        for(int i=2; i <= lastPage; i++ ){
            executor.submit(new GithubSearch(String.format("https://github.com/search?langOverride=&language=&q=%s&repo=&start_value=%d&type=Code", keyword, i)));
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        System.out.println(output);
    }

    static class GithubSearch implements Runnable{

        String url;

        public GithubSearch(String url){
            this.url = url;
        }

        @Override
        public void run() {
            try {
                Document doc = Jsoup.connect(url).get();
                for(Element result : doc.select(".result")){
                    output.add(result.getElementsByAttribute("href").last().text().split(" ")[0]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            githubSearch("tbb");
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
