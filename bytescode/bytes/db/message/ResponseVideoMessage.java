package com.bezkoder.spring.files.upload.db.message;

public class ResponseVideoMessage {
  private String message;

  public ResponseVideoMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

}
