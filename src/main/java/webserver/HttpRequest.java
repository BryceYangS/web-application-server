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

	private String method;
	private String path;
	private Map<String, String> header;
	private Map<String, String> parameter;

	public HttpRequest(InputStream in) throws IOException {
		InputStreamReader reader = new InputStreamReader(in, "UTF-8");
		BufferedReader br = new BufferedReader(reader);
		// Parse StartLine
		String startLine = br.readLine();
		String[] tokens = startLine.split(" ");
		method = tokens[0];
		path = tokens[1];

		// Parse QueryString and Path
		if (HttpMethod.GET.equals(method) && isContainsQueryStrings(path)) {
			int queryStringIndex = path.indexOf("?");
			String queryString = path.substring(queryStringIndex + 1);

			parameter = HttpRequestUtils.parseQueryString(queryString);
			path = path.substring(0, queryStringIndex);
		}

		// Parse Request Header
		header = new HashMap<>();
		while (true) {
			String s = br.readLine();
			if (s == null || "".equals(s)) {
				break;
			}

			HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(s);
			header.put(pair.getKey(), pair.getValue());
		}

		// Parse Request Message Body
		if (HttpMethod.POST.equals(method)) {
			String body = IOUtils.readData(br, Integer.parseInt(header.getOrDefault("Content-Length", "0")));
			parameter = HttpRequestUtils.parseQueryString(body);
		}
	}

	public String getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public String getHeader(String key) {
		return header.getOrDefault(key, "");
	}

	public String getParameter(String key) {
		return parameter.getOrDefault(key, "");
	}

	private boolean isContainsQueryStrings(String uri) {
		return uri.contains("?");
	}

}
