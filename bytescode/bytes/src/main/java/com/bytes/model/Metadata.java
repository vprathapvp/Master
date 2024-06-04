package com.bytes.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "bytes_meta_data")
public class Metadata {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Use GenerationType.IDENTITY for MySQL
	@Column(name = "metadata_id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "video_id", referencedColumnName = "id")

	private Bytes bytes;
	@Column(name = "email")
	private String email;

	@Column(name = "comments")
	private String comments;

	@Column(name = "download")
	private String download;

	@Column(name = "share")
	private String share;

	@Column(name = "like_count")
	private int likeCount;

	@Column(name = "dislike_count")
	private int dislikeCount;

	@Column(name = "views_count")
	private int viewsCount;

	@Column(name = "liked_users")
	private String likedUsers;

	@Column(name = "disliked_users")
	private String dislikedUsers;

	// Constructors, getters, and setters

	public Long getId() {
		return id;
	}

	public Long setvideoid() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getemail() {
		return email;
	}

	public void setemail(String email) {
		this.email = email;
	}

	public Bytes getBytes() {
		return bytes;
	}

	public void setBytes(Bytes bytes) {
		this.bytes = bytes;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getDownload() {
		return download;
	}

	public void setDownload(String download) {
		this.download = download;
	}

	public String getShare() {
		return share;
	}

	public void setShare(String share) {
		this.share = share;
	}

	public int getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	public int getDislikeCount() {
		return dislikeCount;
	}

	public void setDislikeCount(int dislikeCount) {
		this.dislikeCount = dislikeCount;
	}

	public int getViewsCount() {
		return viewsCount;
	}

	public void setViewsCount(int viewsCount) {
		this.viewsCount = viewsCount;
	}

	public String getLikedUsers() {
		return likedUsers;
	}

	public void setLikedUsers(String likedUsers) {
		this.likedUsers = likedUsers;
	}

	public String getDislikedUsers() {
		return dislikedUsers;
	}

	public void setDislikedUsers(String dislikedUsers) {
		this.dislikedUsers = dislikedUsers;
	}

}
