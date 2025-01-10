package com.cookandroid.roommate;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cookandroid.roommate.api.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.ViewHolder> {
    private List<Profile> profileList;
    private String currentUserName;
    private Context context;
    private SharedPreferences ratingPrefs;

    public RatingAdapter(List<Profile> profileList, String currentUserName) {
        this.profileList = profileList;
        this.currentUserName = currentUserName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ratingPrefs = context.getSharedPreferences("ratings", Context.MODE_PRIVATE);
        View view = LayoutInflater.from(context).inflate(R.layout.item_rating, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Profile profile = profileList.get(position);
        holder.tvTargetName.setText(profile.getName());

        // 서버에서 평가 여부 확인
        RetrofitClient.getRatingInstance()
                .getApiService()
                .getRatings(profile.getName())
                .enqueue(new Callback<List<Rating>>() {
                    @Override
                    public void onResponse(Call<List<Rating>> call, Response<List<Rating>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Rating> ratings = response.body();
                            // 현재 사용자의 평가 찾기
                            Rating userRating = null;
                            for (Rating rating : ratings) {
                                if (rating.getRaterName().equals(currentUserName)) {
                                    userRating = rating;
                                    break;
                                }
                            }

                            if (userRating != null) {
                                // 이미 평가한 경우
                                holder.ratingBar.setRating(userRating.getRating());
                                holder.ratingBar.setIsIndicator(true);
                                holder.btnSubmitRating.setEnabled(false);
                                holder.btnSubmitRating.setText("평가 완료");
                            } else {
                                // 아직 평가하지 않은 경우
                                holder.ratingBar.setRating(0);
                                holder.ratingBar.setIsIndicator(false);
                                holder.btnSubmitRating.setEnabled(true);
                                holder.btnSubmitRating.setText("평가하기");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Rating>> call, Throwable t) {
                        Log.e("RatingAdapter", "Failed to get ratings: " + t.getMessage());
                    }
                });

        holder.btnSubmitRating.setOnClickListener(v -> {
            float rating = holder.ratingBar.getRating();
            if (rating > 0) {
                saveRating(profile.getName(), rating);
                holder.ratingBar.setIsIndicator(true);
                holder.btnSubmitRating.setEnabled(false);
                holder.btnSubmitRating.setText("평가 완료");
                Toast.makeText(context, "평가가 저장되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "별점을 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveRating(String targetName, float rating) {
        // 먼저 기존 평가 여부 확인
        RetrofitClient.getRatingInstance()
                .getApiService()
                .getRatings(targetName)
                .enqueue(new Callback<List<Rating>>() {
                    @Override
                    public void onResponse(Call<List<Rating>> call, Response<List<Rating>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Rating> ratings = response.body();
                            // 현재 사용자의 기존 평가 확인
                            boolean alreadyRated = false;
                            for (Rating r : ratings) {
                                if (r.getRaterName().equals(currentUserName)) {
                                    alreadyRated = true;
                                    break;
                                }
                            }

                            if (alreadyRated) {
                                Toast.makeText(context,
                                        "이미 평가한 사용자입니다.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // 기존 평가가 없는 경우에만 새로운 평가 저장
                            Rating ratingObj = new Rating(currentUserName, targetName, rating);
                            RetrofitClient.getRatingInstance()
                                    .getApiService()
                                    .saveRating(ratingObj)
                                    .enqueue(new Callback<Void>() {
                                        @Override
                                        public void onResponse(Call<Void> call, Response<Void> response) {
                                            if (response.isSuccessful()) {
                                                Toast.makeText(context,
                                                        "평가가 저장되었습니다.",
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(context,
                                                        "평가 저장에 실패했습니다.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Void> call, Throwable t) {
                                            Toast.makeText(context,
                                                    "네트워크 오류: " + t.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Rating>> call, Throwable t) {
                        Toast.makeText(context,
                                "평가 확인 중 오류 발생: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTargetName;
        RatingBar ratingBar;
        Button btnSubmitRating;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTargetName = itemView.findViewById(R.id.tvTargetName);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnSubmitRating = itemView.findViewById(R.id.btnSubmitRating);
        }
    }
}