package webserver.controller;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class CreateUserController extends AbstractController {
	@Override
	public void doPost(HttpRequest httpRequest, HttpResponse httpResponse) {
		// User DB 저장
		DataBase.addUser(new User(httpRequest.getParameter("userId"), httpRequest.getParameter("password"),
			httpRequest.getParameter("name"), httpRequest.getParameter("email")));

		// 302 response (location : index.html)
		httpResponse.sendRedirect("/index.html");
	}

	@Override
	public void doGet(HttpRequest httpRequest, HttpResponse httpResponse) {
		// User DB 저장
		DataBase.addUser(
			new User(httpRequest.getParameter("userId"), httpRequest.getParameter("password"),
				httpRequest.getParameter("name"),
				httpRequest.getParameter("email")));

		// 302 response (location : index.html)
		httpResponse.sendRedirect("/index.html");

		return;
	}
}
