package com.bytes.message;

public class ResponseLike {
	private int likeCount;
	private int dislikeCount;
	private int viewsCount;
	private String comment;

	// Constructor
	public ResponseLike(int likeCount, int dislikeCount, int viewsCount, String comment) {
		this.likeCount = likeCount;
		this.dislikeCount = dislikeCount;
		this.viewsCount = viewsCount;
		this.comment = comment;
	}

	// Getters and Setters (optional based on your needs)
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
}
