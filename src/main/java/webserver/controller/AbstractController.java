package webserver.controller;

import java.io.IOException;

import util.HttpMethod;
import webserver.HttpRequest;
import webserver.HttpResponse;

public abstract class AbstractController implements Controller{

	@Override
	public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
		HttpMethod method = httpRequest.getMethod();
		if (method.isPost()) {
			doPost(httpRequest, httpResponse);
		}

		if (method.isGet()) {
			doGet(httpRequest, httpResponse);
		}
	}

	abstract void doPost(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException;
	abstract void doGet(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException;

}
