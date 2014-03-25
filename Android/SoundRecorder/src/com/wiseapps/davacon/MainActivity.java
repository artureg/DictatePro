package com.wiseapps.davacon;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import com.wiseapps.davacon.utils.FontUtils;

import java.util.List;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/19/14
 *         Time: 4:49 PM
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private List<Object> tracks;

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
    }

    private void initWidgets() {
        listTracks = (ListView) findViewById(R.id.tracks);

        buttonPlay = (ImageButton) findViewById(R.id.button_play);

        buttonClear = (Button) findViewById(R.id.button_clear);
        buttonClear.setTypeface(FontUtils.getRobotoRegular(this));

        if (tracks != null) {
            buttonPlay.setVisibility(View.VISIBLE);
            buttonClear.setVisibility(View.VISIBLE);
        }
    }

    public void play(View view) {
    }
}
