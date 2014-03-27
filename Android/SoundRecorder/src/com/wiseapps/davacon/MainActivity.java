package com.wiseapps.davacon;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.wiseapps.davacon.core.CheapWAV;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.utils.FileUtils;
import com.wiseapps.davacon.utils.FontUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.wiseapps.davacon.ActivityNavigator.*;
import static com.wiseapps.davacon.core.CheapWAV.*;
import static com.wiseapps.davacon.utils.FileUtils.*;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/19/14
 *         Time: 4:49 PM
 */
public class MainActivity extends PlayingCapableActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_PROCESS_TRACK = 0;

    private MenuItem menuEdit;

    // TODO generalize for further usage (with other file types)
    private List<CheapWAV> wavs;

    private ImageButton buttonPlay;
    private Button buttonClear;

    private CheapWAV tmp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        initData();
        initWidgets();
    }

    @Override
    protected void onDestroy() {
        File[] tracks = FileUtils.getRoot(this).listFiles();

        if (tracks != null) {
            for (File track : tracks) {
                if (track.getName().contains(TMP_SUFFIX)) {
                    boolean result = track.delete();
                    LoggerFactory.obtainLogger(TAG).
                            d(String.format("onDestroy# File %s %s",
                                    track.getAbsolutePath(), result ? "deleted successully" : "deletion failed"));
                }
            }
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case REQUEST_CODE_PROCESS_TRACK: {
                if (resultCode == Activity.RESULT_OK) {
                    initData();
                    initTracks();
                }

                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        menuEdit = menu.findItem(R.id.edit);
        if (wavs == null) {
            if (menuEdit != null) {
                menuEdit.setVisible(false);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit: {
                // TODO implement
                return true;
            }
            case R.id.add: {
                ActivityNavigator.startProcessTrackActivityForResult(this, REQUEST_CODE_PROCESS_TRACK);
                return true;
            }
        }

        return false;
    }

    private void initData() {
        File root = FileUtils.getRoot(this);

        File[] tracks = root.listFiles();
        if (tracks != null && tracks.length == 0) {
            return;
        }

        this.wavs = new ArrayList<CheapWAV>();
        for (File track : tracks) {
            if (track.getName().contains(TMP_SUFFIX)) {
                continue;
            }

            this.wavs.add(new CheapWAV(track,
                    RECORDER_AUDIO_FORMAT, RECORDER_CHANNEL_CONFIG, RECORDER_SAMPLE_RATE_IN_HZ));
        }
    }

    private void initWidgets() {
        buttonPlay = (ImageButton) findViewById(R.id.button_play);

        buttonClear = (Button) findViewById(R.id.button_clear);
        buttonClear.setTypeface(FontUtils.getRobotoRegular(this));

        initTracks();
    }

    private void initTracks() {
        LoggerFactory.obtainLogger(TAG).d("initTracks# started");

        if (wavs == null) {
            if (menuEdit != null) {
                menuEdit.setVisible(false);
            }

            findViewById(R.id.tracks).setVisibility(View.GONE);

            buttonPlay.setImageDrawable(getResources().
                    getDrawable(R.drawable.ic_action_play));
            buttonPlay.setVisibility(View.GONE);

            buttonClear.setVisibility(View.GONE);

            return;
        }

        LayoutInflater inflater = getLayoutInflater();

        LinearLayout tracks = (LinearLayout) findViewById(R.id.tracks);
        tracks.removeAllViews();

        View convertView;

        int count = 0;
        for (final CheapWAV wav : wavs) {
            convertView = inflater.inflate(R.layout.track, null);

            ((TextView) convertView.findViewById(R.id.track)).
                    setText(wav.file.getName());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onDetails(wav);
                }
            });

            ((LinearLayout) findViewById(R.id.tracks)).addView(convertView);

            if (count < wavs.size()) {
                tracks.addView(inflater.inflate(R.layout.separator, null));
            }

            ++count;
        }

        if (menuEdit != null) {
            menuEdit.setVisible(true);
        }

        tracks.setVisibility(View.VISIBLE);

        buttonPlay.setImageDrawable(getResources().
                getDrawable(R.drawable.ic_action_play));
        buttonPlay.setVisibility(View.VISIBLE);

        buttonClear.setVisibility(View.VISIBLE);

        LoggerFactory.obtainLogger(TAG).d("initTracks# finished");
    }

    public void clearAll(View view) {
        File root = FileUtils.getRoot(this);
        File[] tracks = root.listFiles();

        int count = 0;

        if (tracks != null && tracks.length != 0) {
            for (File track : tracks) {
                boolean result = track.delete();
                LoggerFactory.obtainLogger(TAG).
                        d(String.format("clearAll# File %s %s",
                                track.getAbsolutePath(), result ? "deleted successully" : "deletion failed"));

                if (result) {
                    ++count;
                }
            }

            if (count == tracks.length) {
                ((LinearLayout) findViewById(R.id.tracks)).removeAllViews();
                findViewById(R.id.tracks).setVisibility(View.GONE);

                buttonClear.setVisibility(View.GONE);
                buttonPlay.setVisibility(View.GONE);
            }
        }

        wavs = null;
    }

    public void playAll(View view) {
        new PlayAllTask().execute();
    }

    @Override
    void onPlayerPreparedSuccessfully(int duration) {
        buttonPlay.setImageDrawable(getResources().
                getDrawable(R.drawable.ic_action_play));
    }

    @Override
    void onPlayerPreparationFailed() {
        buttonPlay.setImageDrawable(getResources().
                getDrawable(R.drawable.ic_action_play));

        Toast.makeText(this,
                getResources().getString(R.string.prompt_player_preparation_failed), Toast.LENGTH_SHORT).show();
    }

    @Override
    void onPlayerStarted() {
        super.onPlayerStarted();

        buttonPlay.setImageDrawable(getResources().
                getDrawable(R.drawable.ic_action_pause));
    }

    @Override
    void onPlayerInProgress(int currentPosition, int duration) {
    }

    @Override
    void onPlayerPaused() {
        super.onPlayerPaused();

        buttonPlay.setImageDrawable(getResources().
                getDrawable(R.drawable.ic_action_play));
    }

    @Override
    CheapWAV getWav() {
        return tmp;
    }

    public void onDetails(CheapWAV wav) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_TRACK, wav);

        ActivityNavigator.startProcessTrackActivityForResult(this, REQUEST_CODE_PROCESS_TRACK, bundle);
    }

    public void onDelete(CheapWAV wav) {
        // TODO implement
    }

    private class PlayAllTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                tmp = CheapWAV.concat(
                        MainActivity.this, wavs);
                LoggerFactory.obtainLogger(TAG).
                        d(String.format("doInBackground# Tmp file %s created successfully", tmp.file.getAbsolutePath()));

                return true;
            } catch (Exception e) {
                LoggerFactory.obtainLogger(TAG).
                        e("Tmp file creation failed", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (!result) {
                Toast.makeText(MainActivity.this,
                        getResources().getString(R.string.prompt_concat_failed), Toast.LENGTH_SHORT).show();
                return;
            }

            doPlay();
        }
    }
}
