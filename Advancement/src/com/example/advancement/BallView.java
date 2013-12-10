package com.example.advancement;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/*
public class BallView extends View {

	public float x;
    public float y;
    public final int radius;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    //construct new ball object
    public BallView(Context context, float x, float y, int radius) {
        super(context);
        mPaint.setColor(0x88FFFFFF); //color hex is [transparency][red][green][blue]
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
    
    //called by invalidate()	
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(x, y, radius, mPaint);
    } 
}
*/

public class BallView extends View {

    public float x;
    public float y;
    public final int radius;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap bm;
    
    //construct new ball object
    public BallView(Context context, float x, float y, int radius) {
        super(context);
        mPaint.setColor(0xFFFFFFFF); //color hex is [transparency][red][green][blue]
        this.x = x;
        this.y = y;
        this.radius = radius;
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.chromeball);
    }
    
    //called by invalidate()    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawCircle(x, y, radius, mPaint);
        canvas.drawBitmap(bm, x-radius-2, y-radius-2, mPaint);
    } 
}