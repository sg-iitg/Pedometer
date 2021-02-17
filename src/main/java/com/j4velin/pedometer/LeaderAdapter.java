package com.j4velin.pedometer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LeaderAdapter extends RecyclerView.Adapter<LeaderView> {
    public LeaderAdapter(ArrayList<LeaderBoard> leaderBoards) {
        this.leaderBoards = leaderBoards;
    }
    ArrayList<LeaderBoard>leaderBoards;

    @NonNull
    @Override
    public LeaderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboardscore,parent,false);
        return new LeaderView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderView holder, int position) {
        holder.textView.setText(leaderBoards.get(position).getName());
       // holder.timeview.setText(leaderBoards.get(position).getTime());

    }

    @Override
    public int getItemCount() {
        return leaderBoards.size();
    }
}
