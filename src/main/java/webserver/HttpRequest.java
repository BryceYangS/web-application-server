package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import util.HttpMethod;
import util.HttpRequestUtils;
import util.IOUtils;

/**
 * HTTP Request 정보
 */
public class HttpRequest {

	private RequestLine requestLine;
	private Map<String, String> header;
	private Map<String, String> cookies;
	private Map<String, String> parameter;

	public HttpRequest(InputStream in) throws IOException {
		InputStreamReader reader = new InputStreamReader(in, "UTF-8");
		BufferedReader br = new BufferedReader(reader);
		// Parse StartLine
		String startLine = br.readLine();
		if (startLine == null || "".equals(startLine)) {
			return;
		}

		requestLine = new RequestLine(startLine);
		header = parseHeader(br);

		// Parse Request Message Body
		if (requestLine.getMethod().isPost()) {
			String body = IOUtils.readData(br, Integer.parseInt(header.getOrDefault("Content-Length", "0")));
			parameter = HttpRequestUtils.parseQueryString(body);
		} else {
			parameter = requestLine.getParams();
		}

		// Parse Cookies
		cookies = HttpRequestUtils.parseCookies(getHeader("Cookie"));

	}

	private Map<String, String> parseHeader(BufferedReader br) throws IOException {
		Map<String,String> headers = new HashMap<>();
		while (true) {
			String s = br.readLine();
			if (s == null || "".equals(s)) {
				break;
			}

			HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(s);
			headers.put(pair.getKey(), pair.getValue());
		}
		return headers;
	}


	public boolean isLogin() {
		return Boolean.parseBoolean(cookies.getOrDefault("logined", "false"));
	}

	public HttpMethod getMethod() {
		return requestLine.getMethod();
	}

	public String getPath() {
		return requestLine.getPath();
	}

	public String getHeader(String key) {
		return header.getOrDefault(key, "");
	}

	public String getParameter(String key) {
		return parameter.getOrDefault(key, "");
	}

	public boolean isStaticFileRequest() {
		return requestLine.getPath().contains(".");
	}
}

