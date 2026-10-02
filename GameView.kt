package com.maple.voidextraction

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.random.Random

data class Enemy(var x:Float,var y:Float,var hp:Int=3)
data class Shot(var x:Float,var y:Float,var vx:Float,var vy:Float)

class GameView(c: Context): View(c) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG); private var px=500f; private var py=350f
    private var joyX=0f; private var joyY=0f; private var moveId=-1; private var aimId=-1
    private var hp=100; private var score=0; private var extracted=false; private var last=System.nanoTime()
    private val enemies=mutableListOf<Enemy>(); private val shots=mutableListOf<Shot>()
    private var spawn=0f
    init { p.typeface=Typeface.DEFAULT_BOLD; isFocusable=true }
    override fun onDraw(c:Canvas){
        val now=System.nanoTime(); val dt=((now-last)/1e9f).coerceAtMost(.04f); last=now; update(dt)
        c.drawColor(Color.rgb(7,12,22));
        p.style=Paint.Style.STROKE; p.strokeWidth=2f; p.color=Color.rgb(20,55,70)
        for(x in 0..width step 80)c.drawLine(x.toFloat(),0f,x.toFloat(),height.toFloat(),p)
        for(y in 0..height step 80)c.drawLine(0f,y.toFloat(),width.toFloat(),y.toFloat(),p)
        p.style=Paint.Style.FILL
        // extraction zone
        p.color=Color.argb(90,70,255,170); c.drawCircle(width-100f,100f,65f,p)
        // player armored trooper
        p.color=Color.rgb(80,220,180); c.drawCircle(px,py,26f,p); p.color=Color.rgb(170,255,235); c.drawRect(px-12,py-9,px+18,py+2,p)
        p.color=Color.rgb(255,80,90); enemies.forEach{c.drawCircle(it.x,it.y,22f,p)}
        p.color=Color.CYAN; shots.forEach{c.drawCircle(it.x,it.y,6f,p)}
        p.color=Color.WHITE; p.textSize=28f; c.drawText("HP $hp   KILLS $score",24f,38f,p)
        p.textSize=20f; c.drawText("Reach the green extraction zone after 15 kills",24f,68f,p)
        if(extracted){p.textSize=48f;p.color=Color.rgb(100,255,180);c.drawText("EXTRACTION COMPLETE",width/2f-260,height/2f,p)}
        // touch zones
        p.style=Paint.Style.STROKE;p.strokeWidth=5f;p.color=Color.argb(130,255,255,255);c.drawCircle(120f,height-120f,75f,p);c.drawCircle(width-120f,height-120f,75f,p);p.style=Paint.Style.FILL
        postInvalidateOnAnimation()
    }
    private fun update(dt:Float){ if(extracted)return
        px=(px+joyX*260*dt).coerceIn(30f,width-30f); py=(py+joyY*260*dt).coerceIn(30f,height-30f)
        spawn-=dt; if(spawn<=0){spawn=max(.35f,1.2f-score*.02f); val side=Random.nextInt(4); val e=when(side){0->Enemy(0f,Random.nextFloat()*height);1->Enemy(width.toFloat(),Random.nextFloat()*height);2->Enemy(Random.nextFloat()*width,0f);else->Enemy(Random.nextFloat()*width,height.toFloat())};enemies+=e}
        enemies.forEach{ val dx=px-it.x;val dy=py-it.y;val d=max(1f,sqrt(dx*dx+dy*dy));it.x+=dx/d*90*dt;it.y+=dy/d*90*dt;if(d<45){hp-=1;if(hp<=0){hp=100;score=0;enemies.clear();shots.clear();return}} }
        shots.forEach{it.x+=it.vx*dt;it.y+=it.vy*dt}
        shots.removeAll{it.x<0||it.x>width||it.y<0||it.y>height}
        val dead=mutableSetOf<Enemy>(); val gone=mutableSetOf<Shot>()
        for(s in shots)for(e in enemies){val dx=s.x-e.x;val dy=s.y-e.y;if(dx*dx+dy*dy<800){e.hp--;gone+=s;if(e.hp<=0){dead+=e;score++}}}
        enemies.removeAll(dead);shots.removeAll(gone)
        val ex=px-(width-100f);val ey=py-100f;if(score>=15&&ex*ex+ey*ey<65*65)extracted=true
    }
    override fun onTouchEvent(e:MotionEvent):Boolean{
        for(i in 0 until e.pointerCount){val id=e.getPointerId(i);val x=e.getX(i);val y=e.getY(i);when(e.actionMasked){
            MotionEvent.ACTION_DOWN,MotionEvent.ACTION_POINTER_DOWN->{if(i==e.actionIndex){if(x<width/2){moveId=id;move(x,y)}else{aimId=id;shoot(x,y)}}}
            MotionEvent.ACTION_MOVE->{if(id==moveId)move(x,y); if(id==aimId&&Random.nextInt(4)==0)shoot(x,y)}
            MotionEvent.ACTION_UP,MotionEvent.ACTION_POINTER_UP,MotionEvent.ACTION_CANCEL->{if(i==e.actionIndex){if(id==moveId){moveId=-1;joyX=0f;joyY=0f};if(id==aimId)aimId=-1}}
        }};return true
    }
    private fun move(x:Float,y:Float){val dx=x-120f;val dy=y-(height-120f);val d=max(1f,sqrt(dx*dx+dy*dy));val m=min(1f,d/75f);joyX=dx/d*m;joyY=dy/d*m}
    private fun shoot(x:Float,y:Float){val dx=x-px;val dy=y-py;val d=max(1f,sqrt(dx*dx+dy*dy));shots+=Shot(px,py,dx/d*650,dy/d*650)}
}
