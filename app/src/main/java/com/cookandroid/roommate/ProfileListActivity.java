package com.cookandroid.roommate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cookandroid.roommate.api.RetrofitClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileListActivity extends AppCompatActivity {

    private RecyclerView recyclerProfiles;
    private ProfileAdapter adapter;
    private List<Profile> profileList;
    private SharedPreferences preferences;
    private static final String PREF_NAME = "profiles";
    private static final String KEY_PROFILES = "profile_list";
    private boolean isMatchingMode;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_list);

        // 매칭 모드 확인
        isMatchingMode = getIntent().getBooleanExtra("matching_mode", false);
        if (isMatchingMode) {
            userName = getIntent().getStringExtra("user_name");
        }

        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        recyclerProfiles = findViewById(R.id.recyclerProfiles);
        recyclerProfiles.setLayoutManager(new LinearLayoutManager(this));

        profileList = new ArrayList<>();
        adapter = new ProfileAdapter(profileList, userName);
        recyclerProfiles.setAdapter(adapter);

        loadProfiles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfiles(); // 화면이 다시 보일 때마다 프로필 목록 새로고침
    }

    private void loadProfiles() {
        RetrofitClient.getProfileInstance()
                .getApiService()
                .getProfiles()
                .enqueue(new Callback<List<Profile>>() {
                    @Override
                    public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            profileList.clear();

                            // 디버그용 로그 추가
                            for (Profile profile : response.body()) {
                                Log.d("ProfileList", "Profile: " + profile.getName() +
                                        ", StudentId: " + profile.getStudentId());
                            }

                            // 자신을 제외한 프로필 추가
                            for (Profile profile : response.body()) {
                                if (userName != null && !profile.getName().equals(userName)) {
                                    // bedTime이 null이면 기본값 설정
                                    if (profile.getBedTime() == null) {
                                        profile.setBedTime("선택 안함");
                                    }
                                    profileList.add(profile);
                                } else if (userName == null) {
                                    if (profile.getBedTime() == null) {
                                        profile.setBedTime("선택 안함");
                                    }
                                    profileList.add(profile);
                                }
                            }

                            // 매칭 모드 처리
                            if (isMatchingMode && userName != null) {
                                // 사용자 프로필 찾기
                                Profile userProfile = null;
                                for (Profile profile : response.body()) {
                                    if (profile.getName().equals(userName)) {
                                        userProfile = profile;
                                        break;
                                    }
                                }

                                if (userProfile != null) {
                                    // 매칭 알고리즘 적용
                                    List<Profile> sameGenderSameBedTime = new ArrayList<>();    // 같은 성별, 같은 취침시간
                                    List<Profile> sameGenderDiffBedTime = new ArrayList<>();    // 같은 성별, 다른 취침시간
                                    List<Profile> differentGenderProfiles = new ArrayList<>();   // 다른 성별

                                    // 성별과 취침시간에 따라 프로필 분류
                                    for (Profile profile : profileList) {
                                        if (profile.getGender().equals(userProfile.getGender())) {
                                            if (profile.getBedTime().equals(userProfile.getBedTime())) {
                                                sameGenderSameBedTime.add(profile);
                                            } else {
                                                sameGenderDiffBedTime.add(profile);
                                            }
                                        } else {
                                            differentGenderProfiles.add(profile);
                                        }
                                    }

                                    // 학번 차이로 정렬하는 Comparator
                                    final Profile finalUserProfile = userProfile;
                                    Comparator<Profile> studentIdComparator = (p1, p2) -> {
                                        int diff1 = Math.abs(Integer.parseInt(p1.getStudentId()) -
                                                Integer.parseInt(finalUserProfile.getStudentId()));
                                        int diff2 = Math.abs(Integer.parseInt(p2.getStudentId()) -
                                                Integer.parseInt(finalUserProfile.getStudentId()));
                                        return diff1 - diff2;
                                    };

                                    // 각 그룹 내에서 학번 차이로 정렬
                                    Collections.sort(sameGenderSameBedTime, studentIdComparator);
                                    Collections.sort(sameGenderDiffBedTime, studentIdComparator);
                                    Collections.sort(differentGenderProfiles, studentIdComparator);

                                    // 결과 합치기 (우선순위: 같은 성별&같은 취침시간 > 같은 성별&다른 취침시간 > 다른 성별)
                                    profileList.clear();
                                    profileList.addAll(sameGenderSameBedTime);
                                    profileList.addAll(sameGenderDiffBedTime);
                                    profileList.addAll(differentGenderProfiles);
                                } else {
                                    Toast.makeText(ProfileListActivity.this,
                                            "프로필을 먼저 등록해주세요.", Toast.LENGTH_LONG).show();
                                    finish();
                                    return;
                                }
                            }

                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Profile>> call, Throwable t) {
                        Toast.makeText(ProfileListActivity.this,
                                "프로필 목록을 불러오는데 실패했습니다: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        t.printStackTrace(); // 로그캣에 상세 오류 출력
                    }
                });
    }
} 