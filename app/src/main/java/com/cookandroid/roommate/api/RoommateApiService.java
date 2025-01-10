package com.cookandroid.roommate.api;

import com.cookandroid.roommate.Profile;
import com.cookandroid.roommate.Rating;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface RoommateApiService {

    // 프로필 저장
    @POST("profiles")
    Call<Void> saveProfile(@Body Profile profile);

    // 프로필 목록 가져오기
    @GET("profiles")
    Call<List<Profile>> getProfiles();

    // 프로필 이름으로 가져오기
    @GET("profiles/{name}")
    Call<Profile> getProfile(@Path("name") String name);

    // 요청 저장
    @POST("requests")
    Call<Void> saveRequest(@Body RoommateRequest request);

    // 평점 저장
    @POST("ratings")
    Call<Void> saveRating(@Body Rating rating);

    // 프로필 업데이트
    @PUT("profiles/{hashKey}")
    Call<Void> updateProfile(@Path("hashKey") String hashKey, @Body Profile profile);

    // 프로필 삭제
    @DELETE("profiles/{hashKey}")
    Call<Void> deleteProfile(@Path("hashKey") String hashKey);

    // 평점 목록 가져오기
    @GET("ratings")
    Call<List<Rating>> getRatings(@Query("targetName") String targetName);

    // 알림 전송
    @POST("sendNotification")
    Call<Void> sendNotification(@Body NotificationRequest request);

    // FCM 알림 전송
    @POST("fcm/send")
    Call<Void> sendNotification(
            @Header("Authorization") String serverKey,
            @Body NotificationRequest notificationRequest);

    // 로그인: 로그인 인증
    @POST("login/authenticate")
    Call<String> authenticateLogin(@Body AddLoginRequestDto requestDto);

    // 회원가입: 새 사용자 추가
    @POST("login")
    Call<String> loginUser(@Body AddLoginRequestDto requestDto);
}
