package com.company.my.galaxy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GameView extends View {
    // Context, Thread
    private Context context;
    private GameThread mThread;

    // 화면 크기
    private int w, h;

    // 배경, X-Wing
    private Sky sky;
    private int state = 4;

    // 버튼
    private Button btnDown;
    private Button btnLeft;
    private Button btnRight;
    private Button btnUp;

    //-----------------------------
    // 생성자
    //-----------------------------
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    //-----------------------------
    // View의 크기 구하기
    //-----------------------------
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        this.w = w;  // 화면의 폭과 높이
        this.h = h;

        // 배경, X-Wing
        sky = new Sky(context, w, h);

        // 버튼 만들기
        makeButton();

        // 스레드 기동
        if (mThread == null) {
            mThread = new GameThread();
            mThread.start();
        }
    }

    //-----------------------------
    // View의 종료
    //-----------------------------
    @Override
    protected void onDetachedFromWindow() {
        mThread.canRun = false;
        super.onDetachedFromWindow();
    }

    //-----------------------------
    // 화면 그리기
    //-----------------------------
    @Override
    protected void onDraw(Canvas canvas) {
        sky.draw(canvas);  // 배경

        // 버튼
        canvas.drawBitmap(btnLeft.img, btnLeft.x, btnLeft.y, null);
        canvas.drawBitmap(btnRight.img, btnRight.x, btnRight.y, null);
        canvas.drawBitmap(btnUp.img, btnUp.x, btnUp.y, null);
        canvas.drawBitmap(btnDown.img, btnDown.x, btnDown.y, null);
    }

    //-----------------------------
    // 버튼 만들기
    //-----------------------------
    private void makeButton() {
        // 버튼 이미지
        Bitmap imgLeft = BitmapFactory.decodeResource(getResources(), R.drawable.button_left);
        Bitmap imgRight = BitmapFactory.decodeResource(getResources(), R.drawable.button_right);
        Bitmap imgUp = BitmapFactory.decodeResource(getResources(), R.drawable.button_up);
        Bitmap imgDown = BitmapFactory.decodeResource(getResources(), R.drawable.button_down);

        // 버튼의 크기
        int bw = imgLeft.getWidth();
        int bh = imgLeft.getHeight();

        // 버튼의 위치
        int y = (h - bh)/2;                      // 화면 세로 중앙
        int x = (w - bw)/2;                      // 화면 가로 중앙

        Point lPos = new Point(10, y);            // 왼쪽
        Point rPos = new Point(w - bw - 10, y);       // 오른쪽

        Point uPos = new Point(x, 10);   // 위
        Point dPos = new Point(x, h - bh - 10);   //  아래

        // 버튼 만들기
        btnLeft = new Button(imgLeft, lPos);
        btnRight = new Button(imgRight, rPos);
        btnUp = new Button(imgUp, uPos);
        btnDown = new Button(imgDown, dPos);
    }

    //-----------------------------
    // 이동
    //-----------------------------
    private void moveObject() {
        sky.update();
    }

    //-----------------------------
    // Touch Event
    //-----------------------------
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isTouch = false;

        int action = MotionEventCompat.getActionMasked(event);
        // int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                isTouch = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                isTouch = false;
                break;
            default:
                return true;
        }

        // 터치 index, id
        int ptrIdx = MotionEventCompat.getActionIndex(event);
        int id = MotionEventCompat.getPointerId(event, ptrIdx);

        // 터치 좌표
        float x = MotionEventCompat.getX(event, ptrIdx);
        float y = MotionEventCompat.getY(event, ptrIdx);;

        // 각각의 버튼에 통지
        btnLeft.action(id, isTouch, x, y);
        btnRight.action(id, isTouch, x, y);
        btnUp.action(id, !isTouch, x, y);
        btnDown.action(id, !isTouch, x, y);


        if(btnUp.isTouch)
            state = 0;
        if(btnDown.isTouch)
            state = 1;
        if(btnLeft.isTouch)
            state = 2;
        if(btnRight.isTouch)
            state = 3;

        sky.setState(state);

        return true;
    }

    //-----------------------------
    // Thread
    //-----------------------------
    class GameThread extends Thread {
        public boolean canRun = true;

        @Override
        public void run() {
            while (canRun) {
                try {
                    Time.update();      // deltaTime 계산

                    moveObject();
                    postInvalidate();   // 화면 그리기
                    sleep(10);
                } catch (Exception e) {
                    //
                }
            }
        }
    } // Thread

} // GameView
