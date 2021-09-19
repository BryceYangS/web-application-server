package webserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpMethod;

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

			HttpRequest request = new HttpRequest(in);
			HttpResponse response = new HttpResponse(out);

			// POST /user/create
			if (HttpMethod.POST.equals(request.getMethod()) && "/user/create".equals(request.getPath())) {
				// User DB 저장
				DataBase.addUser(new User(request.getParameter("userId"), request.getParameter("password"),
					request.getParameter("name"), request.getParameter("email")));

				// 302 response (location : index.html)
				response.sendRedirect("/index.html");
				return;
			}

			// GET /user/create
			if (HttpMethod.GET.equals(request.getMethod()) && "/user/create".equals(request.getPath())) {
				// User DB 저장
				DataBase.addUser(
					new User(request.getParameter("userId"), request.getParameter("password"),
						request.getParameter("name"),
						request.getParameter("email")));

				// 302 response (location : index.html)
				response.sendRedirect("/index.html");
				return;
			}

			// POST /user/login
			if (HttpMethod.POST.equals(request.getMethod()) && "/user/login".equals(request.getPath())) {

				User user = DataBase.findUserById(request.getParameter("userId"));
				if (user != null && user.getPassword().equals(request.getParameter("password"))) {
					response.addHeader("Set-Cookie", "logined=true");
					response.sendRedirect("/index.html");
					return;
				}

				response.sendRedirect("/user/login_failed.html");
				return;
			}

			if ("/user/list".equals(request.getPath())) {

				if (!request.isLogin()) {
					response.sendRedirect("/user/login_failed.html");
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

				response.response200Header(builder.length(), "text/html");
				response.responseBody(builder.toString().getBytes());
				return;

			}

			byte[] body = "Hello World".getBytes();
			if (request.isStaticFileRequest()) {
				body = Files.readAllBytes(new File(WEB_APP_PATH + request.getPath()).toPath());
			}

			response.response200Header(body.length, response.makeContentType(request));
			response.responseBody(body);

		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

}
