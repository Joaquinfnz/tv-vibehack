package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * A clear glass slab in the iOS 26 manner: almost no fill, a soft drop shadow, light caught on the
 * top-left and bottom-right rim only, and a faint sheen that fades out before the middle.
 * One instance floats over the grid and is moved with spring animations between tiles.
 */
public class GlassView extends View {

    private final Paint shadow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sheen = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint inner = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final RectF innerRect = new RectF();
    private final Path clip = new Path();
    private final float density;
    private float radius;

    public GlassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        radius = 20f * density;
        shadow.setColor(0xFF000000);
        rim.setStyle(Paint.Style.STROKE);
        rim.setStrokeWidth(1.2f * density);
        inner.setStyle(Paint.Style.STROKE);
        inner.setStrokeWidth(1f * density);
    }

    void setRadius(float px) {
        radius = px;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        fill.setShader(new LinearGradient(0, 0, 0, h, 0x24FFFFFF, 0x0DFFFFFF, Shader.TileMode.CLAMP));
        sheen.setShader(new LinearGradient(0, 0, 0, h * 0.5f, 0x2EFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
        // Light from the top-left: bright there, bright again opposite, dark between.
        rim.setShader(new LinearGradient(0, 0, w, h,
                new int[]{0xE6FFFFFF, 0x1AFFFFFF, 0x1AFFFFFF, 0x8CFFFFFF},
                new float[]{0f, 0.38f, 0.62f, 1f}, Shader.TileMode.CLAMP));
        inner.setShader(new LinearGradient(0, 0, w, h,
                new int[]{0x40FFFFFF, 0x00FFFFFF, 0x00FFFFFF, 0x26FFFFFF},
                new float[]{0f, 0.4f, 0.6f, 1f}, Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float inset = 12f * density;   // room for the shadow
        rect.set(inset, inset, getWidth() - inset, getHeight() - inset);

        // Soft drop shadow: stacked, widening, very faint fills, nudged down.
        for (int i = 5; i >= 1; i--) {
            float o = i * 2.2f * density;
            shadow.setAlpha(9);
            canvas.drawRoundRect(rect.left - o, rect.top - o + 4f * density,
                    rect.right + o, rect.bottom + o + 4f * density, radius + o, radius + o, shadow);
        }

        canvas.drawRoundRect(rect, radius, radius, fill);

        clip.reset();
        clip.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clip);
        canvas.drawRect(rect.left, rect.top, rect.right, rect.top + rect.height() * 0.5f, sheen);
        canvas.restore();

        float k = 2.2f * density;
        innerRect.set(rect.left + k, rect.top + k, rect.right - k, rect.bottom - k);
        canvas.drawRoundRect(innerRect, radius - k, radius - k, inner);
        canvas.drawRoundRect(rect, radius, radius, rim);
    }
}
