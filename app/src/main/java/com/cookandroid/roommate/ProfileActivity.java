package com.cookandroid.roommate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cookandroid.roommate.api.RetrofitClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private EditText etName, etStudentId, etDescription;
    private RadioGroup rgGender, rgBedTime;
    private Button btnSave, btnLogin;
    private SharedPreferences preferences;
    private static final String PREF_NAME = "user_profile";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 뷰 초기화
        etName = findViewById(R.id.etName);
        etStudentId = findViewById(R.id.etStudentId);
        etDescription = findViewById(R.id.etDescription);
        rgGender = findViewById(R.id.rgGender);
        rgBedTime = findViewById(R.id.rgBedTime);
        btnSave = findViewById(R.id.btnSave);


        // SharedPreferences 초기화
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // 로그인 상태 확인
        checkLoginStatus();

        // 저장된 프로필 정보 불러오기
        loadProfile();

        // 로그인 버튼 클릭 리스너

        btnSave.setOnClickListener(v -> saveProfile());

    }

    private void checkLoginStatus() {
        boolean isLoggedIn = preferences.getBoolean("is_logged_in", false);
        if (isLoggedIn) {
            // 이미 로그인된 상태일 경우, 메인 화면으로 이동
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void loadProfile() {
        // 저장된 프로필 정보 불러오기
        etName.setText(preferences.getString("name", ""));
        etStudentId.setText(preferences.getString("studentId", ""));
        etDescription.setText(preferences.getString("description", ""));

        String gender = preferences.getString("gender", "");
        if (gender.equals("남성")) {
            rgGender.check(R.id.rbMale);
        } else if (gender.equals("여성")) {
            rgGender.check(R.id.rbFemale);
        }

        String bedTime = preferences.getString("bedTime", "");
        switch (bedTime) {
            case "12시 이전":
                rgBedTime.check(R.id.rbBefore12);
                break;
            case "12시~1시":
                rgBedTime.check(R.id.rb12to1);
                break;
            case "1시~2시":
                rgBedTime.check(R.id.rb1to2);
                break;
            case "2시 이후":
                rgBedTime.check(R.id.rbAfter2);
                break;
        }

        // 프로필이 이미 존재하면 버튼 텍스트 변경 및 필드 비활성화
        if (preferences.contains("name")) {
            btnSave.setText("프로필 수정");
            etName.setEnabled(false);
            etStudentId.setEnabled(false);
            for (int i = 0; i < rgGender.getChildCount(); i++) {
                rgGender.getChildAt(i).setEnabled(false);
            }
        }
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String studentId = etStudentId.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        RadioButton selectedGender = findViewById(rgGender.getCheckedRadioButtonId());
        String gender = selectedGender != null ? selectedGender.getText().toString() : "";

        RadioButton selectedBedTime = findViewById(rgBedTime.getCheckedRadioButtonId());
        String bedTime = selectedBedTime != null ? selectedBedTime.getText().toString() : "";

        if (name.isEmpty() || studentId.isEmpty() || description.isEmpty() ||
                gender.isEmpty() || bedTime.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 프로필 정보를 SharedPreferences에 저장
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("name", name);
        editor.putString("studentId", studentId);
        editor.putString("description", description);
        editor.putString("bedTime", bedTime);
        editor.putString("gender", gender);
        editor.apply();

        // 프로필 객체 생성
        Profile profile = new Profile(name, studentId, gender, description, bedTime);
        String hashKey = profile.getHashKey();

        try {
            // URL 인코딩된 해시키 생성
            String encodedHashKey = URLEncoder.encode(hashKey, StandardCharsets.UTF_8.toString());

            // 기존 프로필 삭제 후 새 프로필 저장
            RetrofitClient.getApiService()
                    .saveProfile(profile)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            handleProfileResponse(response, profile.getName());
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            handleProfileError(t);
                        }
                    });
        } catch (Exception e) {
            handleProfileError(e);
        }
    }

    private void handleProfileResponse(Response<Void> response, String name) {
        if (response.isSuccessful()) {
            Toast.makeText(ProfileActivity.this,
                    "프로필이 저장되었습니다.",
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.putExtra("user_name", name);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(ProfileActivity.this,
                    "프로필 저장에 실패했습니다.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void handleProfileError(Throwable t) {
        Toast.makeText(ProfileActivity.this,
                "네트워크 오류: " + t.getMessage(),
                Toast.LENGTH_SHORT).show();
    }



}
