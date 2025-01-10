package com.cookandroid.roommate;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cookandroid.roommate.api.AddLoginRequestDto;
import com.cookandroid.roommate.api.RetrofitClient;
import com.google.firebase.messaging.FirebaseMessaging;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private EditText etId, etPassword;
    private Button btnCreateProfile;
    private Button btnFindRoom, btnLogin;
    private Button btnRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etId = findViewById(R.id.etId);
        btnLogin = findViewById(R.id.btnLogin); // 로그인 버튼 초기화
        etPassword = findViewById(R.id.etPassword); // 비밀번호 입력란
        btnCreateProfile = findViewById(R.id.btnCreateProfile);
        btnFindRoom = findViewById(R.id.btnFindRoom);
        btnRating = findViewById(R.id.btnRating);

        btnLogin.setOnClickListener(v -> loginUser());
        // 프로필 저장 버튼 클릭 리스너

        // Android 13 이상에서 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1);
            }
        }

        // FCM 토큰 가져오기
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                return;
            }

            // FCM 토큰 가져오기
            String token = task.getResult();
            Log.d(TAG, "FCM Token: " + token);

            // 필요한 경우 서버에 토큰 전송
            sendTokenToServer(token);
        });

        initializeViews();
        setupClickListeners();
    }

    private void loginUser() {
        Log.d("TAG", "loginUser");
        String userId = etId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (userId.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // AddLoginRequestDto 객체 생성
        AddLoginRequestDto requestDto = new AddLoginRequestDto(userId, password);

        // RetrofitClient 객체를 통해 ApiService 호출
        RetrofitClient profileClient = RetrofitClient.getProfileInstance();  // 프로필 인스턴스 얻기
        profileClient.getApiService()  // getApiService 호출
                .loginUser(requestDto)  // AddLoginRequestDto 전달
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                        if (response.isSuccessful()) {
                            btnCreateProfile.setEnabled(true);
                            btnFindRoom.setEnabled(true);
                            btnRating.setEnabled(true);
                            Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.e("LoginError", "Error: " + t.getMessage()); // 네트워크 오류 로그 추가
                        Toast.makeText(MainActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "알림 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeViews() {
        btnCreateProfile = findViewById(R.id.btnCreateProfile);
        btnFindRoom = findViewById(R.id.btnFindRoom);
        btnRating = findViewById(R.id.btnRating);
    }

    private void setupClickListeners() {
        btnCreateProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        btnFindRoom.setOnClickListener(v -> {
            showMatchingDialog();
        });

        btnRating.setOnClickListener(v -> {
            final EditText input = new EditText(this);
            input.setHint("당신의 이름을 입력하세요");

            new AlertDialog.Builder(this)
                    .setTitle("평가하기")
                    .setMessage("평가를 위해 이름을 입력해주세요")
                    .setView(input)
                    .setPositiveButton("확인", (dialog, which) -> {
                        String name = input.getText().toString().trim();
                        if (!name.isEmpty()) {
                            Intent intent = new Intent(MainActivity.this, RatingListActivity.class);
                            intent.putExtra("user_name", name);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });
    }

    private void showMatchingDialog() {
        final EditText input = new EditText(this);
        input.setHint("당신의 이름을 입력하세요");

        new AlertDialog.Builder(this)
                .setTitle("룸메이트 찾기")
                .setMessage("매칭을 위해 이름을 입력해주세요")
                .setView(input)
                .setPositiveButton("확인", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        Intent intent = new Intent(MainActivity.this, ProfileListActivity.class);
                        intent.putExtra("matching_mode", true);
                        intent.putExtra("user_name", name);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void sendTokenToServer(String token) { // 서버 URL
        String serverUrl = "http://43.200.36.141:8080/"; // 실제 서버 URL로 변경하세요.

        // JSON 형식으로 토큰 전송
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        // JSON 요청 본문 생성
        String jsonBody = "{ \"token\": \"" + token + "\" }";
        RequestBody body = RequestBody.create(jsonBody, JSON);

        // 요청 생성
        Request request = new Request.Builder()
                .url(serverUrl)
                .post(body)
                .build();

        // 비동기로 서버 요청
        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Token successfully sent to server");
                } else {
                    Log.e(TAG, "Failed to send token: " + response.code());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending token to server", e);
            }
        }).start();
    }
}
