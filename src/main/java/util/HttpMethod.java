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

	public static boolean isGet(String httpMethod) {
		return GET.name().equalsIgnoreCase(httpMethod);
	}

	public static boolean isGetOrHead(String httpMethod) {
		return isGet(httpMethod) || HEAD.name().equalsIgnoreCase(httpMethod);
	}

}
