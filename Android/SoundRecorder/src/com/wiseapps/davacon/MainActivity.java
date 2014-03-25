package com.wiseapps.davacon;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import com.wiseapps.davacon.core.WAVFile;
import com.wiseapps.davacon.core.WAVFileReader;
import com.wiseapps.davacon.ui.adapters.TrackAdapter;
import com.wiseapps.davacon.utils.FileUtils;
import com.wiseapps.davacon.utils.FontUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.wiseapps.davacon.ActivityNavigator.*;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/19/14
 *         Time: 4:49 PM
 */
public class MainActivity extends Activity implements TrackAdapter.TrackActionsListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    // TODO generalize for further usage (with other file types)
    private List<WAVFile> tracks;

    private ListView listTracks;
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

        if (tracks == null) {
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

        this.tracks = new ArrayList<WAVFile>();
        for (int i = 0; i < tracks.length; i++) {
            this.tracks.add(new WAVFileReader(tracks[i]).wav);
        }
    }

    private void initWidgets() {
        listTracks = (ListView) findViewById(R.id.tracks);
        listTracks.setAdapter(new TrackAdapter(this, tracks, this));

        buttonPlay = (ImageButton) findViewById(R.id.button_play);

        buttonClear = (Button) findViewById(R.id.button_clear);
        buttonClear.setTypeface(FontUtils.getRobotoRegular(this));

        if (tracks != null) {
            listTracks.setVisibility(View.VISIBLE);

            buttonPlay.setVisibility(View.VISIBLE);
            buttonClear.setVisibility(View.VISIBLE);
        }
    }

    public void play(View view) {
    }

    @Override
    public void onDetails(int position) {
        Bundle bundle = new Bundle();
//        bundle.putSerializable(EXTRA_TRACK, tracks.get(position));
        bundle.putSerializable(EXTRA_TRACK, tracks.get(position).getFile());

        ActivityNavigator.startProcessTrackActivity(this, bundle);
    }

    @Override
    public void onDelete(int position) {
    }
}
