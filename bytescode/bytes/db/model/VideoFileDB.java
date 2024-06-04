package com.bezkoder.spring.files.upload.db.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Column;
@Entity
@Table(name = "files")
public class VideoFileDB {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(name = "file_name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "file_type")
    private String type;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "latitude")
    private Long latitude;

    @Column(name = "longitude")
    private Long longitude;

    @Lob
    @Column(name = "file_data")
    private byte[] data;

    @Column(name = "update_date_time")
    private LocalDateTime updateDateTime;

    public VideoFileDB() {
    }

    public VideoFileDB(String name, String type, byte[] data) {
        this.name = name;
        this.type = type;
        this.data = data;
        this.updateDateTime = LocalDateTime.now();
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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
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

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(Long latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(Long longitude) {
    this.longitude = longitude;
  }
}
