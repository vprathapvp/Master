package com.bytes.message;

import java.time.LocalDateTime;

public class ResponseBytesFile {
	private String name;
	private String url;
	private String description;
	private String id;
	private String firstname;
	private String lastname;
	private LocalDateTime createdDateTime;
    private String userName;

	public ResponseBytesFile(String id, String name, String url, String description, String firstname,
			String lastname) {
		this.name = name;
		this.url = url;
		this.description = description;
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
	}

	public ResponseBytesFile(String id, String name, String url, String description) {
		this.name = name;
		this.url = url;
		this.description = description;
		this.id = id;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFirstname() {
		return firstname;
	}

	// Setter for firstname
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	// Getter for lastname
	public String getLastname() {
		return lastname;
	}

	// Setter for lastname
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
}