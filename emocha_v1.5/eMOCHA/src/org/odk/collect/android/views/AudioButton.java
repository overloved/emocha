/**
 * 
 */

package org.odk.collect.android.views;

import org.emocha.midot.R;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Watch media with media player. 
 * @author ctsims
 * @author carlhartung
 */
public class AudioButton extends ImageButton implements OnClickListener, OnCompletionListener {
    private final static String t = "AudioButton";
    private String URI;
    private MediaPlayer player;


    /**
     * Class constructor.
     * @param context
     * @param URI Media file URI.
     */
    public AudioButton(Context context, String URI) {
        super(context);
        this.setOnClickListener(this);
        this.URI = URI;
        Bitmap b =
            BitmapFactory.decodeResource(context.getResources(),
                android.R.drawable.ic_lock_silent_mode_off);
        this.setImageBitmap(b);
        this.setMinimumWidth(b.getScaledWidth(context.getResources().getDisplayMetrics()));
        player = new MediaPlayer();
    }


    public void onClick(View v) {
        if (URI == null) {
            // No audio file specified
            Log.e(t, "No audio file was specified");
            Toast.makeText(getContext(), getContext().getString(R.string.audio_file_error),
                Toast.LENGTH_LONG).show();
            return;
        }

        String audioFilename = "";
        try {
            audioFilename = ReferenceManager._().DeriveReference(URI).getLocalURI();
        } catch (InvalidReferenceException e) {
            Log.e(t, "Invalid reference exception");
            e.printStackTrace();
        }

        File audioFile = new File(audioFilename);
        if (!audioFile.exists()) {
            // We should have an audio clip, but the file doesn't exist.
            String errorMsg = getContext().getString(R.string.audio_file_missing, audioFile);
            Log.e(t, errorMsg);
            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
            return;
        }

        try {
            synchronized (player) {
                initPlayer();
                player.setDataSource(audioFilename);
                player.prepare();
                player.setOnCompletionListener(this);
                player.start();
            }
        } catch (IOException e) {
            String errorMsg = getContext().getString(R.string.audio_file_invalid);
            Log.e(t, errorMsg);
            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    /**
     * Initialize media player
     */
    private void initPlayer() {
        if (player.isPlaying()) {
            player.stop();
            player.release();
            player = new MediaPlayer();
        }
    }


    /* (non-Javadoc)
     * @see android.media.MediaPlayer.OnCompletionListener#onCompletion(android.media.MediaPlayer)
     */
    public void onCompletion(MediaPlayer mp) {
        synchronized (mp) {
            mp.release();
            player = new MediaPlayer();
        }
    }

}
