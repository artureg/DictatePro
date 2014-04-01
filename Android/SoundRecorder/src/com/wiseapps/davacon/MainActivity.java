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
import com.wiseapps.davacon.core.SoundFile;
import com.wiseapps.davacon.core.SoundFileHandler;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.utils.DurationUtils;
import com.wiseapps.davacon.utils.FileUtils;
import com.wiseapps.davacon.utils.FontUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.wiseapps.davacon.ActivityNavigator.*;
import static com.wiseapps.davacon.utils.FileUtils.*;

/**
 * Application main activity.
 *
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/19/14
 *         Time: 4:49 PM
 */
public class MainActivity extends PlayingCapableActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_PROCESS_TRACK = 0;

    private List<SoundFile> sfs;
    private SoundFile tmp;

    private TextView textDuration;

    private ProgressBar progressBar;

    private LinearLayout trackList;

    private ImageButton buttonPlay;
    private Button buttonClear;

    private MenuItem menuEdit;

    /**
     * Mode of the UI - either view or edit.
     * Default is view.
     */
    private static enum Mode {
        VIEW, EDIT
    }
    private Mode mode = Mode.VIEW;

    /**
     * Called when the activity is starting. Here tracks are initialized,
     * UI initialization takes place.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        initData();
        initWidgets();
    }

    /**
     * Performs final cleanup before an activity is destroyed.
     * <p></>Deletes any temporary files if there are any.<p/>
     */
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

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     *
     * In case of <var>requestCode</var> equal to REQUEST_CODE_PROCESS_TRACK (one is returned from the
     * {@link com.wiseapps.davacon.ProcessTrackActivity ProcessTrackActivity}) and
     * <var>resultCode</var> equal to Activity.RESULT_OK, class fields are reinitializaed, UI is updated.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     */
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

    /**
     * Initialize the contents of the screen's options menu.
     * Menu items are placed in to <var>menu</var>.
     *
     * <p>Depending on existence of already recorded sound files the Edit menu shall
     * either be shown (tracks exist) or hidden (no tracks recorded).</p>
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        menuEdit = menu.findItem(R.id.edit);
        updateMenu();

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Called whenever an item in the options menu is selected.
     *
     * <p>In case Edit menu is called the tracks list is updated
     * to provide track deletion functionality.<p/>
     *
     * <p>In case Add menu is called the user id forwarded to the
     * {@link com.wiseapps.davacon.ProcessTrackActivity ProcessTrackActivity}
     * where a track can be recorded.<p/>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     *         proceed, true to consume it here.
     */
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

    /**
     * Helper method to initialize tracks list.
     */
    private void initData() {
        this.sfs = null;

        File root = FileUtils.getRoot(this);

        File[] tracks = root.listFiles();
        if (tracks == null || tracks.length == 0) {
            return;
        }

        this.sfs = new ArrayList<SoundFile>();
        for (File track : tracks) {
            if (track.getName().contains(TMP_SUFFIX)) {
                continue;
            }
            try {
                this.sfs.add(SoundFile.create(track));
            } catch (IOException e) {
                LoggerFactory.obtainLogger(TAG).
                        e(String.format("initData# Couldn't read %s", track.getAbsolutePath()), e);
            }
        }
    }

    /**
     * Helper method to initialize UI.
     */
    private void initWidgets() {
        textDuration = (TextView) findViewById(R.id.duration);

        progressBar = (ProgressBar) findViewById(R.id.progress);

        trackList = (LinearLayout) findViewById(R.id.tracks);

        buttonPlay = (ImageButton) findViewById(R.id.button_play);

        buttonClear = (Button) findViewById(R.id.button_clear);
        buttonClear.setTypeface(FontUtils.getRobotoRegular(this));

        updateWidgets();
    }

    /**
     * Helper method to update UI in case screen's UI mode has been changed.
     */
    private void updateWidgets() {
        updateMenu();
        updateTrackList();
        updateButtons();
    }

    /**
     * Helper method to update menu in case screen's UI mode has been changed.
     */
    private void updateMenu() {
        if (menuEdit == null) {
            return;
        }

        // set icon
        menuEdit.setIcon(mode == Mode.VIEW ? getResources().getDrawable(R.drawable.ic_action_edit) :
                getResources().getDrawable(R.drawable.ic_action_accept));

        // set visibility
        menuEdit.setVisible(sfs != null && !sfs.isEmpty());
    }

    /**
     * Helper method to update tracks list in case screen's UI mode has been changed.
     */
    private void updateTrackList() {
        if (sfs == null || sfs.isEmpty()) {
            textDuration.setVisibility(View.GONE);
            trackList.setVisibility(View.GONE);
            return;
        }

        double overAllDuration = 0;

        LayoutInflater inflater = getLayoutInflater();
        trackList.removeAllViews();

        int count = 0;
        double duration;

        View convertView;
        for (final SoundFile sf : sfs) {
            convertView = inflater.inflate(R.layout.track, null);

            duration = DurationUtils.format(sf.getDuration());
            overAllDuration += duration;

            ((TextView) convertView.findViewById(R.id.track)).
                    setText(String.format(getResources().getString(R.string.track_duration),
                            duration));

            switch(mode) {
                case VIEW: {
                    convertView.findViewById(R.id.action_forward).setVisibility(View.VISIBLE);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onDetails(sf);
                        }
                    });

                    break;
                }
                case EDIT: {
                    convertView.findViewById(R.id.action_remove).setVisibility(View.VISIBLE);
                    convertView.findViewById(R.id.action_remove).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onDelete(sf);
                        }
                    });

                    break;
                }
            }

            ((LinearLayout) findViewById(R.id.tracks)).addView(convertView);

            if (count < sfs.size()) {
                trackList.addView(inflater.inflate(R.layout.separator, null));
            }

            ++count;
        }

        trackList.setVisibility(View.VISIBLE);

        textDuration.setText(String.format(getResources().getString(R.string.main_duration),
                DurationUtils.format(0), overAllDuration));
        textDuration.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method to update buttons in case screen's UI mode has been changed.
     */
    private void updateButtons() {
        if (sfs != null && !sfs.isEmpty()) {
            buttonPlay.setVisibility(View.VISIBLE);
            buttonClear.setVisibility(View.VISIBLE);
        } else {
            buttonPlay.setVisibility(View.GONE);
            buttonClear.setVisibility(View.GONE);
        }
    }

    /**
     * Clears the tracks list.
     *
     * @param view The clicked view.
     */
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

    /**
     * Plays the tracks list.
     *
     * @param view The clicked view.
     */
    public void playAll(View view) {
        new PlayAllTask().execute();
    }

    /**
     * Callback method to update the screens data and UI after the media player has been prepared.
     */
    @Override
    void onPlayerPreparedSuccessfully() {
    }

    /**
     * Callback method to update the screens data and UI in case an error occured
     * during the media player preparation.
     */
    @Override
    void onPlayerPreparationFailed() {
        Toast.makeText(this,
                getResources().getString(R.string.prompt_player_preparation_failed), Toast.LENGTH_SHORT).show();
    }

    /**
     * Callback method to update the screens data and UI after the track playback has started.
     */
    @Override
    void onPlayerStarted() {
        super.onPlayerStarted();

        progressBar.setMax(tmp.getDuration());
        progressBar.setVisibility(View.VISIBLE);

        buttonPlay.setImageDrawable(getResources().
                getDrawable(R.drawable.ic_action_pause));
    }

    /**
     * Callback method to update the screens data and UI after the track playback is in progress.
     */
    @Override
    void onPlayerInProgress(int currentPosition) {
        progressBar.setProgress(currentPosition);

        textDuration.setText(
                String.format(getResources().getString(R.string.process_track_duration),
                        DurationUtils.format(currentPosition), DurationUtils.format(tmp.getDuration())));
    }

    /**
     * Callback method to update the screens data and UI after the track playback has been paused.
     */
    @Override
    void onPlayerPaused() {
        super.onPlayerPaused();

        buttonPlay.setImageDrawable(getResources().
                getDrawable(R.drawable.ic_action_play));
    }

    /**
     * Callback method to update the screens data and UI after the track playback has completed.
     */
    @Override
    void onPlayerCompleted() {
        super.onPlayerCompleted();

        textDuration.setText(
                String.format(getResources().getString(R.string.process_track_duration),
                        DurationUtils.format(0), DurationUtils.format(tmp.getDuration())));

        progressBar.setVisibility(View.GONE);

        buttonPlay.setImageDrawable(getResources().
                getDrawable(R.drawable.ic_action_play));
    }

    /**
     * Callback method to set the track to play.
     */
    @Override
    SoundFile getSoundFile() {
        return tmp;
    }

    /**
     * Shows details of a {@link com.wiseapps.davacon.core.SoundFile SoundFile}.
     *
     * @param sf Sound file to show the details of.
     */
    public void onDetails(SoundFile sf) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_TRACK, sf.getFile());

        ActivityNavigator.startProcessTrackActivityForResult(this, REQUEST_CODE_PROCESS_TRACK, bundle);
    }

    public void onDelete(SoundFile wav) {
        if (wav.getFile().delete()) {
            LoggerFactory.obtainLogger(TAG).
                    d(String.format("onPostExecute# File %s deleted successully",
                            wav.getFile().getAbsolutePath()));

            Toast.makeText(MainActivity.this,
                    getResources().getString(R.string.prompt_file_deleted), Toast.LENGTH_SHORT).show();
        } else {
            LoggerFactory.obtainLogger(TAG).
                    d(String.format("onPostExecute# File %s deletion failed",
                            wav.getFile().getAbsolutePath()));
        }

        mode = Mode.VIEW;

        initData();
        updateWidgets();
    }

    /**
     * Async task to play the tracks in succession.
     */
    private class PlayAllTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                tmp = SoundFileHandler.concat(MainActivity.this, sfs);
                LoggerFactory.obtainLogger(TAG).
                        d(String.format("doInBackground# Tmp file %s created successfully", tmp.getFile().getAbsolutePath()));

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
