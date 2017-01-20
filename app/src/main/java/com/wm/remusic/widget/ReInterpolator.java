package com.wm.remusic.widget;

import android.util.Log;
import android.view.animation.Interpolator;

public class ReInterpolator implements Interpolator {
    float current;
    float lastValue;
    boolean isrunning = true;

    public void pause() {
        isrunning = false;
    }

    public void start() {
        isrunning = true;
    }

    public void reset() {
        current = 0.0F;
        lastValue = 0.0F;
        isrunning = true;
    }

    public void clear() {
        current = 0.0F;
        lastValue = 0.0F;
        isrunning = false;
    }
    public boolean isRunning(){
        return isrunning;
    }

    public float getInterpolation(float paramFloat) {
       // Log.e("inter","float" + paramFloat);
        if (isrunning) {
            current = (paramFloat - lastValue);
          //  Log.e("inter","current" + current);
            return current;
        }

        lastValue = (paramFloat - current);
      //  Log.e("inter","lastvalue = " + lastValue);
        return current;
    }
}
