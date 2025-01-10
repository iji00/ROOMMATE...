package com.cookandroid.roommate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RatingListActivity extends AppCompatActivity {
    private RecyclerView recyclerRatings;
    private RatingAdapter adapter;
    private List<Profile> rateableProfiles;
    private SharedPreferences preferences;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_list);

        currentUserName = getIntent().getStringExtra("user_name");
        preferences = getSharedPreferences("profiles", MODE_PRIVATE);

        recyclerRatings = findViewById(R.id.recyclerRatings);
        recyclerRatings.setLayoutManager(new LinearLayoutManager(this));

        rateableProfiles = new ArrayList<>();
        adapter = new RatingAdapter(rateableProfiles, currentUserName);
        recyclerRatings.setAdapter(adapter);

        loadRateableProfiles();
    }

    private void loadRateableProfiles() {
        // 평가 가능한 프로필 목록 로드 (요청했던 사람들)
        SharedPreferences requestPrefs = getSharedPreferences("requests", MODE_PRIVATE);
        Set<String> requestedUsers = requestPrefs.getStringSet(currentUserName + "_requests", new HashSet<>());

        // 디버그 메시지 추가
        Toast.makeText(this,
                "요청한 사용자 수: " + requestedUsers.size(),
                Toast.LENGTH_SHORT).show();

        String json = preferences.getString("profile_list", null);
        if (json != null) {
            Gson gson = new Gson();
            Profile[] profiles = gson.fromJson(json, Profile[].class);

            for (Profile profile : profiles) {
                if (requestedUsers.contains(profile.getName())) {
                    rateableProfiles.add(profile);
                }
            }

            // 디버그 메시지 추가
            Toast.makeText(this,
                    "평가 가능한 프로필 수: " + rateableProfiles.size(),
                    Toast.LENGTH_SHORT).show();

            adapter.notifyDataSetChanged();
        }
    }
}