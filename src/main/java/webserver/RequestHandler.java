package webserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpMethod;
import webserver.controller.Controller;
import webserver.controller.CreateUserController;
import webserver.controller.ListUserController;
import webserver.controller.LoginController;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	private static final String WEB_APP_PATH = "./webapp";
	private static final Map<String, Controller> controllers;

	static {
		controllers = new HashMap<>();
		controllers.put("/user/create", new CreateUserController());
		controllers.put("/user/list", new ListUserController());
		controllers.put("/user/login", new LoginController());
	}

	private final Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
			connection.getPort());

		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

			HttpRequest request = new HttpRequest(in);
			HttpResponse response = new HttpResponse(out);

			// static 파일 처리
			if (request.isStaticFileRequest()) {
				byte[] body = Files.readAllBytes(new File(WEB_APP_PATH + request.getPath()).toPath());
				response.response200Header(body.length, response.makeContentType(request));
				response.responseBody(body);
				return;
			}

			Controller controller = controllers.get(request.getPath());
			controller.service(request, response);

		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

}
