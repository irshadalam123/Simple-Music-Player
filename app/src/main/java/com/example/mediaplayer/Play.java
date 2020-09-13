package com.example.mediaplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;


public class Play extends AppCompatActivity implements MediaPlayer.OnCompletionListener{

    Button btn_prev, btn_next, btn_pause;
    TextView songLevel;
    ImageView backButton;
    SeekBar seekbar, sound_seekBar;

    TextView startDuration, lastDuration;

    String song_name;

    static MediaPlayer myMediaPlayer;
    private AudioManager audioManager;
    int position;
    ArrayList<File> mySong;

    int minutesLast = 0;
    int secondsLast = 0;
    int minutesStart = 0;
    int secondsStart = 0;

    Uri uri;

    private Thread playThread, prevThread, nextThread;

    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        btn_prev = findViewById(R.id.prev_button);
        btn_next = findViewById(R.id.next_button);
        btn_pause = findViewById(R.id.play_button);

        backButton = findViewById(R.id.back_main);

        songLevel = findViewById(R.id.textView1);
        seekbar = findViewById(R.id.seekbar);
        sound_seekBar = findViewById(R.id.song_seekBar);

        startDuration = findViewById(R.id.start_duration);
        lastDuration = findViewById(R.id.last_duration);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        sound_seekBar.setMax(maxVol);
        sound_seekBar.setProgress(curVol);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), MainActivity.class));
                overridePendingTransition(R.anim.slid_in_left, R.anim.slid_out_right);
            }
        });

        sound_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        startMusic();

        myMediaPlayer.setOnCompletionListener(this);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    if (myMediaPlayer != null) {
                        myMediaPlayer.seekTo(progress*1000);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        Play.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (myMediaPlayer != null){
                    seekbar.setMax(myMediaPlayer.getDuration()/1000);
                    int current_position = myMediaPlayer.getCurrentPosition()/1000;
                    seekbar.setProgress(current_position);

                    minutesStart = (myMediaPlayer.getCurrentPosition()/1000)/60;
                    secondsStart = (myMediaPlayer.getCurrentPosition()/1000)%60;

                    startDuration.setText(String.valueOf(minutesStart+":"+secondsStart));

                    minutesLast = (myMediaPlayer.getDuration()/1000)/60;
                    secondsLast = (myMediaPlayer.getDuration()/1000)%60;

                    lastDuration.setText(String.valueOf(minutesLast+":"+secondsLast));
                }

                handler.postDelayed(this, 1000);

            }


        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slid_in_left, R.anim.slid_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();

        playThreadButton();
        prevThreadButton();
        nextThreadButton();
    }

    private void nextThreadButton() {
        nextThread = new Thread(){
            @Override
            public void run() {
                super.run();

                btn_next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playNext();
                    }
                });

            }
        };
        nextThread.start();
    }

    private void prevThreadButton() {
        prevThread = new Thread(){
            @Override
            public void run() {
                super.run();

                btn_prev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPrev();

                    }
                });

            }
        };
        prevThread.start();
    }

    private void playThreadButton() {

        playThread = new Thread(){
            @Override
            public void run() {
                super.run();

                btn_pause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        play_pause();
                    }
                });

            }
        };
        playThread.start();
    }

    private void play_pause() {
        if (myMediaPlayer.isPlaying()) {
            btn_pause.setBackgroundResource(R.drawable.stop);
            myMediaPlayer.pause();
            seekbar.setMax(myMediaPlayer.getDuration()/1000);

            Play.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (myMediaPlayer != null){
                        int current_position = myMediaPlayer.getCurrentPosition()/1000;
                        seekbar.setProgress(current_position);
                    }
                    handler.postDelayed(this, 1000);
                }


            });

        } else {
            btn_pause.setBackgroundResource(R.drawable.play);
            myMediaPlayer.start();
            seekbar.setMax(myMediaPlayer.getDuration()/1000);

            Play.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (myMediaPlayer != null){
                        int current_position = myMediaPlayer.getCurrentPosition()/1000;
                        seekbar.setProgress(current_position);
                    }
                    handler.postDelayed(this, 1000);
                }


            });
        }
    }

    private void playNext() {
        if (myMediaPlayer.isPlaying()) {
            myMediaPlayer.stop();
            myMediaPlayer.release();
            position = ((position + 1) % mySong.size());

            uri = Uri.parse(mySong.get(position).toString());

            myMediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            song_name = mySong.get(position).getName().toString();
            songLevel.setText(song_name);
            seekbar.setMax(myMediaPlayer.getDuration()/1000);
            Play.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (myMediaPlayer != null) {
                        int current_position = myMediaPlayer.getCurrentPosition() / 1000;
                        seekbar.setProgress(current_position);
                    }
                    handler.postDelayed(this, 1000);
                }


            });

            myMediaPlayer.setOnCompletionListener(this);
            btn_pause.setBackgroundResource(R.drawable.play);
            myMediaPlayer.start();
        }
        else {
            myMediaPlayer.stop();
            myMediaPlayer.release();
            position = ((position + 1) % mySong.size());

            uri = Uri.parse(mySong.get(position).toString());

            myMediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            song_name = mySong.get(position).getName().toString();
            songLevel.setText(song_name);
            seekbar.setMax(myMediaPlayer.getDuration()/1000);
            Play.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (myMediaPlayer != null) {
                        int current_position = myMediaPlayer.getCurrentPosition() / 1000;
                        seekbar.setProgress(current_position);
                    }
                    handler.postDelayed(this, 1000);
                }


            });

            myMediaPlayer.setOnCompletionListener(this);
            btn_pause.setBackgroundResource(R.drawable.play);
        }

    }

    private void playPrev() {
        if(myMediaPlayer != null) {
            myMediaPlayer.pause();
            myMediaPlayer.stop();
            myMediaPlayer.release();
            position = ((position - 1) < 0) ? (mySong.size() - 1) : (position - 1);

            uri = Uri.parse(mySong.get(position).toString());

            myMediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            song_name = mySong.get(position).getName().toString();
            songLevel.setText(song_name);
            seekbar.setMax(myMediaPlayer.getDuration()/1000);
            Play.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (myMediaPlayer != null) {
                        int current_position = myMediaPlayer.getCurrentPosition() / 1000;
                        seekbar.setProgress(current_position);
                    }
                    handler.postDelayed(this, 1000);
                }


            });

            myMediaPlayer.setOnCompletionListener(this);
            btn_pause.setBackgroundResource(R.drawable.play);
            myMediaPlayer.start();
        }
        else {
            myMediaPlayer.pause();
            myMediaPlayer.stop();
            myMediaPlayer.release();
            position = ((position - 1) < 0) ? (mySong.size() - 1) : (position - 1);

            uri = Uri.parse(mySong.get(position).toString());

            myMediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            song_name = mySong.get(position).getName().toString();
            songLevel.setText(song_name);
            seekbar.setMax(myMediaPlayer.getDuration()/1000);
            Play.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (myMediaPlayer != null) {
                        int current_position = myMediaPlayer.getCurrentPosition() / 1000;
                        seekbar.setProgress(current_position);
                    }
                    handler.postDelayed(this, 1000);
                }


            });

            myMediaPlayer.setOnCompletionListener(this);
            btn_pause.setBackgroundResource(R.drawable.play);
        }
    }


    private void startMusic() {

        if (myMediaPlayer != null) {
            myMediaPlayer.stop();
            myMediaPlayer.release();
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mySong = (ArrayList) bundle.getParcelableArrayList("song");

        position = bundle.getInt("position", 0);
        uri = Uri.parse(mySong.get(position).toString());

        song_name = mySong.get(position).getName().toString();
        songLevel.setText(song_name);
        songLevel.setSelected(true);

        myMediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        btn_pause.setBackgroundResource(R.drawable.play);
        myMediaPlayer.start();

        seekbar.setMax(myMediaPlayer.getDuration()/1000);

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNext();
        if (myMediaPlayer != null){
            myMediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            myMediaPlayer.start();
            myMediaPlayer.setOnCompletionListener(this);
        }
    }


}
