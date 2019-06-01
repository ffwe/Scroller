package com.company.my.galaxy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class Sky {
    // 화면의 폭과 높이
    private int w, h;

    // 이미지 수, 이미지 배열, 출력할 이미지 번호
    private int imgCnt = 9;
    private Bitmap[] arSky = new Bitmap[imgCnt];

    private int n = 0; //hash key
    private int nh = 1;
    private int nv = 2;
    private int nd = 3;

    private boolean a = false;
    private boolean b = false;

    // 스크롤 속도, 이미지 오프셋
    private float speed = 200f;
    private float ofsy = 0;
    private float ofsx = 0;
    private int state = 4;

    public Bitmap imgSky;

    //-----------------------------
    // 생성자
    //-----------------------------
    public Sky(Context context, int width, int height) {
        w = width;
        h = height;

        createBitmap(context);
    }

    //-----------------------------
    // update <-- Thread
    //-----------------------------
    public void update() {
        switch (state){
            case 0: //up
                ofsy -= speed * Time.deltaTime;
                break;
            case 1: //down
                ofsy += speed * Time.deltaTime;
                break;
            case 2: //left
                ofsx -= speed * Time.deltaTime;
                break;
            case 3: //right
                ofsx += speed * Time.deltaTime;
                break;
            default:
                break;
        }

        if (ofsx < 0 || w < ofsx) {
            a = true;
        }

        if (ofsy < 0 || h < ofsy) {
            b = true;
        }

        if(a & b)
            n = nd;
        else if(a)
            n = nh;
        else if(b)
            n = nv;

        if(a | b) {
            nh = (5 - n) % 4; //Horizontal (가로)
            nv = (n + 2) % 4; //Vertical (세로)
            nd = 3 - n; //Diagonal (대각선)
            a = false;
            b = false;
        }

        if (ofsy < 0) {
            ofsy += h;
        }else if (h < ofsy) {
            ofsy -= h;
        }

        if (ofsx < 0) {
            ofsx += w;
        }else if (w < ofsx) {
            ofsx -= w;
        }

    }

    public void setState(int state) {
        this.state = state;
    }

    //-----------------------------
    // draw <-- onDraw
    //-----------------------------
    public void draw(Canvas canvas) {

        canvas.drawBitmap(arSky[nd], ofsx - w, ofsy - h, null); // [0]
        canvas.drawBitmap(arSky[nv], ofsx, ofsy - h, null); // [1]
        canvas.drawBitmap(arSky[nd], ofsx + w, ofsy - h, null); //[2]
        canvas.drawBitmap(arSky[nh], ofsx - w, ofsy, null); // [3]
        canvas.drawBitmap(arSky[n], ofsx, ofsy, null); // [4]
        canvas.drawBitmap(arSky[nh], ofsx + w, ofsy, null); // [5]
        canvas.drawBitmap(arSky[nd], ofsx - w, ofsy + h, null); // [6]
        canvas.drawBitmap(arSky[nv], ofsx, ofsy + h, null); // [7]
        canvas.drawBitmap(arSky[nd], ofsx + w, ofsy + h, null); // [8]
    }

    /*
    * 책의 예제에서는 imgCnt가 3이었지만, 사실 세로 무한 스크롤을 구현하는 데 최소로 필요한 이미지 소스는 2개 뿐입니다.
    * 종이를 원통 모양으로 접어서 잇는다고 생각하면 쉽죠. (가로 무한 스크롤도 마찬가지)
    * 그것을 가로 세로 둘 다 되게 하기 위해서 최소로 필요한 이미지 소스는 s0, s1, s2, s3 총 2x2=4개입니다.
    * 그리고 상하 좌우 양방향으로 스크롤 되게 하기 위해 3x3 으로 렌더링을 실시합니다.
    *
    * center, 정확히는 ofset의 x,y가 좌측 상단의 모서리에 해당하는 이미지의 인덱스를 n이라고 했을 때.
    * 다음과 같은 총 4가지의 패턴이 반복됩니다.
    *
    * i) n = 0
    * {{3,2,3},
    * {1,0,1},
    * {3,2,3}}
    *
    * ii) n = 1
    * {{2,3,2},
    * {0,1,0},
    * {2,3,2}}
    *
    * iii) n = 2
    * {{1,0,1},
    * {3,2,3},
    * {1,0,1}}
    *
    * iv) n = 3
    * {{0,1,0},
    * {2,3,2},
    * {0,1,0}}
    *
    * 각각의 경우를 저장하는 자료구조를 따로 만들어도 되지만 그렇게 하면 저장공간도 낭비되고 따로 만든 자료구조에 계속 접근해야하므로 성능도 떨어집니다.
    * 따라서 중앙의 원소를 기준으로 간단한 연산으로 각 행렬의 원소를 구하는 것이 낫다고 판단했습니다.
    *
    * 1행 1열의 원소 n (정의역 N={0,1,2,3})에 대하여 모든 행렬의 원소들은 하나로 대응되는 관계이므로
    * {{d(n), v(n), d(n)},
    * {h(n), n, h(n) },
    * {d(n), v(n), d(n) }}
    * 로 일반화할 수 있습니다. (h는 horizontal(가로)의 h, v는 vertical(세로)의 v, d는 diagonal(대각선)의 d)
	*
    * 정의역 N={0,1,2,3}에서 기능하는 행렬의 각 원소를 구하는 해시 함수를 적당히 구해보면
	*
    * h:
    * h(0) = 1
    * h(1) = 0
    * h(2) = 3
    * h(3) = 2
    * h(n) = (5 - n) % 4
    *
    * v:
    * v(0) = 2
    * v(1) = 3
    * v(2) = 0
    * v(3) = 1
    * v(n) = (n + 2) % 4
    *
    * d:
    * d(0) = 3
    * d(1) = 2
    * d(2) = 1
    * d(3) = 0
    * d(n) = 3 - n
    *
    * 이 됩니다.
    *
    */

    //-----------------------------
    // 비트맵 만들기
    //-----------------------------
    private void createBitmap(Context context) {

        // 이미지를 배열에 저장
        Bitmap tmp;
        tmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.s0);
        arSky[0] = Bitmap.createScaledBitmap(tmp, w, h, true);
        tmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.s1);
        arSky[1] = Bitmap.createScaledBitmap(tmp, w, h, true);
        tmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.s2);
        arSky[2] = Bitmap.createScaledBitmap(tmp, w, h, true);
        tmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.s3);
        arSky[3] = Bitmap.createScaledBitmap(tmp, w, h, true);
    }

} // Sky
