package webserver.controller;

import java.util.Collection;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class ListUserController extends AbstractController{
	@Override
	void doPost(HttpRequest httpRequest, HttpResponse httpResponse) {
		return;
	}

	@Override
	void doGet(HttpRequest httpRequest, HttpResponse httpResponse) {
		if (!isLogin(httpRequest)) {
			httpResponse.sendRedirect("/user/login_failed.html");
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

		httpResponse.forwardBody(httpRequest, builder.toString());
	}

	private boolean isLogin(HttpRequest httpRequest) {
		return httpRequest.isLogin();
	}
}
