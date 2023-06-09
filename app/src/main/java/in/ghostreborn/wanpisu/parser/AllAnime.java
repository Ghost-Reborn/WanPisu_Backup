package in.ghostreborn.wanpisu.parser;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import in.ghostreborn.wanpisu.MainActivity;
import in.ghostreborn.wanpisu.adapter.AnimeSearchAdapter;
import in.ghostreborn.wanpisu.constants.WanPisuConstants;
import in.ghostreborn.wanpisu.model.Anilist;
import in.ghostreborn.wanpisu.model.WanPisu;

public class AllAnime {

    public static final String ALL_ANIME_SERVER_HEAD = "https://api.allanime.to/allanimeapi?variables={%22showId%22:%22";
    public static final String ALL_ANIME_SERVER_TAIL = "%22}&query=query($showId:String!,$translationType:VaildTranslationTypeEnumType!,$episodeString:String!){episode(showId:$showId,translationType:$translationType,episodeString:$episodeString){episodeString,sourceUrls}}";
    public static final String ALL_ANIME_BLOG_HEAD = "https://blog.allanime.pro/apivtwo/clock.json?";
    public static boolean isHLS = false;
    static boolean isDubEnabled = WanPisuConstants.preferences.getBoolean(WanPisuConstants.WAN_PISU_PREFERENCE_ENABLE_DUB, false);
    public static String ALL_ANIME_QUERY_TAIL = "\"},\"limit\":40,\"page\":1,\"translationType\":\"" +
            (isDubEnabled ? "dub" : "sub") +
            "\",\"countryOrigin\":\"ALL\"}&query=query($search:SearchInput,$limit:Int,$page:Int,$translationType:VaildTranslationTypeEnumType,$countryOrigin:VaildCountryOriginEnumType){shows(search:$search,limit:$limit,page:$page,translationType:$translationType,countryOrigin:$countryOrigin){edges{_id,name,thumbnail,availableEpisodes,malId,englishName}}}";
    public static final String ALL_ANIME_SERVER_MIDDLE = "%22,%22translationType%22:%22" +
            (isDubEnabled ? "dub" : "sub") +
            "%22,%22episodeString%22:%22";
    static boolean isUnknownEnabled = WanPisuConstants.preferences.getBoolean(WanPisuConstants.WAN_PISU_PREFERENCE_ENABLE_UNKNOWN, false);
    public static final String ALL_ANIME_QUERY_HEAD = "https://api.allanime.to/allanimeapi?variables={\"search\":{\"allowAdult\":" +
            true +
            ",\"allowUnknown\":" +
            isUnknownEnabled +
            ",\"query\":\"";

