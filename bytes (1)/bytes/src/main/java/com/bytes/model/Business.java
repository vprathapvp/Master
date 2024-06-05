package com.bytes.model;

import javax.persistence.*;

@Entity
@Table(name = "business")
public class Business {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "distance_range")
	private String distanceRange;

	@Column(name = "subscription")
	private String subscription;

	@Column(name = "payment_amount")
	private double paymentAmount;
	
	@Column(name = "email")
	private String email;
	

	@Column(name = "latitude")
	private String latitude;
	

	@Column(name = "longitude")
	private String longitude;

	@Lob
	@Column(name = "ad", columnDefinition = "LONGBLOB")
	private byte[] ad;

	// Constructors, Getters, and Setters
	public Business() {
	}

	public Business(byte[] ad, String distanceRange, String subscription, double paymentAmount) {
		this.ad = ad;
		this.distanceRange = distanceRange;
		this.subscription = subscription;
		this.paymentAmount = paymentAmount;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public byte[] getAd() {
		return ad;
	}

	public void setAd(byte[] ad) {
		this.ad = ad;
	}

	public String getDistanceRange() {
		return distanceRange;
	}

	public void setDistanceRange(String distanceRange) {
		this.distanceRange = distanceRange;
	}

	public String getSubscription() {
		return subscription;
	}

	public void setSubscription(String subscription) {
		this.subscription = subscription;
	}

	public double getPaymentAmount() {
		return paymentAmount;
	}

	public void setPaymentAmount(double paymentAmount) {
		this.paymentAmount = paymentAmount;
	}
	
	public String getemail() {
		return email;
	}

	public void setemail(String email) {
		this.email = email;
	}
	
	public String getlatitude() {
		return latitude;
	}

	public void setlatitude(String latitude) {
		this.latitude = latitude;
	}
	
	public String getlongitude() {
		return longitude;
	}

	public void setlongitude(String longitude) {
		this.longitude = longitude;
	}
}
