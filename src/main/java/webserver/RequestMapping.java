package webserver;

import java.util.HashMap;
import java.util.Map;

import webserver.controller.Controller;
import webserver.controller.CreateUserController;
import webserver.controller.ListUserController;
import webserver.controller.LoginController;

public class RequestMapping {
	private static final Map<String, Controller> controllers;

	static {
		controllers = new HashMap<>();
		controllers.put("/user/create", new CreateUserController());
		controllers.put("/user/list", new ListUserController());
		controllers.put("/user/login", new LoginController());
	}

	public static Controller getController(String requestUrl) {
		return controllers.get(requestUrl);
	}
}
