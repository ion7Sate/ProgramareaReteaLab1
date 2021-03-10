package UTM;
import javax.net.ssl.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

    public class GetRequestToUtm {

        public static void main(String[] args) throws Exception {

           requestToUtmMD();

        }


        public static void requestToUtmMD() throws InterruptedException {
            String serverResponseSecurised = getResponse("utm.md", 443, "/");
            List<String> listOfImgSecurised = getPics(serverResponseSecurised);
            listOfImgSecurised.remove(0);
            listOfImgSecurised.remove(0);
            listOfImgSecurised.remove(0);
            System.out.println("List of images from site [utm.md] :" + listOfImgSecurised);

            Semaphore semaphore = new Semaphore(2);
            ExecutorService exec = Executors.newFixedThreadPool(4);
            boolean status = true;
            while (status) {
                for (String element : listOfImgSecurised) {
                    semaphore.acquire();
                    exec.execute(() -> {
                        try {
                            getImages(getRealNameOfPicture(element, "https://utm.md"));
                            semaphore.release();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        System.out.println(Thread.currentThread().getName());
                    });
                    if (element.equals(listOfImgSecurised.get(listOfImgSecurised.size() - 1))) {
                        status = false;
                        break;
                    }
                }
            }
            exec.shutdown();
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        }

        public static List<String> getPics(String text) {
            String img;
            String regex = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
            List<String> pics = new ArrayList<>();

            Pattern pImage = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher mImage = pImage.matcher(text);

            while (mImage.find()) {
                img = mImage.group();
                Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);
                while (m.find()) {
                    pics.add(m.group(1));
                }
            }
            return pics;
        }


        public static String getResponse(String hostName, int port, String getArgument) {
            String serverResponse = "";
            try {
                SSLSocketFactory factory =
                        (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket socket =
                        (SSLSocket) factory.createSocket(hostName, port);
                socket.startHandshake();

                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        socket.getOutputStream())));

                StringBuilder dataRequest = new StringBuilder();
                dataRequest
                        .append("GET " + getArgument + " HTTP/1.1\r\n")
                        .append("Host: " + hostName + "\r\n")
                        .append("Content-Type: text/html;charset=utf-8 \r\n")
                        .append("Accept-Language: ro \r\n")
                        .append("Content-Language: en, ase, ru \r\n")
                        .append("User-Agent: Mozilla/5.0 (X11; Linux i686; rv:2.0.1) Gecko/20100101 Firefox/4.0.1 \r\n")
                        .append("Vary: Accept-Encoding \r\n")
                        .append("\r\n");

                out.println(dataRequest);
                out.flush();

                if (out.checkError())
                    System.out.println(
                            "SSLSocketClient:  java.io.PrintWriter error");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    serverResponse += inputLine + "\n";

                in.close();
                out.close();
                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Secured connection performed successfully");
            return serverResponse;
        }


        public static String getRealNameOfPicture(String text, String hostName) {
            String result = null;

            if (text.contains(hostName)) {
                result = text.replace(hostName, "");
                result = result.replace("'", "");
            } else result = text;
            return result;
        }



        private static void getImages(String imgName) {
            try {
                SSLSocketFactory factory =
                        (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket socket =
                        (SSLSocket) factory.createSocket("utm.md", 443);
                socket.startHandshake();

                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        socket.getOutputStream())));

                out.println("GET " + imgName + " HTTP/1.1\r\nHost: " + "utm.md" + " \r\n\r\n");
                out.flush();

                if (out.checkError())
                    System.out.println(
                            "SSLSocketClient:  java.io.PrintWriter error");
                String[] tokens = imgName.split("/");
                DataInputStream in = new DataInputStream(socket.getInputStream());
                OutputStream dos = new FileOutputStream("C:\\Users\\sapte\\OneDrive - Technical University of Moldova\\Рабочий стол\\Programarea in retea\\SocketHTTP\\imgutm" + tokens[tokens.length - 1]);

                int count, offset;
                byte[] buffer = new byte[2048];
                boolean eohFound = false;
                while ((count = in.read(buffer)) != -1) {
                    offset = 0;
                    if (!eohFound) {
                        String string = new String(buffer, 0, count);
                        int indexOfEOH = string.indexOf("\r\n\r\n");
                        if (indexOfEOH != -1) {
                            count = count - indexOfEOH - 4;
                            offset = indexOfEOH + 4;
                            eohFound = true;
                        } else {
                            count = 0;
                        }
                    }
                    dos.write(buffer, offset, count);
                    dos.flush();
                }
                in.close();
                dos.close();
                System.out.println("Images is transfered");

                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

