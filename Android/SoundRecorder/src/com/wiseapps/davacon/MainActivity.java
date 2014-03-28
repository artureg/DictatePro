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

    // TODO generalize for further usage (with other file types)
    private List<CheapWAV> wavs;
    private CheapWAV tmp;

    private LinearLayout trackList;

    private ImageButton buttonPlay;
    private Button buttonClear;

    private MenuItem menuEdit;

    private static enum Mode {
        VIEW, EDIT
    }
    private Mode mode = Mode.VIEW;

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
                    updateWidgets();
                }

                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        menuEdit = menu.findItem(R.id.edit);
        updateMenu();

//        if (wavs == null) {
//            if (menuEdit != null) {
//                menuEdit.setVisible(false);
//            }
//        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit: {
                mode = Mode.EDIT;

                updateWidgets();

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
        this.wavs = null;

        File root = FileUtils.getRoot(this);

        File[] tracks = root.listFiles();
        if (tracks == null || tracks.length == 0) {
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
        trackList = (LinearLayout) findViewById(R.id.tracks);

        buttonPlay = (ImageButton) findViewById(R.id.button_play);

        buttonClear = (Button) findViewById(R.id.button_clear);
        buttonClear.setTypeface(FontUtils.getRobotoRegular(this));

        updateWidgets();
    }

    private void updateWidgets() {
        updateMenu();
        updateTrackList();
        updateButtons();
    }

    private void updateMenu() {
        if (menuEdit == null) {
            return;
        }

        // set icon
        menuEdit.setIcon(mode == Mode.VIEW ? getResources().getDrawable(R.drawable.ic_action_edit) :
                getResources().getDrawable(R.drawable.ic_action_accept));

        // set visibility
        menuEdit.setVisible(wavs != null && !wavs.isEmpty());
    }

    private void updateTrackList() {
        if (wavs == null || wavs.isEmpty()) {
            trackList.setVisibility(View.GONE);
            return;
        }

        LayoutInflater inflater = getLayoutInflater();
        trackList.removeAllViews();

        int count = 0;

        View convertView;
        for (final CheapWAV wav : wavs) {
            convertView = inflater.inflate(R.layout.track, null);

            ((TextView) convertView.findViewById(R.id.track)).
                    setText(wav.file.getName());

            switch(mode) {
                case VIEW: {
                    convertView.findViewById(R.id.action_forward).setVisibility(View.VISIBLE);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onDetails(wav);
                        }
                    });

                    break;
                }
                case EDIT: {
                    convertView.findViewById(R.id.action_remove).setVisibility(View.VISIBLE);
                    convertView.findViewById(R.id.action_remove).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onDelete(wav);
                        }
                    });

                    break;
                }
            }

            ((LinearLayout) findViewById(R.id.tracks)).addView(convertView);

            if (count < wavs.size()) {
                trackList.addView(inflater.inflate(R.layout.separator, null));
            }

            ++count;
        }

        trackList.setVisibility(View.VISIBLE);
    }

    private void updateButtons() {
        if (wavs != null && !wavs.isEmpty()) {
            buttonPlay.setVisibility(View.VISIBLE);
            buttonClear.setVisibility(View.VISIBLE);
        } else {
            buttonPlay.setVisibility(View.GONE);
            buttonClear.setVisibility(View.GONE);
        }
    }

    public void clearAll(View view) {
        File root = FileUtils.getRoot(this);
        File[] tracks = root.listFiles();

        if (tracks != null && tracks.length != 0) {
            for (File track : tracks) {
                boolean result = track.delete();
                LoggerFactory.obtainLogger(TAG).
                        d(String.format("clearAll# File %s %s",
                                track.getAbsolutePath(), result ? "deleted successully" : "deletion failed"));
            }
        }

        initData();
        updateWidgets();
    }

    public void playAll(View view) {
        new PlayAllTask().execute();
    }

    @Override
    void onPlayerPreparedSuccessfully(int duration) {
    }

    @Override
    void onPlayerPreparationFailed() {
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
    void onPlayerCompleted() {
        super.onPlayerCompleted();

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
        if (wav.file.delete()) {
            LoggerFactory.obtainLogger(TAG).
                    d(String.format("onPostExecute# File %s deleted successully",
                            wav.file.getAbsolutePath()));

            Toast.makeText(MainActivity.this,
                    getResources().getString(R.string.prompt_file_deleted), Toast.LENGTH_SHORT).show();
        } else {
            LoggerFactory.obtainLogger(TAG).
                    d(String.format("onPostExecute# File %s deletion failed",
                            wav.file.getAbsolutePath()));
        }

        mode = Mode.VIEW;

        initData();
        updateWidgets();
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
