package util;

public enum HttpMethod {
	GET,
	HEAD,
	POST,
	PUT,
	PATCH,
	DELETE,
	OPTIONS,
	TRACE;

	public boolean equals(String method) {
		return name().equals(method);
	}

	public boolean isGet() {
		return this == GET;
	}

	public boolean isPost() {
		return this == POST;
	}
}
