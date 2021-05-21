package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StreamTokenizer;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private static final String WEB_APP_PATH = "./webapp";

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            InputStreamReader reader = new InputStreamReader(in, "UTF-8");
            BufferedReader br = new BufferedReader(reader);

            String startLine = br.readLine();
            log.debug("Start Line : {}", startLine);
            if (startLine == null){
                return;
            }

            Map<String, String> pairMap = new HashMap<>();
            while (true) {
                String s = br.readLine();
                if (s == null || "".equals(s)){
                    break;
                }

                HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(s);
                pairMap.put(pair.getKey(), pair.getValue());
                log.debug("header : {}", pair);
            }

            byte[] body = "Hello World".getBytes();
            String[] tokens = startLine.split(" ");
            String uri = tokens[1];
            String staticFileExt = uri.substring(uri.lastIndexOf(".") + 1);
            if (isStaticFile(staticFileExt)) {
                body = Files.readAllBytes(new File( WEB_APP_PATH + uri).toPath());
            }

            String contentType = makeContentType(pairMap, staticFileExt);
            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length, contentType);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isStaticFile(String staticFileExt) {
        return staticFileExt.equals("html") ||
            staticFileExt.equals("css") ||
            staticFileExt.equals("js") ||
            staticFileExt.equals("ico") ||
            staticFileExt.equals("png") ||
            staticFileExt.equals("eot") ||
            staticFileExt.equals("svg") ||
            staticFileExt.equals("ttf") ||
            staticFileExt.equals("woff") ||
            staticFileExt.equals("woff2");
    }

    private String makeContentType(Map<String, String> pairMap, String staticFileExt) {
        String[] accepts = pairMap.getOrDefault("Accept", "*/*").split(",");
        String contentType = "*/*";
        for (String accept : accepts) {
            if (accept.contains(staticFileExt)){
                contentType = accept;
                break;
            }
        }
        return contentType;
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + "\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
