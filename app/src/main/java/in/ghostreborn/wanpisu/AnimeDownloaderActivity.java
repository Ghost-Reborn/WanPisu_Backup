package in.ghostreborn.wanpisu;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;

import in.ghostreborn.wanpisu.adapter.AnimeDownloadAdapter;
import in.ghostreborn.wanpisu.constants.WanPisuConstants;
import in.ghostreborn.wanpisu.model.AnimeDown;
import in.ghostreborn.wanpisu.parser.AllAnime;

public class AnimeDownloaderActivity extends AppCompatActivity {

    EditText animeEpisodeEditText;
    RecyclerView animeDownloadRecycler;
    ArrayList<AnimeDown> animeDowns;
    AnimeDownloadAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_downloader);

        TextView animeName = findViewById(R.id.anime_download_text_view);
        TextView totalEpisodes = findViewById(R.id.anime_total_text_view);
        animeDownloadRecycler = findViewById(R.id.anime_download_recycler);
        animeEpisodeEditText = findViewById(R.id.anime_episode_edit_text);
        animeName.setText(WanPisuConstants.preferences.getString(WanPisuConstants.ALL_ANIME_ANIME_NAME, "0"));
        totalEpisodes.setText(WanPisuConstants.preferences.getString(WanPisuConstants.ALL_ANIME_ANIME_EPISODES, "0"));
        Button animeDownloadButton = findViewById(R.id.anime_download_button);
        animeDownloadButton.setOnClickListener(view -> {
            new AnimeDownloadTask().execute();
        });

        animeDowns = new ArrayList<>();
        adapter = new AnimeDownloadAdapter(animeDowns);
        LinearLayoutManager manager = new LinearLayoutManager(AnimeDownloaderActivity.this);
        animeDownloadRecycler.setLayoutManager(manager);
        animeDownloadRecycler.setAdapter(adapter);

    }

    private class AnimeDownloadTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String episodes = animeEpisodeEditText.getText().toString();
            ArrayList<String> servers = AllAnime.getAnimeServer(WanPisuConstants.preferences.getString(WanPisuConstants.ALL_ANIME_ANIME_ID, ""), episodes);
            String srvr = "";
            for (String server: servers) {
                if (server.contains("workfields")){
                    srvr = server;
                }
            }
            WanPisuDownloadManager downloadManager = new WanPisuDownloadManager();
            try {
                String animeName = WanPisuConstants.preferences
                                .getString(WanPisuConstants.ALL_ANIME_ANIME_NAME, "");
                animeDowns.add(new AnimeDown(animeName, 0));
                downloadManager.download(srvr, "/sdcard/file.mp4",new WanPisuDownloadManager.ProgressListener() {
                    @Override
                    public void onProgress(long bytesRead, long contentLength, boolean done) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> {
                            animeDowns.get(0).setProgress(50);
                            adapter = new AnimeDownloadAdapter(animeDowns);
                            animeDownloadRecycler.setAdapter(adapter);
                        });
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String server) {
        }
    }

}