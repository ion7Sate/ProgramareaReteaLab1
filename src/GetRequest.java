import java.io.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GetRequest {
    public static void main(String[] args) throws IOException {
        InetAddress addr = InetAddress.getByName("me.utm.md");
        Socket socket = new Socket(addr, 80);
        boolean autoflush = true;
        PrintWriter out = new PrintWriter(socket.getOutputStream(), autoflush);
        BufferedReader in = new BufferedReader(

                new InputStreamReader(socket.getInputStream()));
        // send an HTTP request to the web server
        out.println("GET / HTTP/1.1");
        out.println("Host: me.utm.md:80");
        out.println("Connection: Close");
        out.println();

        // read the response
        boolean loop = true;
        StringBuilder sb = new StringBuilder(8096);
        while (loop) {
            if (in.ready()) {
                int i = 0;
                while (i != -1) {
                    i = in.read();
                    sb.append((char) i);
                }
                loop = false;
            }
        }

        System.out.println(sb.toString());
        socket.close();

        Pattern pattern = Pattern.compile("[^\"']*\\.(?:png|jpg|gif)");
        List<String> allPhotos = new ArrayList<>();

        Matcher m = pattern.matcher(sb.toString());
        while (m.find()) {
            allPhotos.add(m.group());
        }

        List<String> allPhotosLinks = new ArrayList<>();

        allPhotos.forEach((photo) -> {
            if (photo.startsWith("http://")) {
                allPhotosLinks.add(photo);
            } else {
                allPhotosLinks.add("http://me.utm.md/" + photo);
            }
        });

        List<String> allPaths = new ArrayList<>();
        Pattern pattern2 = Pattern.compile("([^\\/]+$)");
        File downloads = new File("C:\\Users\\sapte\\OneDrive - Technical University of Moldova\\Рабочий стол\\Programarea in retea\\SocketHTTP\\img");

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        final Semaphore semaphore = new Semaphore(2);

        for (String link : allPhotosLinks) {

            ImageDownloader imageDownloader = null;

            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                imageDownloader = new ImageDownloader(link, downloads);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            executor.execute(imageDownloader);
            semaphore.release();
        }
        System.out.println("Maximum threads inside pool " + executor.getMaximumPoolSize());
        executor.shutdown();
    }
    }

