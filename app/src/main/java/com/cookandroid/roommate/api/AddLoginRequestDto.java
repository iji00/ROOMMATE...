package com.cookandroid.roommate.api;

public class AddLoginRequestDto {

    private String id;
    private String password;

    // 생성자
    public AddLoginRequestDto(String id, String password) {
        this.id = id;
        this.password = password;
    }

    // getter, setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
