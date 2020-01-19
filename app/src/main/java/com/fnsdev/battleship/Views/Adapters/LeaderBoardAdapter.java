package com.fnsdev.battleship.Views.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fnsdev.battleship.Models.Profile;
import com.fnsdev.battleship.R;

import java.util.List;

public class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.RssViewHolder>{
    class RssViewHolder extends RecyclerView.ViewHolder {
        private final TextView emailTextView;
        private final TextView winsCountTextView;
        private final TextView lossesCountTextView;

        private RssViewHolder(View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            winsCountTextView = itemView.findViewById(R.id.winsCountTextView);
            lossesCountTextView = itemView.findViewById(R.id.lossesCountTextView);
        }
    }

    private final LayoutInflater inflater;
    private List<Profile> profiles;
    private Context context;


    public LeaderBoardAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public @NonNull RssViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.leader_board_item, parent, false);
        return new RssViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RssViewHolder holder, int position) {
        if(profiles != null) {
            Profile current = profiles.get(position);
            holder.emailTextView.setText(current.email);
            holder.winsCountTextView.setText("Wins: " + String.valueOf(current.winsCount));
            holder.lossesCountTextView.setText("Losses: " + String.valueOf(current.lossesCount));
        }
    }


    public void setProfiles(List<Profile> profiles){
        this.profiles = profiles;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (profiles != null)
            return profiles.size();
        else return 0;
    }
}
