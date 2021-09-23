package webserver.controller;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class LoginController extends AbstractController {
	@Override
	void doPost(HttpRequest httpRequest, HttpResponse httpResponse) {
		User user = DataBase.findUserById(httpRequest.getParameter("userId"));
		if (user != null && user.getPassword().equals(httpRequest.getParameter("password"))) {
			httpResponse.addHeader("Set-Cookie", "logined=true");
			httpResponse.sendRedirect("/index.html");
			return;
		}

		httpResponse.sendRedirect("/user/login_failed.html");
	}

	@Override
	void doGet(HttpRequest httpRequest, HttpResponse httpResponse) {
		return;
	}
}
