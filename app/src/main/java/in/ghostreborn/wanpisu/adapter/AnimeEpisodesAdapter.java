package in.ghostreborn.wanpisu.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import in.ghostreborn.wanpisu.R;
import in.ghostreborn.wanpisu.constants.WanPisuConstants;
import in.ghostreborn.wanpisu.ui.ServersSelectActivity;

public class AnimeEpisodesAdapter extends RecyclerView.Adapter<AnimeEpisodesAdapter.ViewHolder> {

    int episodes = 0;
    Context context;
    String animeID;

    public AnimeEpisodesAdapter(int episodes, Context context, String animeID) {
        this.episodes = episodes;
        this.context = context;
        this.animeID = animeID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.anime_episodes_list, parent, false);
        return new AnimeEpisodesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        int correctPosition = position + 1;

        holder.episodeNumberTextView.setText(String.valueOf(correctPosition));
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ServersSelectActivity.class);
            WanPisuConstants.preferences.edit()
                            .putString(WanPisuConstants.ALL_ANIME_ANIME_EPISODE_NUMBER, String.valueOf(correctPosition))
                                    .apply();
            Log.e("LOG_TAG", "Episodes: " + WanPisuConstants.preferences.getString(WanPisuConstants.ALL_ANIME_ANIME_EPISODE_NUMBER, ""));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return episodes;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView episodeNumberTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            episodeNumberTextView = itemView.findViewById(R.id.episode_number_text_view);
        }
    }

}
