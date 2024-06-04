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

/**
 * Represents a data entity.
 */
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

    @OneToOne(mappedBy = "bytes", cascade = CascadeType.ALL)
    private Metadata metadata;

    @Column(name = "file_type")
    private String type;

    @Column(name = "description")
    private String description;

    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;

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

    /**
     * Default constructor.
     */
    public Bytes() {
    }

    /**
     * Constructor with parameters.
     * 
     * @param name The name of the data.
     * @param user The user associated with the data.
     * @param type The type of the data.
     * @param data The actual data.
     */
    public Bytes(String name, User user, String type, byte[] data) {
        this.name = name;
        this.user = user;
        this.type = type;
        this.data = data;
        this.updateDateTime = null;
        this.createdDateTime = LocalDateTime.now();
    }

    /**
     * Constructor with parameters.
     * 
     * @param fileName     The name of the file.
     * @param contentType  The content type of the file.
     * @param bytes        The data bytes.
     */
    public Bytes(String fileName, String contentType, byte[] bytes) {
        // TODO Auto-generated constructor stub
    }

    // Getters and setters

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
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

    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(LocalDateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
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
