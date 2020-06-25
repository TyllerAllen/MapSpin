package com.wlu.cp470.group12.mapspin;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.view.GestureDetectorCompat;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.preference.PreferenceManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class CircleImageView extends AppCompatImageView implements GestureDetector.OnGestureListener {


    public interface OnSpinStartListener{
        CompletableFuture<Float> startSpin();
    }

    public interface OnSpinEndListener{
        void spinEnd();
    }

    private OnSpinStartListener onStartSpinListener;
    private OnSpinEndListener onSpinEndListener;

    private GestureDetectorCompat mDetector;
    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDetector = new GestureDetectorCompat(context,this);
    }


    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDetector = new GestureDetectorCompat(context,this);
    }
    public CircleImageView(Context context) {
        super(context);
        mDetector = new GestureDetectorCompat(context,this);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        double radius = getWidth() / 2.0;
        double dist = Math.sqrt(Math.pow((getWidth()/2.0)-event.getX(),2)+Math.pow((getHeight()/2.0)-event.getY(),2));

        return dist <= radius && dist >= radius * .75 && this.mDetector != null && this.mDetector.onTouchEvent(event);
    }



    public void setOnStartSpinListener(OnSpinStartListener onStartSpinListener){
        this.onStartSpinListener = onStartSpinListener;
    }

    public void setOnSpinEndListener(OnSpinEndListener onSpinEndListener){
        this.onSpinEndListener = onSpinEndListener;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    float lastDegrees = 0f;

    Direction getDirection(float x1, float y1, float x2, float y2){
        double angle = getAngle(x1, y1, x2, y2);
        return Direction.fromAngle(angle);
    }

    double getAngle(float x1, float y1, float x2, float y2) {
        double rad = Math.atan2(y1-y2,x2-x1) + Math.PI;
        return (rad*180/Math.PI + 180)%360;
    }


    private boolean isAnimationEnabled(){
        //TODO: Please add logic here to grab settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("pref_toggle_ani", false)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float x1 = e1.getX();
        float y1 = e1.getY();

        float x2 = e2.getX();
        float y2 = e2.getY();

        Direction direction = getDirection(x1,y1,x2,y2);
        int dir = ((direction == Direction.right && y1 < getHeight()/2f) ||
                (direction == Direction.left && y1 > getHeight()/2f) ||
                (direction == Direction.down && x1 > getWidth()/2f) ||
                (direction == Direction.up && x1 < getWidth()/2f))?1:-1;
        if(onStartSpinListener != null){
            if(isAnimationEnabled()) {
                CompletableFuture<Float> angleFuture = onStartSpinListener.startSpin();
                Animation loop = new RotateAnimation(lastDegrees, lastDegrees + 360 * 4 * dir, getWidth() / 2f, getHeight() / 2f);
                loop.setRepeatCount(Animation.INFINITE);
                loop.setRepeatMode(Animation.RESTART);
                loop.setDuration(2000);
                loop.setFillAfter(true);
                loop.setFillEnabled(true);
                loop.setInterpolator(new LinearInterpolator());
                loop.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        if (angleFuture.isDone()) {
                            animation.cancel();
                            try {
                                Animation a = null;
                                float finalAngle = angleFuture.get();
                                a = new RotateAnimation(lastDegrees, finalAngle + 360 * 4 * dir, getWidth() / 2f, getHeight() / 2f);

                                a.setDuration(2000);
                                a.setFillAfter(true);
                                a.setFillEnabled(true);
                                a.setInterpolator(new LinearInterpolator());
                                a.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        if (onSpinEndListener != null) onSpinEndListener.spinEnd();
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                startAnimation(a);
                                lastDegrees = finalAngle;
                            } catch (ExecutionException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                startAnimation(loop);
            } else {
                CompletableFuture<Float> angleFuture = onStartSpinListener.startSpin();
                angleFuture.thenAccept(angle -> {
                    setRotation(angle);
                    lastDegrees = angle;
                    if (onSpinEndListener != null) onSpinEndListener.spinEnd();
                });
            }
        }
        return true;
    }

    public enum Direction{
        up,
        down,
        left,
        right;

        public static Direction fromAngle(double angle){
            if(inRange(angle, 45, 135)){
                return Direction.up;
            }
            else if(inRange(angle, 0,45) || inRange(angle, 315, 360)){
                return Direction.right;
            }
            else if(inRange(angle, 225, 315)){
                return Direction.down;
            }
            else{
                return Direction.left;
            }

        }

        private static boolean inRange(double angle, float init, float end){
            return (angle >= init) && (angle < end);
        }
    }
}
