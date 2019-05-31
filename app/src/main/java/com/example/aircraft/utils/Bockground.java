package com.example.aircraft.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import com.example.aircraft.R;

import java.util.Vector;

class constant {
    public static int js = 0;//击杀数
    public static int w, h;//屏幕的宽高
    public static float bili;//比例，用于适应不同屏幕
    public static Vector<hj> list = new Vector<hj>();//所有飞行物的集合,添加进这个集合才能被画出来
    public static Vector<hj> drlist = new Vector<hj>();//敌人飞机的集合，添加进这个集合才能被子弹打中

    public static Bitmap myhj, drhj, bj, myzd;//图片：我的灰机 敌人灰机 背景 我的子弹
    public static myhj my;//我的灰机
    public static bj b;//背景
}

public class Bockground extends View {//画
    private Paint p=new Paint();//画笔
    private float x,y;//按下屏幕时的坐标
    private float myx,myy;//按下屏幕时玩家飞机的坐标

    public Bockground(Context context) {
        super(context);
        //添加事件控制玩家飞机
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent e) {
                if(e.getAction()==MotionEvent.ACTION_DOWN){
                    x=e.getX();
                    y=e.getY();
                    myx=constant.my.r.left;
                    myy=constant.my.r.top;
                }
                float xx=myx+e.getX()-x;
                float yy=myy+e.getY()-y;
                //我的飞机不能飞出屏幕
                xx=xx<constant.w-constant.my.w/2?xx:constant.w-constant.my.w/2;
                xx=xx>-constant.my.w/2?xx:-constant.my.w/2;
                yy=yy<constant.h-constant.my.h/2?yy:constant.h-constant.my.h/2;
                yy=yy>-constant.my.h/2?yy:-constant.my.h/2;
                constant.my.setX(xx);
                constant.my.setY(yy);
                return true;
            }
        });

        setBackgroundColor(Color.BLACK);//设背景颜色为黑色

        constant.myhj= BitmapFactory.decodeResource(getResources(),R.mipmap.hj);//加载图片
        constant.drhj=BitmapFactory.decodeResource(getResources(),R.mipmap.dr);
        constant.myzd=BitmapFactory.decodeResource(getResources(),R.mipmap.zd);
        constant.bj=BitmapFactory.decodeResource(getResources(), R.mipmap.bj);

        new Thread(new re()).start();//新建一个线程 让画布自动重绘
        new Thread(new loaddr()).start();//新建一个 加载敌人的线程
    }
    @Override
    protected void onDraw(Canvas g) {//这个相当于swing的paint方法吧 用于绘制屏幕上的所有物体
        super.onDraw(g);
        g.drawBitmap(constant.b.img,null,constant.b.r,p);//画背景 我没有把背景添加到list里

        for(int i=0;i<constant.list.size();i++){//我们把所有的飞行物都添加到了my.list这个集合里
            hj h=constant.list.get(i);           //然后在这里用一个for循环画出来
            g.drawBitmap(h.img,null,h.r,p);
        }
        g.drawText("击杀："+constant.js,0,constant.h-50,p);

    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {//这个方法用来获取屏幕宽高的
        super.onSizeChanged(w, h, oldw, oldh);
        constant.w=w;//获取宽
        constant.h=h;//高

        //获取手机（应该不是手机的吧 是这控件的吧）分辨率和1920*1080的比例
        //然后飞机的宽高乘上这个分辨率就能在不同大小的屏幕正常显示了
        //为什么用1920*1080呢 因为我手机就是这个分辨率。。。
        constant.bili= (float) (Math.sqrt(constant.w * constant.h)/ Math.sqrt(1920 * 1080));
        p.setTextSize(50*constant.bili);//设置字体大小，“击杀”的大小
        p.setColor(Color.WHITE);//设为白色
        //好了 到这里游戏开始了
        constant.b=new bj();//初始化背景
        constant.my=new myhj();//初始化 我的灰机
    }
    private class re implements Runnable {
        @Override
        public void run() {
            //每10ms刷新一次界面
            while(true){
                try { Thread.sleep(10);} catch (InterruptedException e) {e.printStackTrace();}
                postInvalidate();//刷新画布
                //swing是repaint()方法刷新的
                //然后这里没有repaint方法
                //C#有一个invalidate()方法是刷新画布的
                //然后这线程里用invalidate()会闪退.....
            }
        }
    }
    private class loaddr implements Runnable{
        @Override
        public void run() {
            while(true){
                //每300ms刷一个敌人
                try {Thread.sleep(300);} catch (InterruptedException e) {e.printStackTrace();}
                try {
                    new drhj();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
class hj{//游戏内所有物体的父类
    public RectF r=new RectF();//这个是用来确定位置的
    public int hp;//生命
    public float w,h;//宽高
    public Bitmap img;//图片


    //这里的画图方法和swing的不太一样
    //设两个方法来设置x,y的坐标
    public void setX(float x){
        r.left=x;
        r.right=x+w;
    }
    public void setY(float y){
        r.top=y;
        r.bottom=y+h;
    }

    public boolean pengzhuang(hj obj,float px) {//判断碰撞 判断时忽略px个像素
        px*=constant.bili;//凡是涉及到像素的 都乘一下分辨率比例my.bili
        if (r.left+px - obj.r.left <= obj.w && obj.r.left - this.r.left+px <= this.w-px-px)
            if (r.top+px - obj.r.top <= obj.h && obj.r.top - r.top+px <= h-px-px) {
                return true;
            }
        return false;

    }
}
class bj extends hj implements  Runnable{//背景
    public bj(){
        w=constant.w;
        h=constant.h*2;//背景的高是 屏幕高的两倍
        img=constant.bj;
        setX(0);
        setY(-constant.h);
        new Thread(this).start();
    }
    @Override
    public void run() {
        //这里控制背景一直向下移
        while(true){
            try {Thread.sleep(10);} catch (InterruptedException e) {e.printStackTrace();}
            if(r.top+2<=0){
                setY(r.top+2);
            }else{
                setY(-constant.h);
            }
        }
    }
}

class drhj extends hj implements Runnable{//敌人灰机
    private long sd0=(long) (Math.random()*10)+10;//生成一个[10,20)的随机数 用来控制敌人速度 敌人速度是不一样的

    public drhj(){
//        w=my.w/5.4f;
//        h=my.h/9.6f;
        w=h=200*constant.bili;
        //敌人刷出来的位置
        setX((float)( Math.random()*(constant.w-w)));//x是随机的
        setY(-h);//在屏幕外 刚好看不到的位置
        img=constant.drhj;
        hp=12;//生命=12
        constant.list.add(this);//添加到集合里 这样才能被画出来
        constant.drlist.add(this);//添加到敌人的集合 添加进这个集合子弹才打得到
        new Thread(this).start();
    }

    @Override
    public void run() {
        while(hp>0){//如果生命>0 没有死 就继续向前飞，死了还飞什么？
            try {Thread.sleep(sd0);} catch (InterruptedException e) {e.printStackTrace();}
            setY(r.top+2*constant.bili);
            if(r.top>=constant.h)break;//敌人飞出屏幕 跳出循环
        }
        //从集合删除
        constant.list.remove(this);
        constant.drlist.remove(this);
    }
}

class myhj extends hj implements Runnable{//我的灰机

    public myhj(){
        w=h=200*constant.bili;//凡是涉及到像素的 都乘一下分辨率比例my.bili
        //设置初始位置
        setX(constant.w/2-w/2);
        setY(constant.h*0.7f-h/2);
        img=constant.myhj;//初始化图片
        constant.list.add(this);//添加到集合里 这样才能被画出来
        new Thread(this).start();//发射子弹的线程
    }

    @Override
    public void run() {
        while(true){
            //90毫秒发射一发子弹
            try {Thread.sleep(90);} catch (InterruptedException e) {e.printStackTrace();}
            new myzd(this);
        }
    }
}
class myzd extends hj implements Runnable{//我的子弹
    private int dps;
    private float sd0;

    public myzd(hj hj){
        w=h=90*constant.bili;//凡是涉及到像素的 都乘一下分辨率比例my.bili
        img=constant.myzd;//图片
        sd0=6*constant.bili;//速度=6
        dps=6;//伤害=6
        //设在玩家中心的偏上一点
        setX(hj.r.left+hj.w/2-w/2);
        setY(hj.r.top-h/2);
        constant.list.add(this);//添加到集合里 这样才能被画出来
        new Thread(this).start();//新建一个子弹向上移动的线程
    }

    @Override
    public void run() {
        boolean flag=false;//一个标记 用来跳出嵌套循环
        while(true){
            try {Thread.sleep(5);} catch (InterruptedException e) {e.printStackTrace();}
            setY(r.top-sd0);//向上移sd0个像素，sd0=6

            try {//try一下 怕出错
                //这里判断有没有和集合里的敌人发生碰撞
                for(int i=0;i<constant.drlist.size();i++){
                    hj h=constant.drlist.get(i);
                    if(pengzhuang(h,30)){//判断碰撞
                        h.hp-=dps;//敌人生命-子弹伤害
                        flag=true;//一个标记 用来跳出嵌套循环
                        constant.js++;//击杀+1
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
            if(flag || r.top+h<=0)break;//如果子弹击中过敌人 或者超出屏幕范围 跳出循环
        }
        constant.list.remove(this);//从集合删除
    }
}