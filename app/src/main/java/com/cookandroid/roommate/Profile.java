package com.cookandroid.roommate;

public class Profile {
    private String name;
    private String studentId;
    private String gender;
    private String description;
    private String bedTime;
    private String hashKey;

    public Profile(String name, String studentId, String gender, String description, String bedTime) {
        this.name = name;
        this.studentId = studentId;
        this.gender = gender;
        this.description = description;
        this.bedTime = bedTime;
        this.hashKey = name + studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBedTime() {
        return bedTime;
    }

    public void setBedTime(String bedTime) {
        this.bedTime = bedTime;
    }

    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "name='" + name + '\'' +
                ", studentId='" + studentId + '\'' +
                ", gender='" + gender + '\'' +
                ", description='" + description + '\'' +
                ", bedTime='" + bedTime + '\'' +
                '}';
    }
}