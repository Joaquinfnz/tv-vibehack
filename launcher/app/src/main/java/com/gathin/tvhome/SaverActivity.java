package com.gathin.tvhome;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

/**
 * Aerial-style screensaver: landscapes drifting slowly with cross-fades, a clock that moves now and then
 * so nothing stays burnt into the panel. Any key or touch closes it.
 */
public class SaverActivity extends Activity {

    private static final long SLIDE_MS = 22_000L;
    private static final long FADE_MS = 2_400L;
    private static final long CLOCK_MOVE_MS = 90_000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private ImageView front, back;
    private View clockBlock;
    private int index;
    private float density;

    private final Runnable nextSlide = new Runnable() {
        @Override
        public void run() {
            index = (index + 1) % Wallpapers.SAVER.length;
            final int res = Wallpapers.SAVER[index];
            new Thread(() -> {
                Bitmap bmp = Wallpapers.decode(getResources(), res, 1);
                handler.post(() -> crossFadeTo(bmp));
            }).start();
            handler.postDelayed(this, SLIDE_MS);
        }
    };

    private final Runnable moveClock = new Runnable() {
        @Override
        public void run() {
            clockBlock.animate().alpha(0f).setDuration(900).withEndAction(() -> {
                placeClock();
                clockBlock.animate().alpha(1f).setDuration(1200).start();
            }).start();
            handler.postDelayed(this, CLOCK_MOVE_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saver);
        density = getResources().getDisplayMetrics().density;
        front = findViewById(R.id.slideA);
        back = findViewById(R.id.slideB);
        clockBlock = findViewById(R.id.saverClock);
        String wx = getSharedPreferences("tvhome", MODE_PRIVATE).getBoolean("weather_on", true)
                ? getSharedPreferences("tvhome", MODE_PRIVATE).getString("wx_text", null) : null;
        TextView weather = findViewById(R.id.saverWeather);
        if (wx != null) weather.setText(wx);
        else weather.setVisibility(View.GONE);

        index = random.nextInt(Wallpapers.SAVER.length);
        front.setImageResource(Wallpapers.SAVER[index]);
        drift(front);
        clockBlock.setAlpha(0f);
        clockBlock.post(() -> {
            placeClock();
            clockBlock.animate().alpha(1f).setDuration(1500).start();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(nextSlide, SLIDE_MS);
        handler.postDelayed(moveClock, CLOCK_MOVE_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
        finish();   // never linger behind another app
    }

    /** Slow zoom and pan in a random direction, the length of one slide plus its fade. */
    private void drift(ImageView v) {
        v.animate().cancel();
        float dx = (random.nextFloat() - 0.5f) * 60f * density;
        float dy = (random.nextFloat() - 0.5f) * 30f * density;
        v.setScaleX(1.04f);
        v.setScaleY(1.04f);
        v.setTranslationX(0f);
        v.setTranslationY(0f);
        v.animate().scaleX(1.14f).scaleY(1.14f).translationX(dx).translationY(dy)
                .setDuration(SLIDE_MS + FADE_MS).setInterpolator(new LinearInterpolator()).start();
    }

    private void crossFadeTo(Bitmap bmp) {
        if (bmp == null || isFinishing()) return;
        back.setImageBitmap(bmp);
        back.setAlpha(0f);
        back.bringToFront();
        clockBlock.bringToFront();
        drift(back);
        final ImageView old = front;
        back.animate().alpha(1f).setDuration(FADE_MS).withEndAction(() -> {
            old.animate().cancel();
            old.setImageDrawable(null);   // free the previous bitmap
        }).start();
        front = back;
        back = old;
    }

    /** Somewhere in the lower or upper third, away from the edges. */
    private void placeClock() {
        View root = (View) clockBlock.getParent();
        float margin = 64 * density;
        float maxX = Math.max(margin, root.getWidth() - clockBlock.getWidth() - margin);
        boolean low = random.nextBoolean();
        float y = low ? root.getHeight() - clockBlock.getHeight() - margin : margin;
        clockBlock.setX(margin + random.nextFloat() * (maxX - margin));
        clockBlock.setY(y);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) finish();
        return true;   // swallow the key that wakes it, as tvOS does
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        finish();
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }
}
