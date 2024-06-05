package com.bytes.message;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ResponseFile {
	private String name;
	private String url;
	private String description;
	private String id;
	private LocalDateTime createdDateTime;
	@JsonIgnore
	private String type;

	public ResponseFile(String name, String url, String type, String size) {
		this.name = name;
		this.url = url;
		this.type = type;

	}

	public ResponseFile(String id, String name, String url, String description, LocalDateTime createdDateTime) {
		this.name = name;
		this.url = url;
		this.description = description;
		this.id = id;
		this.createdDateTime = createdDateTime;
	}

	public String getId() {
		return id;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDateTime getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(LocalDateTime createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

}
