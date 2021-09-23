package webserver;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpMethod;
import util.HttpRequestUtils;

public class RequestLine {
	private static final Logger log = LoggerFactory.getLogger(RequestLine.class);

	private HttpMethod method;
	private String path;
	private Map<String, String> params = new HashMap<>();

	public RequestLine(String requestLine) {
		String[] tokens = requestLine.split(" ");
		if (tokens.length != 3) {
			throw new IllegalArgumentException(requestLine + "이 형식에 맞지 않습니다.");
		}
		method = HttpMethod.valueOf(tokens[0]);
		path = tokens[1];

		// Parse QueryString and Path
		if (method.isGet() && isContainsQueryStrings(path)) {
			int queryStringIndex = path.indexOf("?");
			String queryString = path.substring(queryStringIndex + 1);

			params = HttpRequestUtils.parseQueryString(queryString);
			path = path.substring(0, queryStringIndex);
		}
	}

	public HttpMethod getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public Map<String, String> getParams() {
		return params;
	}

	private boolean isContainsQueryStrings(String uri) {
		return uri.contains("?");
	}

}
