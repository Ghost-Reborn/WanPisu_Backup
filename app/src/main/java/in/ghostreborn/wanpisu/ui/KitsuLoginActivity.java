package in.ghostreborn.wanpisu.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import in.ghostreborn.wanpisu.R;
import in.ghostreborn.wanpisu.parser.Kitsu;

public class KitsuLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitsu_login);

        EditText kitsuUserNameTextView = findViewById(R.id.kitsu_user_name_text_view);
        EditText kitsuUserPassTextView = findViewById(R.id.kitsu_user_pass_text_view);
        Button kitsuLoginButton = findViewById(R.id.kitsu_login_button);
        Button kitsuSignUpButton = findViewById(R.id.kitsu_signup_button);
        kitsuLoginButton.setOnClickListener(view -> {
            String USERNAME = kitsuUserNameTextView.getText().toString().trim();
            String PASSWORD = kitsuUserPassTextView.getText().toString().trim();
            new KitsuLoginTask(
                    USERNAME,
                    PASSWORD
            ).execute();
        });
        kitsuSignUpButton.setOnClickListener(view -> {
            startActivity(
                    new Intent(Intent.ACTION_VIEW)
                            .setData(Uri.parse("https://kitsu.io/explore/anime")
                            )
            );
        });

    }

    private class KitsuLoginTask extends AsyncTask<Void, Void, String> {

        String USERNAME, PASSWORD;

        public KitsuLoginTask(String USERNAME, String PASSWORD) {
            this.USERNAME = USERNAME;
            this.PASSWORD = PASSWORD;
        }

        @Override
        protected String doInBackground(Void... voids) {
            return Kitsu.login(USERNAME, PASSWORD);
        }

        @Override
        protected void onPostExecute(String accessToken) {
            if (accessToken != null) {
                Toast.makeText(KitsuLoginActivity.this, accessToken, Toast.LENGTH_SHORT).show();
                // Store the access token or use it to make authenticated API requests
            } else {
                Toast.makeText(KitsuLoginActivity.this, "failed to login", Toast.LENGTH_SHORT).show();
            }
        }
    }


}