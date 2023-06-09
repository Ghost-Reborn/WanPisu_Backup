package in.ghostreborn.wanpisu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentContainerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import in.ghostreborn.wanpisu.adapter.AnimeSearchAdapter;
import in.ghostreborn.wanpisu.constants.WanPisuConstants;
import in.ghostreborn.wanpisu.model.WanPisu;
import in.ghostreborn.wanpisu.parser.AllAnime;
import in.ghostreborn.wanpisu.ui.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    public static ArrayList<WanPisu> animeDetailsArray;
    public static AnimeSearchAdapter adapter;
    ConstraintLayout allAnimeConstraintLayout;
    FragmentContainerView anilistAnimeContainerView;
    ProgressBar allAnimeProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        allAnimeConstraintLayout = findViewById(R.id.all_anime_constraint_layout);
        anilistAnimeContainerView = findViewById(R.id.anilist_login_fragment_container);
        allAnimeProgressBar = findViewById(R.id.all_anime_progress_bar);

        getAnilistTokenFromIntentFilter();
        setData();

        RecyclerView animeContainerView = findViewById(R.id.anime_container);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 3);
        animeContainerView.setLayoutManager(gridLayoutManager);

        ImageView settingsImageView = findViewById(R.id.settings_image_view);
        settingsImageView.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        // Get latest anime updates
        Executor executor = Executors.newSingleThreadExecutor();
        Runnable task = () -> {
            WanPisuConstants.wanPisus = new ArrayList<>();
            AllAnime.getUsersAnime(MainActivity.this);
            adapter = new AnimeSearchAdapter(MainActivity.this,WanPisuConstants.wanPisus);
            runOnUiThread(() -> {
                allAnimeProgressBar.setVisibility(View.GONE);
                animeContainerView.setAdapter(adapter);
            });
        };
        executor.execute(task);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Runnable task = () -> {
                    animeDetailsArray = AllAnime.parseAnimeIDAnimeNameAnimeThumbnail(
                            searchView.getQuery().toString()
                    );
                    adapter = new AnimeSearchAdapter(MainActivity.this, animeDetailsArray);
                    runOnUiThread(() -> animeContainerView.setAdapter(adapter));
                };
                executor.execute(task);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

    }

    private void getAnilistTokenFromIntentFilter(){
        SharedPreferences preferences = getSharedPreferences(WanPisuConstants.WAN_PISU_PREFERENCE, Context.MODE_PRIVATE);
        Intent urlIntent = getIntent();
        Uri data = urlIntent.getData();
        if (data != null){
            String url = data.toString();
            String token = url.substring(
                    url.indexOf("token=") + 6,
                    url.indexOf("&token_type")
            );
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(WanPisuConstants.WAN_PISU_ANILIST_TOKEN, token);
            editor.apply();
        }
    }

    private void setData(){
        WanPisuConstants.preferences = getSharedPreferences(WanPisuConstants.WAN_PISU_PREFERENCE, MODE_PRIVATE);
        String TOKEN = WanPisuConstants.preferences.getString(WanPisuConstants.WAN_PISU_ANILIST_TOKEN, "");
        if (TOKEN != ""){
            anilistAnimeContainerView.setVisibility(View.GONE);
        }else {
            allAnimeConstraintLayout.setVisibility(View.GONE);
        }
    }

}