package com.bytes.message;

public class ResponseFile {
	private String name;
	private String url;
	private String type;

	public ResponseFile(String name, String url, String type, String size) {
		this.name = name;
		this.url = url;
		this.type = type;

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
