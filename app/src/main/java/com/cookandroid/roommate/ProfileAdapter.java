package com.cookandroid.roommate;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cookandroid.roommate.api.NotificationRequest;
import com.cookandroid.roommate.api.RetrofitClient;
import com.cookandroid.roommate.api.RoommateApiService;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {
    private List<Profile> profileList;
    private Context context;
    private static final String CHANNEL_ID = "roommate_request";
    private static final String CHANNEL_NAME = "Roommate Requests";
    private String currentUserName;

    public ProfileAdapter(List<Profile> profileList, String currentUserName) {
        this.profileList = profileList;
        this.currentUserName = currentUserName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Profile profile = profileList.get(position);
        holder.tvName.setText("이름: " + profile.getName());
        holder.tvStudentId.setText("학번: " + profile.getStudentId());
        holder.tvGender.setText("성별: " + profile.getGender());
        holder.tvDescription.setText("자기소개: " + profile.getDescription());
        holder.tvBedTime.setText("취침시간: " + profile.getBedTime());

        // 서버에서 평점 정보 가져오기
        RetrofitClient.getRatingInstance()
                .getApiService()
                .getRatings(profile.getName())
                .enqueue(new Callback<List<Rating>>() {
                    @Override
                    public void onResponse(Call<List<Rating>> call, Response<List<Rating>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Rating> ratings = response.body();
                            // 디버그 로그 추가
                            Log.d("ProfileAdapter", "Ratings for " + profile.getName() + ": " + ratings.size());
                            for (Rating rating : ratings) {
                                Log.d("ProfileAdapter", "Rating from " + rating.getRaterName() + ": " + rating.getRating());
                            }

                            if (!ratings.isEmpty()) {
                                // 평균 평점 계산
                                float sum = 0;
                                for (Rating rating : ratings) {
                                    sum += rating.getRating();
                                }
                                float avgRating = sum / ratings.size();

                                String ratingText = String.format("평점: %.1f (%d)", avgRating, ratings.size());
                                holder.tvRating.setText(ratingText);
                                holder.tvRating.setVisibility(View.VISIBLE);
                            } else {
                                holder.tvRating.setVisibility(View.GONE);
                                Log.d("ProfileAdapter", "No ratings found for " + profile.getName());
                            }
                        } else {
                            // 오류 응답 로그 추가
                            Log.e("ProfileAdapter", "Error response: " + response.code());
                            try {
                                Log.e("ProfileAdapter", "Error body: " + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            holder.tvRating.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Rating>> call, Throwable t) {
                        // 네트워크 오류 로그 추가
                        Log.e("ProfileAdapter", "Network error: " + t.getMessage());
                        t.printStackTrace();
                        holder.tvRating.setVisibility(View.GONE);
                    }
                });

        // 이미 요청을 보낸 사용자인지 확인
        SharedPreferences requestPrefs = context.getSharedPreferences("requests", Context.MODE_PRIVATE);
        Set<String> requestedUsers = requestPrefs.getStringSet(currentUserName + "_requests", new HashSet<>());

        if (requestedUsers.contains(profile.getName())) {
            // 이미 요청을 보낸 경우
            holder.btnRequest.setEnabled(false);
            holder.btnRequest.setText("요청 완료");
        } else {
            // 아직 요청을 보내지 않은 경우
            holder.btnRequest.setEnabled(true);
            holder.btnRequest.setText("룸메 요청");
        }

        holder.btnRequest.setOnClickListener(v -> {
            // 한번 더 확인 (동시성 문제 방지)
            Set<String> currentRequests = requestPrefs.getStringSet(currentUserName + "_requests", new HashSet<>());
            if (currentRequests.contains(profile.getName())) {
                Toast.makeText(context, "이미 요청을 보낸 사용자입니다.", Toast.LENGTH_SHORT).show();
                holder.btnRequest.setEnabled(false);
                holder.btnRequest.setText("요청 완료");
                return;
            }

            // SharedPreferences에서 현재 사용자의 프로필 찾기
            SharedPreferences preferences = context.getSharedPreferences("profiles", Context.MODE_PRIVATE);
            String json = preferences.getString("profile_list", null);
            if (json != null) {
                Gson gson = new Gson();
                Profile[] profiles = gson.fromJson(json, Profile[].class);
                Profile requesterProfile = null;

                for (Profile p : profiles) {
                    if (p.getName().equals(currentUserName)) {
                        requesterProfile = p;
                        break;
                    }
                }

                if (requesterProfile != null) {
                    sendRoommateRequest(profile, requesterProfile);
                } else {
                    Toast.makeText(context, "프로필을 먼저 등록해주세요.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void sendRoommateRequest(Profile receiverProfile, Profile requesterProfile) {
        String message = "룸메이트 요청이 있습니다."; // 알림 메시지
        String receiverId = receiverProfile.getName(); // B 디바이스의 식별자 (예: 이름)

        // 서버에 알림 요청 전송
        sendNotificationToServer(receiverId, message);
    }

    private void sendNotificationToServer(String receiverId, String message) {
        // RetrofitClient의 인스턴스를 가져옵니다.
        RoommateApiService apiService = RetrofitClient.getProfileInstance().getApiService();

        // NotificationRequest 객체 생성
        NotificationRequest notificationRequest = new NotificationRequest(receiverId, message);

        // 서버에 알림 요청 전송
        apiService.sendNotification(notificationRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "알림이 전송되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "알림 전송 실패.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // 알림 표시를 위한 별도 메서드
    private void showNotification(Profile requesterProfile, Profile receiverProfile) {
        String bigText = String.format(
                "%s님이 %s님에게 룸메이트 요청을 보냈습니다.\n\n" +
                        "요청자 정보:\n" +
                        "이름: %s\n" +
                        "학번: %s\n" +
                        "성별: %s\n" +
                        "취침시간: %s\n" +
                        "자기소개: %s",
                requesterProfile.getName(),
                receiverProfile.getName(),
                requesterProfile.getName(),
                requesterProfile.getStudentId(),
                requesterProfile.getGender(),
                requesterProfile.getBedTime(),
                requesterProfile.getDescription()
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("새로운 룸메이트 요청")
                .setContentText(requesterProfile.getName() + "님이 룸메이트가 되고 싶어합니다.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle("룸메이트 요청")
                        .setSummaryText("요청자 정보")
                        .bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);  // 중요도를 HIGH로 변경
            channel.setDescription("룸메이트 요청 알림");
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStudentId, tvGender, tvDescription, tvBedTime, tvRating;
        Button btnRequest;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvGender = itemView.findViewById(R.id.tvGender);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvBedTime = itemView.findViewById(R.id.tvBedTime);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnRequest = itemView.findViewById(R.id.btnRequest);
        }
    }
}