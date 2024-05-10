package com.bytes.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Table(name = "user")
public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "First name shouldn't be blank")
	private String firstName;

	@NotBlank(message = "Last name shouldn't be blank")
	private String lastName;

	@NotBlank(message = "Email shouldn't be blank")
	@Email(message = "Invalid email address")
	private String email;

//	@NotBlank(message = "Password is required")
	@Size(min = 6, message = "Password must be at least 6 characters long")
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=]).*$", message = "Password must contain at least one letter, one number, and one special character")
	private String password;

	private Double latitude;

	private Double longitude;

	// Modify profile to store either image data or initial letter
	@Column(length = 100000)
	private byte[] profileImage;

	@Column(name = "phone")
	private String phone;

	@Column(name = "created_date_time")
	private LocalDateTime createddatetime;

	@Column(name = "update_Date_Time")
	private LocalDateTime updateDateTime;

	@Column(name = "active")
	private char active; // Change the type to char

	public User() {
		this.createddatetime = LocalDateTime.now();
		this.updateDateTime = null;
	}

	// Getters and setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public byte[] getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(byte[] profileImage) {
		this.profileImage = profileImage;
	}

	public LocalDateTime getcreateddatetime() {
		return createddatetime;
	}

	public void setcreateddatetime(LocalDateTime createdAt) {
		this.createddatetime = createdAt;
	}

	public LocalDateTime getupdateDateTime() {
		return updateDateTime;
	}

	public void setupdateDateTime(LocalDateTime updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public char getActive() {
		return active;
	}

	public void setActive(char active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return "User{" + "id=" + id + ", profileImage=" + Arrays.toString(profileImage) + ", firstName='" + firstName
				+ '\'' + ", lastName='" + lastName + '\'' + ", email='" + email + '\'' + ", password='" + password
				+ '\'' + ", latitude=" + latitude + ", longitude=" + longitude + ", createdAt=" + createddatetime
				+ ", updatedAt=" + updateDateTime + '}';
	}
}