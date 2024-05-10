package com.bytes.model;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "bytes")
public class Bytes {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	@Column(name = "bytes_name")
	private String name;

	@ManyToOne
	@JoinColumn(name = "user_email", referencedColumnName = "email")
	private User user;
//    private String email;
	@OneToOne(mappedBy = "bytes", cascade = CascadeType.ALL)
	private Metadata metadata;

	@Column(name = "file_type")
	private String type;

	@Column(name = "description")
	private String description;

	@Column(name = "created_date_time")
	private LocalDateTime createddatetime;

	@Lob
	@Column(name = "bytes_data")
	private byte[] data;

	@Column(name = "update_date_time")
	private LocalDateTime updateDateTime;

	@Column(name = "video_duration")
	private String videoDuration;

	@Column(name = "video")
	private String video;
	private String latitude;

	private String longitude;

	public Bytes() {
	}

	public Bytes(String name, User user, String type, byte[] data) {
		this.name = name;
		this.user = user;
		this.type = type;
		this.data = data;
		this.updateDateTime = null;
		this.createddatetime = LocalDateTime.now();
	}

	public Bytes(String fileName, String contentType, byte[] bytes) {
		// TODO Auto-generated constructor stub
	}
	// Other fields and getters/setters

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	// Getters and setters
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public LocalDateTime getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(LocalDateTime updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public LocalDateTime getcreateddatetime() {
		return createddatetime;
	}

	public void setcreateddatetime(LocalDateTime createddatetime) {
		this.createddatetime = createddatetime;
	}

	public String getdescription() {
		return description;
	}

	public void setdescription(String description) {
		this.description = description;
	}

	public String getVideoDuration() {
		return videoDuration;
	}

	public void setVideoDuration(String videoDuration) {
		this.videoDuration = videoDuration;
	}

	public String getVideo() {
		return video;
	}

	public void setVideo(String video) {
		this.video = video;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
}