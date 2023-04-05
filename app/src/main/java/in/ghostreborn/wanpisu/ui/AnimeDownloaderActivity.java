package in.ghostreborn.wanpisu.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

import in.ghostreborn.wanpisu.R;
import in.ghostreborn.wanpisu.constants.WanPisuConstants;
import in.ghostreborn.wanpisu.manager.WanPisuDownloadManager;
import in.ghostreborn.wanpisu.model.AnimeDown;
import in.ghostreborn.wanpisu.parser.AllAnime;

public class AnimeDownloaderActivity extends AppCompatActivity {

    EditText animeEpisodeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_downloader);

        TextView animeName = findViewById(R.id.anime_download_text_view);
        TextView totalEpisodes = findViewById(R.id.anime_total_text_view);
        animeEpisodeEditText = findViewById(R.id.anime_episode_edit_text);
        animeName.setText(WanPisuConstants.preferences.getString(WanPisuConstants.ALL_ANIME_ANIME_NAME, "0"));
        totalEpisodes.setText(WanPisuConstants.preferences.getString(WanPisuConstants.ALL_ANIME_ANIME_EPISODES, "0"));
        Button animeDownloadButton = findViewById(R.id.anime_download_button);
        animeDownloadButton.setOnClickListener(view -> {
            new AnimeDownloadTask().execute();
        });

    }

    private class AnimeDownloadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
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
                animeName = animeName + " - " + episodes;
                AnimeDown animeDown = new AnimeDown(animeName, episodes, 0);
                WanPisuConstants.animeDowns.add(animeDown);

                Log.e("WAN_PISU_INDEX", WanPisuConstants.animeDowns.indexOf(animeDown) + "");

                String fileNameDestination = WanPisuConstants.wanPisuFolder.getAbsolutePath()
                        + animeName
                        + " - "
                        + episodes
                        + ".mp4";

                downloadManager.download(srvr, fileNameDestination, (bytesRead, contentLength, done) -> {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> {
                        double progress = ((double) bytesRead / contentLength) * 100;
                        WanPisuConstants.animeDowns.get(WanPisuConstants.animeDowns.indexOf(animeDown)).setProgress((int)progress);
                    });
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void server) {
        }
    }

}