package ru.driics.playm8.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.Display;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.HashMap;

import ru.driics.playm8.MyApplication;
import ru.driics.playm8.core.utils.Utils;
import ru.driics.playm8.core.utils.ViewUtils;

public class GameParticles {

    private static String[] names = new String[]{
            "CS:GO", "Dota 2", "Squad", "Arma 3",
            "Sea of Thieves", "War Thunder", "PAYDAY 2", "Raft"
    };

    public static class Drawable {
        Display display = ((WindowManager) MyApplication.applicationContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point displaySize = new Point();

        private TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        private float bitmapScale = 1;
        private HashMap<String, Bitmap> bitmaps = new HashMap<>();

        public RectF rect = new RectF();
        public RectF screenRect = new RectF();
        public boolean paused;
        private Paint paint = new Paint();

        ArrayList<Drawable.Particle> particles = new ArrayList<>();
        public float speedScale = 1;

        public final int count;
        public boolean useGradient;
        public int size1 = 14, size2 = 12, size3 = 10;
        public long minLifeTime = 2000;
        private int lastColor;
        private final float dt = 1000 / display.getRefreshRate();

        public Drawable(int count) {
            this.count = count;
            textPaint.setColor(Color.WHITE);

            //TODO: calculate bitmap scale
            bitmapScale = .5f;

            display.getSize(displaySize);

            textPaint.setTextSize(ViewUtils.INSTANCE.getToPx(24 * bitmapScale));
            paint.setColor(Color.WHITE);
        }

        public void init() {
            if (particles.isEmpty()) {
                for (int i = 0; i < count; i++) {
                    particles.add(new Drawable.Particle());
                }
            }
        }

        public void resetPositions() {
            long time = System.currentTimeMillis();

            for (int i = 0; i < particles.size(); i++) {
                particles.get(i).genPosition(time, i, true);
            }
        }

        public void onDraw(Canvas canvas) {
            long time = System.currentTimeMillis();

            for (int i = 0; i < particles.size(); i++) {
                Particle particle = particles.get(i);

                if (paused) {
                    particle.draw(canvas, i, pausedTime);
                } else {
                    particle.draw(canvas, i, time);
                }

                if (particle.inProgress >= 1) {
                    particle.genPosition(time, i, false);
                }
            }
        }

        public void onDraw() {
            Canvas canvas = new Canvas();
            long time = System.currentTimeMillis();

            for (int i = 0; i < particles.size(); i++) {
                Particle particle = particles.get(i);

                if (paused) {
                    particle.draw(canvas, i, pausedTime);
                } else {
                    particle.draw(canvas, i, time);
                }

                if (particle.inProgress >= 1) {
                    particle.genPosition(time, i, false);
                }
            }
        }

        public void recycle() {
            for (Bitmap bitmap : bitmaps.values()) {
                bitmap.recycle();
            }

            bitmaps.clear();
        }

        long pausedTime;

        private class Particle {
            private boolean set;
            private float x, y;
            private float vecX, vecY;
            private int alpha;
            private StaticLayout staticLayout;
            private Bitmap bitmap;
            private int l, w, h;
            private long duration;
            private float scale;
            float inProgress;

            public void draw(Canvas canvas, int index, long time) {
                if (!paused) {
                    float speed = ViewUtils.INSTANCE.getToPx(4) * (dt / 660f) * speedScale;

                    if (inProgress != 1) {
                        inProgress += dt / duration;

                        if (inProgress > 1) {
                            inProgress = 1;
                        }
                    }
                }

                if (bitmap != null) {
                    canvas.save();

                    float t = (float) (1f - 4f * Math.pow(inProgress - .5f, 2));
                    float s = scale / bitmapScale * (.7f + .4f * t);

                    canvas.translate(x - w / 2f, y - h / 2f);
                    canvas.scale(s, s, w / 2f, h / 2f);

                    paint.setAlpha((int) (alpha * t));

                    canvas.drawBitmap(bitmap, 0, 0, paint);
                    canvas.restore();
                }
            }

            public void genPosition(long time, int index, boolean reset) {
                duration = 2250 + Math.abs(Utils.fastRandom.nextLong() % 2250);
                scale = .6f + .45f * Math.abs(Utils.fastRandom.nextFloat());

                String name = names[Math.abs(Utils.fastRandom.nextInt() % names.length)];
                if (name.length() > 7) {
                    scale *= .6f;
                } else if (name.length() > 5) {
                    scale *= .75f;
                }

                staticLayout = new StaticLayout(name, textPaint, displaySize.x, Layout.Alignment.ALIGN_NORMAL, 1f, 0, false);
                if (staticLayout.getLineCount() <= 0) {
                    w = h = l = 0;
                } else {
                    l = (int) staticLayout.getLineLeft(0);
                    w = (int) staticLayout.getLineWidth(0);
                    h = staticLayout.getHeight();
                }

                bitmap = bitmaps.get(name);
                if (bitmap == null) {
                    bitmap = Bitmap.createBitmap(Math.max(1, w - Math.max(0, l)), Math.max(1, h), Bitmap.Config.ARGB_8888);

                    Canvas canvas = new Canvas(bitmap);
                    canvas.translate(-l, 0);

                    staticLayout.draw(canvas);

                    bitmaps.put(name, bitmap);
                }

                float bestDistance = 0;
                float minX = rect.left + w / 4f, maxX = rect.right - w / 4f;
                if (index % 2 == 0) {
                    maxX = rect.centerX() - w / 2f;
                } else {
                    minX = rect.centerX() + w / 2f;
                }

                float bestX = minX + Math.abs(Utils.fastRandom.nextInt() % (maxX - minX));
                float bestY = rect.top + Math.abs(Utils.fastRandom.nextInt() % rect.height());
                for (int k = 0; k < 10; k++) {
                    float randX = minX + Math.abs(Utils.fastRandom.nextInt() % (maxX - minX));
                    float randY = rect.top + Math.abs(Utils.fastRandom.nextInt() % rect.height());
                    float minDistance = Integer.MAX_VALUE;
                    for (int j = 0; j < particles.size(); j++) {
                        Particle p = particles.get(j);
                        if (!p.set) {
                            continue;
                        }
                        float rx = Math.min(Math.abs(p.x + p.w * (scale / bitmapScale) * 1.1f - randX), Math.abs(p.x - randX));
                        float ry = p.y - randY;
                        float distance = rx * rx + ry * ry;
                        if (distance < minDistance) {
                            minDistance = distance;
                        }
                    }
                    if (minDistance > bestDistance) {
                        bestDistance = minDistance;
                        bestX = randX;
                        bestY = randY;
                    }
                }
                x = bestX;
                y = bestY;

                double a = Math.atan2(x - rect.centerX(), y - rect.centerY());
                vecX = (float) Math.sin(a);
                vecY = (float) Math.cos(a);
                alpha = (int) (255 * ((50 + Utils.fastRandom.nextInt(50)) / 100f));

                inProgress = reset ? Math.abs((Utils.fastRandom.nextFloat() % 1f) * .9f) : 0;
                set = true;
            }
        }
    }
}
