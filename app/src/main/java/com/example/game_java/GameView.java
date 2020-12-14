package com.example.game_java;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread поток;
    private boolean запушенЛи, конецЛи = false;
    private int экранХ, экранY, скорость = 0;
    public static float screenRatioX, screenRatioY;
    private final Paint paint;
  //  private Bird[] птицы;
    private SharedPreferences prefs;
    private Random random;
    private final SoundPool звук_пуль;
    private List<Bullet> пули;
    private final int звук;
    private final Flight летание;
    private final GameActivity activity;
    private final Background background1;
    private final Background background2;

    public GameView(GameActivity activity, int screenX, int screenY) {
        super(activity);

        this.activity = activity;

        prefs = activity.getSharedPreferences("game", Context.MODE_PRIVATE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            AudioAttributes audio = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();

            звук_пуль = new SoundPool.Builder()
                    .setAudioAttributes(audio)
                    .build();

        } else
            звук_пуль = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        звук = звук_пуль.load(activity, R.raw.shoot, 1);

        this.экранХ = screenX;
        this.экранY = screenY;
        screenRatioX = 1920f / screenX;
        screenRatioY = 1080f / screenY;

        background1 = new Background(screenX, screenY, getResources());
        background2 = new Background(screenX, screenY, getResources());

        летание = new Flight(this, screenY, getResources());

        пули = new ArrayList<>();

        background2.x = screenX;

        paint = new Paint();
        paint.setTextSize(128);
        paint.setColor(Color.RED);

      //  птицы = new Bird[5];

     //   for (int i = 0;i < 5;i++) {

     //       Bird bird = new Bird(getResources());
    //        птицы[i] = bird;
//
      //  }

      //  random = new Random();

    }

    @Override
    public void run() {

        while (запушенЛи) {

            update ();
            draw ();
            sleep ();

        }

    }

    private void update () {

        background1.x -= 0 * screenRatioX;
        background2.x -= 0 * screenRatioX;

        if (background1.x + background1.background.getWidth() < 0) {
            background1.x = экранХ;
        }

        if (background2.x + background2.background.getWidth() < 0) {
            background2.x = экранХ;
        }

        if (летание.isGoingUp)
            летание.y -= 30 * screenRatioY;
        else
            летание.y += 30 * screenRatioY;

        if (летание.y < 0)
            летание.y = 0;

        if (летание.y >= экранY - летание.height)
            летание.y = экранY - летание.height;

        List<Bullet> trash = new ArrayList<>();

        for (Bullet bullet : пули) {

            if (bullet.x > экранХ)
                trash.add(bullet);

            bullet.x += 50 * screenRatioX;

         /*   for (Bird bird : птицы) {

                if (Rect.intersects(bird.getCollisionShape(),
                        bullet.getCollisionShape())) {

                    скорость++;
                    bird.x = -50;
                    bullet.x = экранХ + 50;
                    bird.wasShot = true;

                }

            }*/

        }

   /*     for (Bullet bullet : trash)
            пули.remove(bullet);

        for (Bird bird : птицы) {

            bird.x -= bird.speed;

            if (bird.x + bird.width < 0) {

                if (!bird.wasShot) {
                    конецЛи = true;
                    return;
                }

                int прыжок = (int) (2 * screenRatioX);
                bird.speed = random.nextInt(прыжок);

                if (bird.speed < 10 * screenRatioX)
                    bird.speed = (int) (10 * screenRatioX);

                bird.x = экранХ;
                bird.y = random.nextInt(экранY - bird.height);

                bird.wasShot = false;
            }

            if (Rect.intersects(bird.getCollisionShape(), летание.getCollisionShape())) {

                конецЛи = false;
                return;
            }

        }

    }*/}

    private void draw () {

        if (getHolder().getSurface().isValid()) {

            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background1.background, background1.x, background1.y, paint);
            canvas.drawBitmap(background2.background, background2.x, background2.y, paint);

         //   for (Bird bird : птицы)
            //    canvas.drawBitmap(bird.getBird(), bird.x, bird.y, paint);
//
         //   canvas.drawText(скорость + "", экранХ / 2f, 164, paint);

            if (конецЛи) {
                запушенЛи = false;
                canvas.drawBitmap(летание.getDead(), летание.x, летание.y, paint);
                getHolder().unlockCanvasAndPost(canvas);
                saveIfHighScore();
                waitBeforeExiting ();
                return;
            }

            canvas.drawBitmap(летание.getFlight(), летание.x, летание.y, paint);

            for (Bullet bullet : пули)
                canvas.drawBitmap(bullet.bullet, bullet.x, bullet.y, paint);

            getHolder().unlockCanvasAndPost(canvas);

        }

    }

    private void waitBeforeExiting() {

        try {
            Thread.sleep(3000);
            activity.startActivity(new Intent(activity, MainActivity.class));
            activity.finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void saveIfHighScore() {

        if (prefs.getInt("highscore", 0) < скорость) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highscore", скорость);
            editor.apply();
        }

    }

    private void sleep () {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume () {

        запушенЛи = true;
        поток = new Thread(this);
        поток.start();

    }

    public void pause () {

        try {
            запушенЛи = false;
            поток.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < экранХ / 2) {
                    летание.isGoingUp = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                летание.isGoingUp = false;
                if (event.getX() > экранХ / 2)
                    летание.toShoot++;
                break;
        }

        return true;
    }

    public void newBullet() {

        if (!prefs.getBoolean("isMute", false))
            звук_пуль.play(звук, 1, 1, 0, 0, 1);

        Bullet bullet = new Bullet(getResources());
        bullet.x = летание.x + летание.width;
        bullet.y = летание.y + (летание.height / 2);
        пули.add(bullet);

    }
}
