package com.bytes.message;

public class ResponseMetadata {
	private int likeCount;
	private int dislikeCount;
	private int viewsCount;
	private String comment;
	private String download;
	private String share;
	private String email;

	// Constructor
	public ResponseMetadata(int likeCount, int dislikeCount, int viewsCount, String comment, String share,
			String download, String email) {
		this.likeCount = likeCount;
		this.dislikeCount = dislikeCount;
		this.viewsCount = viewsCount;
		this.comment = comment;
		this.share = share;
		this.download = download;
		this.email = email;
	}

	// Getters and Setters (optional based on your needs)
	public String getemail() {
		return email;
	}

	public void setemail(String email) {
		this.email = email;
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

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
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
}
