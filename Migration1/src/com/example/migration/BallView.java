package com.example.migration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

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