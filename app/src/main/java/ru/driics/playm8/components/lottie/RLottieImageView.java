package ru.driics.playm8.components.lottie;

import android.content.Context;

import java.util.HashMap;

import ru.driics.playm8.utils.ViewUtils;

public class RLottieImageView extends androidx.appcompat.widget.AppCompatImageView {
    private HashMap<String, Integer> layerColors;
    private RLottieDrawable drawable;
    private boolean autoRepeat;
    private boolean attachedToWindow;
    private boolean playing;
    private boolean startOnAttach;
    private Integer layerNum;
    private boolean onlyLastFrame;
    public boolean cached;
    private boolean reverse;

    public RLottieImageView(Context context) {
        super(context);
    }

    public void clearLayerColors() {
        layerColors.clear();
    }

    public void setLayerNum(Integer layerNum) {
        this.layerNum = layerNum;
    }

    public void setLayerColor(String layer, int color) {
        if (layerColors == null) {
            layerColors = new HashMap<>();
        }
        layerColors.put(layer, color);
        if (drawable != null) {
            drawable.setLayerColor(layer, color);
        }
    }

    public void replaceColors(int[] colors) {
        if (drawable != null) {
            drawable.replaceColors(colors);
        }
    }

    public void setAnimation(int resId, int w, int h) {
        setAnimation(resId, w, h, null);
    }

    public void setAnimation(int resId, int w, int h, int[] colorReplacement) {
        setAnimation(new RLottieDrawable(resId, "" + resId, (int) ViewUtils.INSTANCE.getToPx(w), (int) ViewUtils.INSTANCE.getToPx(h), false, colorReplacement));
    }

    public void setOnAnimationEndListener(Runnable r) {
        if (drawable != null) {
            drawable.setOnAnimationEndListener(r);
        }
    }

    public void setAnimation(RLottieDrawable lottieDrawable) {
        if (drawable == lottieDrawable) {
            return;
        }
        drawable = lottieDrawable;
        drawable.setMasterParent(this);
        if (autoRepeat) {
            drawable.setAutoRepeat(1);
        }
        if (layerColors != null) {
            drawable.beginApplyLayerColors();
            for (HashMap.Entry<String, Integer> entry : layerColors.entrySet()) {
                drawable.setLayerColor(entry.getKey(), entry.getValue());
            }
            drawable.commitApplyLayerColors();
        }
        drawable.setAllowDecodeSingleFrame(true);
        setImageDrawable(drawable);
    }

    public void setOnlyLastFrame(boolean onlyLastFrame) {
        this.onlyLastFrame = onlyLastFrame;
    }

    public void setReverse() {
        if (drawable != null) {
            drawable.setPlayInDirectionOfCustomEndFrame(true);
            drawable.setCurrentFrame(drawable.getFramesCount());
            drawable.setCustomEndFrame(0);
        }
    }

    protected void onLoaded() {

    }

    public void clearAnimationDrawable() {
        if (drawable != null) {
            drawable.stop();
        }
        drawable = null;
        setImageDrawable(null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
        if (drawable != null) {
            drawable.setCallback(this);
            if (playing) {
                drawable.start();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachedToWindow = false;
        if (drawable != null) {
            drawable.stop();
        }
    }

    public boolean isPlaying() {
        return drawable != null && drawable.isRunning();
    }

    public void setAutoRepeat(boolean repeat) {
        autoRepeat = repeat;
    }

    public void setProgress(float progress) {
        if (drawable != null) {
            drawable.setProgress(progress);
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        drawable = null;
    }

    public void playAnimation() {
        if (drawable == null) {
            return;
        }
        playing = true;
        if (attachedToWindow) {
            drawable.start();
        } else {
            startOnAttach = true;
        }
    }

    public void stopAnimation() {
        if (drawable == null) {
            return;
        }
        playing = false;
        if (attachedToWindow) {
            drawable.stop();
        } else {
            startOnAttach = false;
        }
    }

    public RLottieDrawable getAnimatedDrawable() {
        return drawable;
    }
}
