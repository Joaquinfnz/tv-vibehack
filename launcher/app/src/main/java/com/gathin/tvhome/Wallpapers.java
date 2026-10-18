package com.gathin.tvhome;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Calendar;

/** The bundled wallpapers, shared by the home screen, the picker and the screensaver. */
final class Wallpapers {

    static final int BLACK = 0;
    /** Aurora by day, Midnight at night. */
    static final int AUTO = -1;

    /** Order matters: the saved preference is an index into this list. */
    static final int[] RES = {R.drawable.wallpaper, R.drawable.wp_medianoche,
            R.drawable.wp_atardecer, R.drawable.wp_bosque, BLACK, AUTO,
            R.drawable.wp_seda, R.drawable.wp_andes, R.drawable.wp_cristal, R.drawable.wp_grafito,
            R.drawable.wp_costa, R.drawable.wp_niebla,
            R.drawable.wp_paine_amanecer, R.drawable.wp_paine_lago, R.drawable.wp_paine_nieve};
    static final int[] NAMES = {R.string.bg_aurora, R.string.bg_midnight,
            R.string.bg_sunset, R.string.bg_forest, R.string.bg_black, R.string.bg_auto,
            R.string.bg_silk, R.string.bg_andes, R.string.bg_crystal, R.string.bg_graphite,
            R.string.bg_coast, R.string.bg_mist,
            R.string.bg_paine_dawn, R.string.bg_paine_lake, R.string.bg_paine_snow};

    /** Landscapes for the screensaver: they read well with slow movement and a clock on top. */
    static final int[] SAVER = {R.drawable.wp_paine_amanecer, R.drawable.wp_paine_lago,
            R.drawable.wp_paine_nieve, R.drawable.wp_andes, R.drawable.wp_costa, R.drawable.wp_niebla};

    private Wallpapers() {
    }

    static int clampIndex(int i) {
        return Math.max(0, Math.min(i, RES.length - 1));
    }

    /** Drawable to show for a preference index right now: a resource id, or {@link #BLACK}. */
    static int resolve(int index) {
        int res = RES[clampIndex(index)];
        if (res != AUTO) return res;
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour >= 7 && hour < 19 ? R.drawable.wallpaper : R.drawable.wp_medianoche;
    }

    /** Decodes a wallpaper at 1/sample of its size; sample 8 gives a 240 px wide thumbnail. */
    static Bitmap decode(Resources r, int res, int sample) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inSampleSize = sample;
        return BitmapFactory.decodeResource(r, res, o);
    }
}
