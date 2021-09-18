package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.print.attribute.standard.PDLOverrideSupported;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpMethod;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private static final String WEB_APP_PATH = "./webapp";

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            DataOutputStream dos = new DataOutputStream(out);

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

            String[] tokens = startLine.split(" ");
            String httpMethod = tokens[0];
            String uri = tokens[1];

            Map<String, String> queryStringMap = new HashMap<>();
            if (isContainsQueryStrings(uri)) {
                int queryStringIndex = uri.indexOf("?");
                String queryString = uri.substring(queryStringIndex + 1);
                queryStringMap = HttpRequestUtils.parseQueryString(queryString);

                uri = uri.substring(0, queryStringIndex);
            }

            // POST /user/create
            if (HttpMethod.POST.name().equalsIgnoreCase(httpMethod) && "/user/create".equals(uri)) {
                String body = IOUtils.readData(br, Integer.parseInt(pairMap.getOrDefault("Content-Length", "0")));
                Map<String, String> httpBody = HttpRequestUtils.parseQueryString(body);

                // User DB 저장
                DataBase.addUser(new User(httpBody.get("userId"), httpBody.get("password"), httpBody.get("name"), httpBody.get("email")));

                // 302 response (location : index.html)
                response302Header(dos, "/index.html");
                return;
            }

            // GET /user/create
            if (HttpMethod.isGet(httpMethod) && "/user/create".equals(uri)) {
                // User DB 저장
                DataBase.addUser(new User(queryStringMap.get("userId"), queryStringMap.get("password"), queryStringMap.get("name"), queryStringMap.get("email")));

                // 302 response (location : index.html)
                response302Header(dos, "/index.html");
                return;
            }

            // POST /user/login
            if (HttpMethod.POST.name().equalsIgnoreCase(httpMethod) && "/user/login".equals(uri)) {

                String s = IOUtils.readData(br, Integer.parseInt(pairMap.getOrDefault("Content-Length", "0")));
                Map<String, String> httpBody = HttpRequestUtils.parseQueryString(s);
                log.debug(httpBody.toString());

                User user = DataBase.findUserById(httpBody.get("userId"));
                if (user != null && user.getPassword().equals(httpBody.get("password"))) {
                    responseLoginSuccessHeader(dos);
                    return;
                }

                responseLoginFailHeader(dos);
                return;
            }

            if ("/user/list".equals(uri)) {

                if (!isLogin(pairMap)) {
                    responseLoginFailHeader(dos);
                    return;
                }

                StringBuilder builder = new StringBuilder("<html>");
                builder.append("<body>");
                builder.append("<table border='1'>");
                Collection<User> all = DataBase.findAll();
                for (User user : all) {
                    builder.append("<tr>");
                    builder.append("<td>" + user.getUserId() + "</td>");
                    builder.append("<td>" + user.getName() + "</td>");
                    builder.append("<td>" + user.getEmail() + "</td>");
                    builder.append("</tr>");
                }
                builder.append("</table>");
                builder.append("</body>");
                builder.append("</html>");

                response200Header(dos, builder.length(), "text/html");

                dos.writeBytes(builder.toString());
                return;

            }

            byte[] body = "Hello World".getBytes();
            if (isStaticFile(uri)) {
                body = Files.readAllBytes(new File( WEB_APP_PATH + uri).toPath());
            }

            String contentType = makeContentType(pairMap, uri.substring(uri.lastIndexOf(".") + 1));
            response200Header(dos, body.length, contentType);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isLogin(Map<String, String> pairMap) {
        Map<String, String> cookie = HttpRequestUtils.parseCookies(pairMap.get("Cookie"));
        return Boolean.parseBoolean(cookie.getOrDefault("logined", "false"));
    }

    private void responseLoginFailHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: /user/login_failed.html\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    private void responseLoginSuccessHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: /index.html\r\n");
            dos.writeBytes("Set-Cookie: logined=true\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isContainsQueryStrings(String uri) {
        return uri.contains("?");
    }

    private boolean isStaticFile(String uri) {
        return uri.contains(".");
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
