package com.tyler.miniaudio;
/**
 * Song adapter class for Recycler View
 */

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongHolder> {

    private ArrayList<SongInfo> _songs;
    private Context context;
    private OnItemClickListener mOnItemClickListener;

    public SongAdapter(Context context, ArrayList<SongInfo> songs) {
        this.context = context;
        this._songs = songs;
    }

    public interface OnItemClickListener {
        void onItemClick(/*Button b ,*/Context mcontext, View view, SongInfo obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    @Override
    public SongHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View myView = LayoutInflater.from(context).inflate(R.layout.row_songs,viewGroup,false);
        return new SongHolder(myView);
    }

    @Override
    public void onBindViewHolder(final SongHolder songHolder, final int i) {
        final SongInfo s = _songs.get(i);
        final Context xx = songHolder.mContext;

        songHolder.tvSongName.setText(_songs.get(i).getSongname());
        songHolder.tvSongArtist.setText(_songs.get(i).getArtistname());


        if(FloatingViewService.themeNumber == 0)
        {
            songHolder.tvSongName.setTextColor(Color.BLACK);
            songHolder.tvSongArtist.setTextColor(Color.BLACK);
        }
        else
        {
            songHolder.tvSongName.setTextColor(Color.WHITE);
            songHolder.tvSongArtist.setTextColor(Color.WHITE);
        }

        songHolder.row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(xx,/*songHolder.btnAction,*/v, s, i);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return _songs.size();
    }

    public class SongHolder extends RecyclerView.ViewHolder {
        TextView tvSongName,tvSongArtist;
        private final Context mContext;

        LinearLayout row;
        public SongHolder(View itemView) {
            super(itemView);
            this.mContext = itemView.getContext();

            row =  itemView.findViewById(R.id.rowSongTemplate);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvSongArtist =  itemView.findViewById(R.id.tvArtistName);
//            btnAction = (Button) itemView.findViewById(R.id.btnPlay);
        }
    }
}
