package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponse {
	private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
	private static final String WEB_APP_PATH = "./webapp";

	private DataOutputStream dos;
	private Map<String, String> header;

	public HttpResponse(OutputStream out) {
		dos = new DataOutputStream(out);
		header = new HashMap<>();
	}

	public void addHeader(String key, String value) {
		header.put(key, value);
	}

	public void forward(HttpRequest request, String url) throws IOException {

		String contentType = request == null ? "*/*" : makeContentType(request);
		byte[] body = forwardBody(url);

		response200Header(body.length, contentType);
		responseBody(body);
	}

	private byte[] forwardBody(String url) throws IOException {
		return Files.readAllBytes(new File(WEB_APP_PATH + url).toPath());
	}

	public String makeContentType(HttpRequest request) {
		String[] accepts = request.getHeader("Accept").split(",");
		String contentType = "*/*";
		String fileExt = request.getPath().substring(request.getPath().lastIndexOf(".") + 1);

		for (String accept : accepts) {
			if (accept.contains(fileExt)) {
				contentType = accept;
				break;
			}
		}
		return contentType;
	}

	public void response200Header(int lengthOfBodyContent, String contentType) {
		addHeader("Content-Type", contentType);
		addHeader("Content-Length", Integer.toString(lengthOfBodyContent));

		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			processHeaders();
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	public void responseBody(byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	public void sendRedirect(String url) throws IOException {
		addHeader("Location", url);

		dos.writeBytes("HTTP/1.1 302 Found \r\n");
		processHeaders();
		dos.writeBytes("\r\n");

	}

	private void processHeaders() throws IOException {

		Set<String> keys = header.keySet();
		for (String key : keys) {
			dos.writeBytes(key + ": " + header.get(key) + "\r\n");
		}
	}

}
