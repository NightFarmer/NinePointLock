package com.nightfarmer.ninepointlock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by zhangfan on 16-8-5.
 */
public class NinePointClockView extends View {

    private float lineWidth = 20;//连线宽度
//    private float circleRadius = 100f;//圆形半径
    private float circleRadius = 20;//圆形半径
//    private float strokeWidth = 4f;//圆形边框宽度
    private float strokeWidth = 0;//圆形边框宽度
    private float actDotRadius = 40;//触点显示半径
    private float actRadius = 50;//吸附半径
    private int padding = 200;//内边界

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
        paintLine.setStrokeWidth(lineWidth);
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        paintCircleStroke.setStrokeWidth(strokeWidth);
    }

    private Paint paintLine;
    private Paint paintCircleStroke;
    private Paint paintCircle;
    private Paint paintDot;

    private Point lastTouchPoint;


    private ArrayList<Point> lockPoints = new ArrayList<>();

    private LinkedList<Point> pointLink = new LinkedList<Point>();

    public NinePointClockView(Context context) {
        this(context, null);
    }

    public NinePointClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NinePointClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        paintLine = new Paint();
        paintLine.setColor(Color.parseColor("#ff0000"));

        paintLine.setAntiAlias(true);//抗锯齿
        paintLine.setDither(true);//防抖动
        paintLine.setColor(Color.BLUE);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeJoin(Paint.Join.ROUND);
        paintLine.setStrokeWidth(lineWidth);

        paintCircleStroke = new Paint();
        paintCircleStroke.setColor(Color.parseColor("#ff0000"));
        paintCircleStroke.setAntiAlias(true);//抗锯齿
        paintCircleStroke.setDither(true);//防抖动
        paintCircleStroke.setColor(Color.BLUE);
        paintCircleStroke.setStyle(Paint.Style.FILL_AND_STROKE);
        paintCircleStroke.setStrokeJoin(Paint.Join.ROUND);
        paintCircleStroke.setStrokeWidth(strokeWidth);

        paintCircle = new Paint();
        paintCircle.setColor(Color.parseColor("#ff0000"));
        paintCircle.setAntiAlias(true);//抗锯齿
        paintCircle.setDither(true);//防抖动
        paintCircle.setColor(Color.WHITE);
        paintCircle.setStyle(Paint.Style.FILL);


        paintDot = new Paint();
        paintDot.setColor(Color.parseColor("#ff0000"));
        paintDot.setAntiAlias(true);//抗锯齿
        paintDot.setDither(true);//防抖动
        paintDot.setColor(Color.BLUE);
        paintDot.setStyle(Paint.Style.FILL);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Point point = pointLink.peekLast();
        if (point != null) {
            Path path = new Path();
            path.moveTo(point.x, point.y);

            for (int i = pointLink.size() - 2; i >= 0; i--) {
                Point point1 = pointLink.get(i);
                path.lineTo(point1.x, point1.y);
            }
            if (lastTouchPoint != null) {
                path.lineTo(lastTouchPoint.x, lastTouchPoint.y);
            }
            canvas.drawPath(path, paintLine);
        }

        for (Point lockPoint : lockPoints) {
            canvas.drawCircle(lockPoint.x, lockPoint.y, circleRadius, paintCircleStroke);
            canvas.drawCircle(lockPoint.x, lockPoint.y, circleRadius, paintCircle);
        }

        for (Point lockPoint : pointLink) {
            canvas.drawCircle(lockPoint.x, lockPoint.y, actDotRadius, paintDot);
        }
        if (lastTouchPoint != null) {
            canvas.drawCircle(lastTouchPoint.x, lastTouchPoint.y, actDotRadius, paintDot);
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Point touchedPoint = getTouchedLockPoint((int) event.getX(), (int) event.getY());
                if (touchedPoint == null) break;
                pointLink.clear();
                pointLink.push(touchedPoint);
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                if (!pointLink.isEmpty()) {
                    lastTouchPoint = new Point((int) event.getX(), (int) event.getY());
                }
                touchedPoint = getTouchedLockPoint((int) event.getX(), (int) event.getY());
                if (touchedPoint != null && !pointLink.contains(touchedPoint)) {
                    pointLink.push(touchedPoint);
                }
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                lastTouchPoint = null;
                pointLink.clear();
                invalidate();
                break;
        }
        return true;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //次控件一般不会使用wrapcontent来布局，所以不再复写onMeasure方法
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        initLockPoints(width, height);
    }

    private Point getTouchedLockPoint(int x, int y) {
        for (Point lockPoint : lockPoints) {
            int dx = lockPoint.x - x;
            int dy = lockPoint.y - y;
            if ((dx * dx + dy * dy) < actRadius * actRadius) {
                return lockPoint;
            }
        }
        return null;
    }

    private void initLockPoints(int width, int height) {
        int centerX = width / 2;
        int centerY = height / 2;
        int pointDistance = Math.min(width, height) / 2 - padding;
        lockPoints.clear();//防止重复布局造成数据重复
        lockPoints.add(new Point(centerX - pointDistance, centerY - pointDistance));
        lockPoints.add(new Point(centerX, centerY - pointDistance));
        lockPoints.add(new Point(centerX + pointDistance, centerY - pointDistance));

        lockPoints.add(new Point(centerX - pointDistance, centerY));
        lockPoints.add(new Point(centerX, centerY));
        lockPoints.add(new Point(centerX + pointDistance, centerY));

        lockPoints.add(new Point(centerX - pointDistance, centerY + pointDistance));
        lockPoints.add(new Point(centerX, centerY + pointDistance));
        lockPoints.add(new Point(centerX + pointDistance, centerY + pointDistance));

    }


    public float getLineWidth() {
        return lineWidth;
    }

    public float getCircleRadius() {
        return circleRadius;
    }

    public void setCircleRadius(float circleRadius) {
        this.circleRadius = circleRadius;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public float getActDotRadius() {
        return actDotRadius;
    }

    public void setActDotRadius(float actDotRadius) {
        this.actDotRadius = actDotRadius;
    }

    public float getActRadius() {
        return actRadius;
    }

    public void setActRadius(float actRadius) {
        this.actRadius = actRadius;
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }
}
