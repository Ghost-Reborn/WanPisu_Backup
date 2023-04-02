package in.ghostreborn.wanpisu.parser;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import in.ghostreborn.wanpisu.constants.WanPisuConstants;
import in.ghostreborn.wanpisu.fragments.KitsuFragment;
import in.ghostreborn.wanpisu.model.Kitsu;
import in.ghostreborn.wanpisu.model.KitsuDetails;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class KitsuAPI {

    public static final String KITSU_API_BASE = "https://kitsu.io/api/edge/users/";
    public static final String KITSU_API_TAIL = "/library-entries?include=anime&page%5Blimit%5D=10&page%5Boffset%5D=0";
    private static final String TAG = "KitsuApi";
    private static final String AUTH_ENDPOINT = "https://kitsu.io/api/oauth/token";

    public static ArrayList<String> login(String username, String password) {
        ArrayList<String> loginData = new ArrayList<>();
        try {
            // Create HTTP client and request
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/vnd.api+json");
            RequestBody body = RequestBody.create(mediaType,
                    "{\"grant_type\":\"password\",\"username\":\"" + username +
                            "\",\"password\":\"" + password + "\"}");
            Request request = new Request.Builder()
                    .url(AUTH_ENDPOINT)
                    .post(body)
                    .addHeader("Content-Type", "application/vnd.api+json")
                    .addHeader("Accept", "application/vnd.api+json")
                    .build();

            // Send request and get response
            Response response = client.newCall(request).execute();
            String json = response.body().string();

            // Parse JSON response
            JSONObject jsonObject = new JSONObject(json);
            loginData.add(jsonObject.getString("access_token"));
        } catch (IOException e) {
            Log.e(TAG, "Error making API request", e);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            loginData.add(getUserID(loginData.get(0)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return loginData;
    }

    public static String getUserID(String TOKEN) throws Exception {
        String url = "https://kitsu.io/api/edge/users?filter[self]=true";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + TOKEN)
                .build();
        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        JSONObject responseObject = new JSONObject(response.body().string());
        JSONArray dataArray = responseObject.getJSONArray("data");
        return ((JSONObject) dataArray.get(0))
                .getString("id");
    }

    public static ArrayList<Kitsu> getUserAnimeList(String TOKEN, String URL) throws Exception {
        Request request = new Request.Builder()
                .url(URL)
                .addHeader("Authorization", "Bearer " + TOKEN)
                .build();
        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        JSONObject responseObject = new JSONObject(responseBody);
        JSONArray included = responseObject
                .getJSONArray("included");
        for (int i = 0; i < included.length(); i++) {
            JSONObject includedObject = included.getJSONObject(i);
            JSONObject attributes = includedObject
                    .getJSONObject("attributes");
            String animeID = includedObject.getString("id");
            String anime = attributes.getString("canonicalTitle");
            String thumbnail;
            thumbnail = attributes.getJSONObject("posterImage")
                    .getString("medium");
            WanPisuConstants.kitsus.add(new Kitsu(animeID, anime, thumbnail));
        }

        JSONObject links = responseObject.getJSONObject("links");
        if (links.has("next")) {
            KitsuFragment.hasNext = true;
            KitsuFragment.nextURL = links.getString("next");
        } else {
            KitsuFragment.hasNext = false;
        }

        return WanPisuConstants.kitsus;
    }

    public static ArrayList<KitsuDetails> getAnimeDetails(String animeID) {

        OkHttpClient client = new OkHttpClient();
        WanPisuConstants.kitsuDetails = new ArrayList<>();

        String endpoint = "https://kitsu.io/api/edge/anime/" + animeID;

        Request request = new Request.Builder()
                .url(endpoint)
                .header("Content-Type", "application/json")
                .header("Accept", "application/vnd.api+json")
                .build();

        Log.e("ANIME_ID", animeID);

        Response response = null;

        try {
            response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String responseBody = response.body().string();

                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONObject attributes = jsonResponse.getJSONObject("data")
                        .getJSONObject("attributes");
                String animeTitle = attributes.getString("canonicalTitle");
                String thumbnail = attributes.getJSONObject("posterImage")
                        .getString("medium");
                String description = attributes.getString("description");
                String averageRating = attributes.getString("averageRating");
                String episodes = attributes.getString("episodeCount");
                WanPisuConstants.kitsuDetails.add(new KitsuDetails(
                        animeTitle,
                        thumbnail,
                        description,
                        averageRating,
                        episodes
                ));
            } else {
                Log.e("Error", "Unexpected response: " + response);
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return WanPisuConstants.kitsuDetails;

    }

    public static String getAnimeCastings(String TOKEN){
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = HttpUrl.parse("https://kitsu.io/api/edge/")
                .newBuilder()
                .addPathSegment("anime")
                .addPathSegment("20")
                .addQueryParameter("include", "person")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.api+json")
                .addHeader("Content-Type", "application/vnd.api+json")
                .addHeader("Authorization", "Bearer " + TOKEN)
                .build();
        Response response = null;
        String json = null;
        try {
            response = client.newCall(request).execute();
            json = response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return json;

    }

}
