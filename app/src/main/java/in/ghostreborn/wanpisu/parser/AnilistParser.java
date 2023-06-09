package in.ghostreborn.wanpisu.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import in.ghostreborn.wanpisu.constants.WanPisuConstants;
import in.ghostreborn.wanpisu.model.Anilist;

public class AnilistParser {

    public static final String LOG_TAG = "WAN_PISU";

    // Token URLs
    public static final String CLIENT_ID = "11820";
    public static final String ANILIST_BASE_URL = "https://anilist.co/";
    public static final String ANILIST_TOKEN_URL = ANILIST_BASE_URL + "api/v2/oauth/authorize?client_id=" + CLIENT_ID + "&response_type=token";

    // Query API URLs
    public static final String QUERY_API_BASE = "https://graphql.anilist.co/";

    public static String getAnilistUserDetails(String ACCESS_TOKEN) {
        String QUERY = "query { " +
                "Viewer {" +
                "name " +
                "} " +
                "} ";

        try {
            URL url = new URL(QUERY_API_BASE);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);

            JSONObject requestBody = new JSONObject();
            requestBody.put("query", QUERY);
            requestBody.put("variables", new JSONObject());

            conn.setDoOutput(true);
            conn.getOutputStream().write(requestBody.toString().getBytes());

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            conn.disconnect();

            return getUserName(response.toString());

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return "";

    }

    private static String getUserName(String baseJSON) {
        try {
            JSONObject viewerObject = new JSONObject(baseJSON)
                    .getJSONObject("data")
                    .getJSONObject("Viewer");

            return viewerObject.getString("name");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static ArrayList<Anilist> getAnimeDetails(String userName, String animeStatus, String ACCESS_TOKEN) {
        ArrayList<Anilist> anilistParsers = new ArrayList<>();
        WanPisuConstants.anilists = new ArrayList<>();
        String QUERY = "query{" +
                "MediaListCollection(userName: \"" + userName + "\", type: ANIME, status: " + animeStatus + "){" +
                "lists{" +
                "entries{" +
                "media{" +
                "id " +
                "idMal " +
                "title{" +
                "english" +
                "}" +
                "coverImage {" +
                "extraLarge " +
                "}" +
                "}" +
                "}" +
                "}" +
                "}" +
                "}";

        try {
            URL url = new URL(QUERY_API_BASE);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);

            JSONObject requestBody = new JSONObject();
            requestBody.put("query", QUERY);
            requestBody.put("variables", new JSONObject());

            conn.setDoOutput(true);
            conn.getOutputStream().write(requestBody.toString().getBytes());

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            conn.disconnect();

            try {
                JSONObject baseJSON = new JSONObject(response.toString());
                JSONArray listArray = baseJSON
                        .getJSONObject("data")
                        .getJSONObject("MediaListCollection")
                        .getJSONArray("lists");
                if (listArray.length() == 0){return anilistParsers;}
                JSONArray entriesArray = listArray.getJSONObject(0)
                        .getJSONArray("entries");
                for (int i = 0; i < entriesArray.length(); i++) {
                    JSONObject mediaObject = entriesArray
                            .getJSONObject(i)
                            .getJSONObject("media");
                    String id = mediaObject.getString("id");
                    String idMal = mediaObject.getString("idMal");
                    String title = mediaObject.getJSONObject("title")
                            .getString("english");
                    String imageUrl = mediaObject.getJSONObject("coverImage")
                            .getString("extraLarge");
                    anilistParsers.add(new Anilist(title, imageUrl, idMal, id));
                    WanPisuConstants.anilists.add(new Anilist(title, imageUrl, idMal, id));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return anilistParsers;

    }

}
