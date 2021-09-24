package webserver.controller;

import util.HttpMethod;
import webserver.HttpRequest;
import webserver.HttpResponse;

public abstract class AbstractController implements Controller{

	@Override
	public void service(HttpRequest httpRequest, HttpResponse httpResponse) {
		HttpMethod method = httpRequest.getMethod();
		if (method.isPost()) {
			doPost(httpRequest, httpResponse);
		}

		if (method.isGet()) {
			doGet(httpRequest, httpResponse);
		}
	}

	protected void doPost(HttpRequest httpRequest, HttpResponse httpResponse){};
	protected void doGet(HttpRequest httpRequest, HttpResponse httpResponse){};

}
