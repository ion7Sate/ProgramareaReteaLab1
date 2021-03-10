import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;

public class ImageDownloader extends  Thread{
    private String urlString;

    public ImageDownloader(String urlString, File path) throws IOException, URISyntaxException, MalformedURLException, FileNotFoundException {

        this.urlString = urlString;

        // Generate a pathname to write the image to.
        String toWriteTo = path.toPath().toString() + System.getProperty("file.separator");

        // Convert String to URL.
        URL url = new URL(urlString);

        // Connect to the HTTP host and send the GET request for the image.
        Socket socket = new Socket(url.getHost(), 80);
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.println("GET " + url.getPath() + " HTTP/1.1");
        pw.println("Host: " + url.getHost());
        pw.println();
        pw.flush();

        // Initialize the streams.
        final FileOutputStream fileOutputStream = new FileOutputStream(toWriteTo + url.getPath().replaceAll(".*/", ""));
        final InputStream inputStream = socket.getInputStream();


        boolean headerEnded = false;

        byte[] bytes = new byte[2048];
        int length;
        while ((length = inputStream.read(bytes)) != -1) {
            if (headerEnded)
                fileOutputStream.write(bytes, 0, length);
            else {
                for (int i = 0; i < 2048; i++) {
                    if (bytes[i] == 13 && bytes[i + 1] == 10 && bytes[i + 2] == 13 && bytes[i + 3] == 10) {
                        headerEnded = true;
                        fileOutputStream.write(bytes, i+4, 2048-i-4);
                        break;
                    }
                }
            }
        }
        inputStream.close();
        fileOutputStream.close();
    }
}
