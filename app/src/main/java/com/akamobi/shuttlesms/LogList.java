package com.akamobi.shuttlesms;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
//import com.example.android.apis.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

public class LogList extends Activity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
       
        /*
        Shuttle shuttle 		= new Shuttle(this.getApplicationContext());
        Shuttle.Action action	= shuttle.Process("+8613601369912", "短信门神#阿布");
        String replyMessage 	= shuttle.getReplyMessage();
		*/
        
    }
    
    private static class SampleView extends View {
        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float mRotate;
        private Matrix mMatrix = new Matrix();
        private Shader mShader;
        private boolean mDoTiming;

        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            setFocusableInTouchMode(true);

            float x = 160;
            float y = 100;
            mShader = new SweepGradient(x, y, new int[] { Color.GREEN,
                                                  Color.RED,
                                                  Color.BLUE,
                                                  Color.GREEN }, null);
            mPaint.setShader(mShader);
        }
        
        @Override protected void onDraw(Canvas canvas) {
            Paint paint = mPaint;
            float x = 160;
            float y = 100;

            canvas.drawColor(Color.WHITE);

            mMatrix.setRotate(mRotate, x, y);
            mShader.setLocalMatrix(mMatrix);
            mRotate += 3;
            if (mRotate >= 360) {
                mRotate = 0;
            }
            invalidate();

            if (mDoTiming) {
                long now = System.currentTimeMillis();
                for (int i = 0; i < 20; i++) {
                    canvas.drawCircle(x, y, 80, paint);
                }
                now = System.currentTimeMillis() - now;
                android.util.Log.d("skia", "sweep ms = " + (now/20.));
            }
            else {
                canvas.drawCircle(x, y, 80, paint);
            }
        }

        @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_D:
                    mPaint.setDither(!mPaint.isDither());
                    invalidate();
                    return true;
                case KeyEvent.KEYCODE_T:
                    mDoTiming = !mDoTiming;
                    invalidate();
                    return true;
            }
            return super.onKeyDown(keyCode, event);
        }
    }
}
 