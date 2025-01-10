package com.cookandroid.roommate.api;

public class UserResponse {
    private String userId;
    private String name;  // 이 필드는 서버 응답에 맞게 정의된 사용자 이름입니다.
    private String token;

    // Getter와 Setter 메서드
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {  // 이 메서드가 실제로 사용자 이름을 반환합니다.
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
