package in.ghostreborn.wanpisu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import in.ghostreborn.wanpisu.R;
import in.ghostreborn.wanpisu.model.Anilist;

public class AnilistRecyclerAdapter extends RecyclerView.Adapter<AnilistRecyclerAdapter.ViewHolder> {
    ArrayList<Anilist> anilists;

    public AnilistRecyclerAdapter(ArrayList<Anilist> anilists) {
        this.anilists = anilists;
    }

    @NonNull
    @Override
    public AnilistRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(in.ghostreborn.wanpisu.R.layout.anilist_anime_list, parent, false);
        return new AnilistRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnilistRecyclerAdapter.ViewHolder holder, int position) {
        holder.anilistAnimeTextView.setText(anilists.get(position).getAnimeName());
        holder.anilistAnimeMalTextView.setText(anilists.get(position).getMalID());
        Picasso.get().load(anilists.get(position).getAnimeImageUrl()).into(
                holder.anilistImageView
        );
    }

    @Override
    public int getItemCount() {
        return anilists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView anilistAnimeTextView;
        public TextView anilistAnimeMalTextView;
        public ImageView anilistImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            anilistAnimeTextView = itemView.findViewById(R.id.anilist_anime_text_view);
            anilistAnimeMalTextView = itemView.findViewById(R.id.anilist_anime_mal_text_view);
            anilistImageView = itemView.findViewById(R.id.anilist_anime_image_view);
        }
    }
}
