package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

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
			DataOutputStream dos = new DataOutputStream(out);
			HttpRequest request = new HttpRequest(in);

			// POST /user/create
			if (HttpMethod.POST.equals(request.getMethod()) && "/user/create".equals(request.getPath())) {
				// User DB 저장
				DataBase.addUser(new User(request.getParameter("userId"), request.getParameter("password"),
					request.getParameter("name"), request.getParameter("email")));

				// 302 response (location : index.html)
				response302Header(dos, "/index.html");
				return;
			}

			// GET /user/create
			if (HttpMethod.GET.equals(request.getMethod()) && "/user/create".equals(request.getPath())) {
				// User DB 저장
				DataBase.addUser(
					new User(request.getParameter("userId"), request.getParameter("password"), request.getParameter("name"),
						request.getParameter("email")));

				// 302 response (location : index.html)
				response302Header(dos, "/index.html");
				return;
			}

			// POST /user/login
			if (HttpMethod.POST.equals(request.getMethod()) && "/user/login".equals(request.getPath())) {

				User user = DataBase.findUserById(request.getParameter("userId"));
				if (user != null && user.getPassword().equals(request.getParameter("password"))) {
					responseLoginSuccessHeader(dos);
					return;
				}

				responseLoginFailHeader(dos);
				return;
			}

			if ("/user/list".equals(request.getPath())) {

				if (!request.isLogin()) {
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
			if (request.isStaticFileRequest()) {
				body = Files.readAllBytes(new File(WEB_APP_PATH + request.getPath()).toPath());
			}

			String contentType = makeContentType(request.getHeader("Accept"), request.getPath().substring(request.getPath().lastIndexOf(".") + 1));
			response200Header(dos, body.length, contentType);
			responseBody(dos, body);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
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

	private String makeContentType(String acceptHeader, String staticFileExt) {
		String[] accepts = acceptHeader.split(",");
		String contentType = "*/*";
		for (String accept : accepts) {
			if (accept.contains(staticFileExt)) {
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
