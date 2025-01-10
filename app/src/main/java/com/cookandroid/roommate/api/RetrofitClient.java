package com.cookandroid.roommate.api;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class RetrofitClient {
    private static final String PROFILE_BASE_URL = "http://43.200.36.141:8080/";
    private static final String RATING_BASE_URL = "http://43.200.36.141:8080/";
    private static RetrofitClient profileInstance;
    private static RetrofitClient ratingInstance;
    private static Retrofit retrofit;  // static으로 변경

    // 생성자에서 Retrofit 객체 초기화
    private RetrofitClient(String baseUrl) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // 프로필 인스턴스 반환
    public static synchronized RetrofitClient getProfileInstance() {
        if (profileInstance == null) {
            profileInstance = new RetrofitClient(PROFILE_BASE_URL);
        }
        return profileInstance;
    }

    // 레이팅 인스턴스 반환
    public static synchronized RetrofitClient getRatingInstance() {
        if (ratingInstance == null) {
            ratingInstance = new RetrofitClient(RATING_BASE_URL);
        }
        return ratingInstance;
    }

    // Retrofit 인스턴스를 반환하는 메서드
    public static RoommateApiService getApiService() {
        return retrofit.create(RoommateApiService.class);  // static 방식 유지
    }

    // 로그인 및 회원가입 기능을 위한 API 호출
    public static Call<String> loginUser(AddLoginRequestDto requestDto) {
        return getApiService().loginUser(requestDto);  // 회원가입
    }

    public static Call<String> authenticateLogin(AddLoginRequestDto requestDto) {
        return getApiService().authenticateLogin(requestDto);  // 로그인 인증
    }
}
