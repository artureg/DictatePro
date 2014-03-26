package com.wiseapps.davacon;

import android.app.Activity;
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

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/19/14
 *         Time: 4:49 PM
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    // TODO generalize for further usage (with other file types)
    private List<CheapWAV> wavs;

    private ImageButton buttonPlay;
    private Button buttonClear;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        initData(savedInstanceState);
        initWidgets();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        if (wavs == null) {
            MenuItem menuEdit = menu.findItem(R.id.edit);
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
                return true;
            }
            case R.id.add: {
                ActivityNavigator.startProcessTrackActivity(this);
                return true;
            }
        }

        return false;
    }

    private void initData(Bundle savedInstanceState) {
        File root = FileUtils.getRoot(this);

        File[] tracks = root.listFiles();
        if (tracks == null) {
            return;
        }

        this.wavs = new ArrayList<CheapWAV>();
        for (int i = 0; i < tracks.length; i++) {
            this.wavs.add(new CheapWAV(tracks[i],
                    RECORDER_AUDIO_FORMAT, RECORDER_CHANNEL_CONFIG, RECORDER_SAMPLE_RATE_IN_HZ));
        }

//        new ReadTask().execute();
    }

    private void initWidgets() {
        initTracks();

        buttonPlay = (ImageButton) findViewById(R.id.button_play);

        buttonClear = (Button) findViewById(R.id.button_clear);
        buttonClear.setTypeface(FontUtils.getRobotoRegular(this));

        if (wavs != null) {
            buttonPlay.setVisibility(View.VISIBLE);
            buttonClear.setVisibility(View.VISIBLE);
        }
    }

    private void initTracks() {
        LoggerFactory.obtainLogger(TAG).d("initTracks# started");

        LayoutInflater inflater = getLayoutInflater();

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
                ((LinearLayout) findViewById(R.id.tracks)).
                        addView(inflater.inflate(R.layout.separator, null));
            }

            ++count;
        }

        LoggerFactory.obtainLogger(TAG).d("initTracks# finished");
    }

    public void play(View view) {
    }

    public void onDetails(CheapWAV wav) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_TRACK, wav);
//        bundle.putSerializable(EXTRA_TRACK, wavs.get(position).file);

        ActivityNavigator.startProcessTrackActivity(this, bundle);
    }

    public void onDelete(CheapWAV wav) {
    }

//    private class ReadTask extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            for (CheapWAV wav : wavs) {
//                wav.read();
//            }
//
//            return null;
//        }
//    }
}
