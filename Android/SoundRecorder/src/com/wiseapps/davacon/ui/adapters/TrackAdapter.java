package com.wiseapps.davacon.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.wiseapps.davacon.R;
import com.wiseapps.davacon.core.CheapWAV;

import java.util.List;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/20/14
 *         Time: 9:28 AM
 *
 * TODO generalize
 */
public class TrackAdapter extends ArrayAdapter<CheapWAV> {

    private final TrackActionsListener listener;
    private final LayoutInflater inflater;

    public TrackAdapter(Context context, List<CheapWAV> tracks, TrackActionsListener listener) {
        super(context, R.layout.track, tracks);

        this.listener = listener;
        this.inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.track, null);
        }

        CheapWAV wav = getItem(position);

        ((TextView) convertView.findViewById(R.id.track)).
                setText(wav.file.getName());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onDetails(position);
                }
            }
        });

        return convertView;
    }

    public interface TrackActionsListener {
        void onDetails(int position);
        void onDelete(int position);
    }
}
