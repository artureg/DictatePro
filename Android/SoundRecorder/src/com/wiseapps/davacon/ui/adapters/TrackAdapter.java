package com.wiseapps.davacon.ui.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import com.wiseapps.davacon.R;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/20/14
 *         Time: 9:28 AM
 *
 * TODO use real track object type instead of java.lang.Object
 */
public class TrackAdapter extends ArrayAdapter<Object> {

    public TrackAdapter(Context context) {
        super(context, R.layout.process_track);
    }
}
