package com.example.spotify2;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;


public class MainActivity extends Activity {

    private static final int REQUEST_CODE = 1337;
    private Button buttonPlayPlaylist;
    private TextView textViewPlaylist;
    private ImageView imageViewBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wireWidgets();
        login();

    }

    private void login() {
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(Credentials.CLIENT_ID,
                        AuthenticationResponse.Type.TOKEN, Credentials.REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    private void wireWidgets() {
        buttonPlayPlaylist = findViewById(R.id.button_main_playPlaylist);
        textViewPlaylist = findViewById(R.id.textView_main_playlist);
        imageViewBackground = findViewById(R.id.imageView_main_background);
        buttonPlayPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }

        });
        buttonPlayPlaylist.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Credentials.mSpotifyAppRemote.getPlayerApi().pause();
                return true;
            }

        });
    }

    private void connected() {
        // Then we will write some more code here.
        // Play a playlist
        Credentials.mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");

    }

    private void connect() {
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(Credentials.CLIENT_ID)
                        .setRedirectUri(Credentials.REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        Credentials.mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // And we will finish off here.
        SpotifyAppRemote.disconnect(Credentials.mSpotifyAppRemote);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
                    Toast.makeText(this, "Other Error", Toast.LENGTH_SHORT).show();
            }
        }
    }



}