    private static String connectAndGetJsonSearchData(String url) {

        StringBuilder result = new StringBuilder();
        try {
            URL queryURL = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) queryURL.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            urlConnection.disconnect();
        } catch (MalformedURLException e) {
            Log.e("TAG", "Unable to parse URL");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    /**
     * JSON array with `edges` provides anime details
     *
     * @return ArrayList<WanPisu>
     */
    public static ArrayList<WanPisu> parseAnimeIDAnimeNameAnimeThumbnail(String anime) {

        String rawJson = connectAndGetJsonSearchData(
                ALL_ANIME_QUERY_HEAD + anime + ALL_ANIME_QUERY_TAIL
        );

        WanPisuConstants.wanPisus = new ArrayList<>();
        try {
            JSONArray edgesArray = new JSONObject(rawJson)
                    .getJSONObject("data")
                    .getJSONObject("shows")
                    .getJSONArray("edges");
            for (int i = 0; i < edgesArray.length(); i++) {
                JSONObject edges = edgesArray.getJSONObject(i);
                String malID = edges.getString("malId");
                if (malID.equals("null")) continue;
                String animeID = edges.getString("_id");
                String animeName = edges.getString("name");
                String animeEnglishName = edges.getString("englishName");
                if (!animeEnglishName.equals("null")) {
                    animeName = animeEnglishName;
                }
                String animeThumbnailUrl = edges.getString("thumbnail");
                String lastEpisode = edges.getJSONObject("availableEpisodes")
                        .getString("sub");
                String url = "";
                if (animeThumbnailUrl.contains("__Show__")) {
                    if (animeThumbnailUrl.contains("images"))
                        url = "https://wp.youtube-anime.com/aln.youtube-anime.com/";
                    else {
                        url = "https://wp.youtube-anime.com/aln.youtube-anime.com/images/";
                    }
                }
                WanPisuConstants.wanPisus.add(new WanPisu(
                        animeID,
                        animeName,
                        url + animeThumbnailUrl,
                        Integer.parseInt(lastEpisode),
                        malID
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return WanPisuConstants.wanPisus;

    }

    public static ArrayList<String> getAnimeServer(String animeID, String episodeNumber) {
        String apiUrl = ALL_ANIME_SERVER_HEAD + animeID + ALL_ANIME_SERVER_MIDDLE + episodeNumber + ALL_ANIME_SERVER_TAIL;
        ArrayList<String> servers = new ArrayList<>();
        String apiClock = "";
        try {
            JSONObject baseJSON = new JSONObject(connectAndGetJsonSearchData(apiUrl));
            JSONArray sourceURLs = baseJSON.
                    getJSONObject("data")
                    .getJSONObject("episode")
                    .getJSONArray("sourceUrls");
            for (int i = 0; i < sourceURLs.length(); i++) {
                String server = sourceURLs.getJSONObject(i).getString("sourceUrl");
                if (server.contains("apivtwo")) {
                    apiClock = server;
                    continue;
                }
                servers.add(server);
            }
            apiUrl = ALL_ANIME_BLOG_HEAD + apiClock.substring(15);
            Log.e("ALLANIME", apiUrl);
            baseJSON = new JSONObject(connectAndGetJsonSearchData(apiUrl));
            JSONArray links = baseJSON.getJSONArray("links");
            for (int i = 0; i < links.length(); i++) {
                JSONObject linkObject = links.getJSONObject(i);
                String server = linkObject.getString("link");
                if (linkObject.has("mp4")) {
                    isHLS = !linkObject.getBoolean("mp4");
                }
                servers.add(server);
            }
            return servers;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return servers;

    }

    public static ArrayList<WanPisu> getUsersAnime(Context context) {
        WanPisuConstants.wanPisus = new ArrayList<>();
        String TOKEN = WanPisuConstants.preferences.getString(WanPisuConstants.WAN_PISU_ANILIST_TOKEN, "");
        ArrayList<Anilist> anilists = AnilistParser
                .getAnimeDetails(
                        AnilistParser.getAnilistUserDetails(TOKEN),
                        WanPisuConstants.ANIME_CURRENT,
                        TOKEN
                );
        String allAnimeAnimeID = "";
        String totalEpisodes = "0";
        for (int i = 0; i < anilists.size(); i++) {
            Anilist anilist = anilists.get(i);
            String baseJSONText = connectAndGetJsonSearchData(
                    ALL_ANIME_QUERY_HEAD
                            + anilist.getAnimeName()
                            + ALL_ANIME_QUERY_TAIL
            );
            try {
                JSONArray edgesArray = new JSONObject(baseJSONText)
                        .getJSONObject("data")
                        .getJSONObject("shows")
                        .getJSONArray("edges");

                String anilistMalID = anilists.get(i).getMalID().trim();
                for (int j = 0; j < edgesArray.length(); j++) {
                    JSONObject edges = edgesArray.getJSONObject(j);
                    String malID = edges.getString("malId").trim();
                    if (malID.equals(anilistMalID)){
                        allAnimeAnimeID = edges.getString("_id");
                        JSONObject availableEpisodes = edges.getJSONObject("availableEpisodes");
                        totalEpisodes = availableEpisodes.getString("sub");
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            WanPisuConstants.wanPisus.add(new WanPisu(
                    allAnimeAnimeID,
                    anilist.getAnimeName(),
                    anilist.getAnimeImageUrl(),
                    Integer.parseInt(totalEpisodes),
                    anilists.get(i).getMalID()
            ));
        }

        return WanPisuConstants.wanPisus;
    }
}
