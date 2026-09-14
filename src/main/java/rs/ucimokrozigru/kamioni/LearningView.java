package rs.ucimokrozigru.kamioni;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

public class LearningView extends View {
    private static final float W = 1080f;
    private static final float H = 1920f;
    private static final int HOME = 0;
    private static final int GAME = 1;
    private static final int COMPLETE = 2;
    private static final RectF IMAGE = new RectF(90, 475, 990, 1600);

    private static final String[] TITLES = {
            "KIPER", "TERETNI KAMION", "KIPER", "VATROGASNI KAMION",
            "MEŠALICA", "ŠLEP KAMION", "SMEĆAR", "CISTERNA",
            "DOSTAVNI KAMION", "PRONAĐI KAMION"
    };
    private static final String[] QUESTIONS = {
            "Gde su točkovi? Dodirni jedan točak.",
            "Gde je kabina kamiona? Dodirni kabinu.",
            "Pronađi sanduk u kome se nosi teret.",
            "Gde su merdevine vatrogasnog kamiona?",
            "Pronađi veliki bubanj mešalice.",
            "Gde je kuka šlep kamiona?",
            "Pronađi zeleni kontejner kamiona smećara.",
            "Gde je velika cisterna?",
            "Pronađi prednje svetlo kamiona.",
            "Koje od ova tri vozila je kamion?"
    };
    private static final String[] SHORT_ANSWERS = {
            "TO JE TOČAK!", "TO JE KABINA!", "TO JE SANDUK!", "TO SU MERDEVINE!",
            "TO JE BUBANJ!", "TO JE KUKA!", "TO JE KONTEJNER!", "TO JE CISTERNA!",
            "TO JE SVETLO!", "TO JE KAMION!"
    };
    private static final int[] SCENE_IMAGES = {
            R.drawable.scene_0, R.drawable.scene_1, R.drawable.scene_2, R.drawable.scene_3,
            R.drawable.scene_4, R.drawable.scene_5, R.drawable.scene_6, R.drawable.scene_7,
            R.drawable.scene_8, R.drawable.scene_9
    };
    private static final int[] QUESTION_AUDIO = {
            R.raw.q0, R.raw.q1, R.raw.q2, R.raw.q3, R.raw.q4,
            R.raw.q5, R.raw.q6, R.raw.q7, R.raw.q8, R.raw.q9
    };
    private static final int[] ANSWER_AUDIO = {
            R.raw.a0, R.raw.a1, R.raw.a2, R.raw.a3, R.raw.a4,
            R.raw.a5, R.raw.a6, R.raw.a7, R.raw.a8, R.raw.a9
    };

    private final MainActivity activity;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Path path = new Path();
    private final Rect source = new Rect();
    private Bitmap sceneBitmap;
    private int screen = HOME;
    private int scene = 0;
    private int totalAttempts = 0;
    private boolean answered = false;
    private boolean soundEnabled = true;
    private float scale = 1f;
    private float offsetX;
    private float offsetY;
    private float tapX;
    private float tapY;
    private long celebrationStart;
    private long shakeStart;
    private long screenStart = SystemClock.uptimeMillis();
    private Runnable voiceTask;
    private Runnable reminderTask;

    private final int navy = Color.rgb(25, 54, 82);
    private final int orange = Color.rgb(255, 134, 52);
    private final int cream = Color.rgb(255, 248, 232);
    private final int yellow = Color.rgb(251, 200, 58);
    private final int sky = Color.rgb(171, 226, 240);
    private final int green = Color.rgb(52, 177, 106);
    private final int red = Color.rgb(238, 83, 77);
    private final int muted = Color.rgb(83, 107, 127);

    public LearningView(Context context) {
        super(context);
        activity = (MainActivity) context;
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setContentDescription("Interaktivna knjiga Učimo kroz igru – Kamioni");
        loadScene(0);
        scheduleVoice(R.raw.welcome, 650);
    }

    public boolean isSoundEnabled() { return soundEnabled; }

    public boolean goHome() {
        if (screen == HOME) return false;
        cancelScheduledVoice();
        activity.stopVoice();
        screen = HOME;
        scene = 0;
        answered = false;
        screenStart = SystemClock.uptimeMillis();
        loadScene(0);
        scheduleVoice(R.raw.welcome, 450);
        invalidate();
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelScheduledVoice();
        if (sceneBitmap != null) {
            sceneBitmap.recycle();
            sceneBitmap = null;
        }
        super.onDetachedFromWindow();
    }

    private void loadScene(int index) {
        if (sceneBitmap != null && !sceneBitmap.isRecycled()) sceneBitmap.recycle();
        sceneBitmap = BitmapFactory.decodeResource(getResources(), SCENE_IMAGES[index]);
        if (sceneBitmap != null) source.set(0, 0, sceneBitmap.getWidth(), sceneBitmap.getHeight());
    }

    private void scheduleVoice(final int resourceId, long delay) {
        if (voiceTask != null) removeCallbacks(voiceTask);
        voiceTask = new Runnable() {
            @Override public void run() { activity.playVoice(resourceId); }
        };
        postDelayed(voiceTask, delay);
    }

    private void scheduleReminder() {
        if (reminderTask != null) removeCallbacks(reminderTask);
        reminderTask = new Runnable() {
            @Override public void run() {
                if (screen == GAME && answered) activity.playVoice(R.raw.next_reminder);
            }
        };
        postDelayed(reminderTask, 11500);
    }

    private void cancelScheduledVoice() {
        if (voiceTask != null) removeCallbacks(voiceTask);
        if (reminderTask != null) removeCallbacks(reminderTask);
        voiceTask = null;
        reminderTask = null;
    }

    @Override
    protected void onDraw(Canvas real) {
        super.onDraw(real);
        float sx = getWidth() / W;
        float sy = getHeight() / H;
        scale = Math.min(sx, sy);
        offsetX = (getWidth() - W * scale) / 2f;
        offsetY = (getHeight() - H * scale) / 2f;
        real.drawColor(navy);
        real.save();
        real.translate(offsetX, offsetY);
        real.scale(scale, scale);
        if (screen == HOME) drawHome(real);
        else if (screen == GAME) drawGame(real);
        else drawComplete(real);
        real.restore();

        if (screen == HOME || answered || SystemClock.uptimeMillis() - shakeStart < 650
                || screen == COMPLETE) postInvalidateDelayed(16);
    }

    private void drawHome(Canvas c) {
        fill(c, cream, 0, 0, W, H);
        fill(c, sky, 0, 0, W, 430);
        drawCloud(c, 135, 180, 1f);
        drawCloud(c, 910, 290, .72f);
        text(c, "UČIMO KROZ", 540, 120, 55, navy, true, Paint.Align.CENTER);
        text(c, "IGRU", 540, 220, 108, orange, true, Paint.Align.CENTER);
        rounded(c, Color.WHITE, 145, 280, 935, 405, 50);
        text(c, "KAMIONI", 540, 365, 65, navy, true, Paint.Align.CENTER);

        float bob = (float) Math.sin((SystemClock.uptimeMillis() - screenStart) / 430.0) * 9f;
        drawImageCard(c, new RectF(170, 465 + bob, 910, 1390 + bob), 64);
        rounded(c, Color.argb(220, 255, 255, 255), 238, 1280 + bob, 842, 1365 + bob, 36);
        text(c, "10 ZABAVNIH ZADATAKA", 540, 1338 + bob, 29, navy, true, Paint.Align.CENTER);

        float pulse = 1f + .025f * (float) Math.sin((SystemClock.uptimeMillis() - screenStart) / 260.0);
        c.save();
        c.scale(pulse, pulse, 540, 1535);
        roundedShadow(c, orange, 165, 1440, 915, 1628, 88);
        drawPlay(c, 330, 1534, Color.WHITE);
        text(c, "POČNI IGRU", 610, 1560, 48, Color.WHITE, true, Paint.Align.CENTER);
        c.restore();
        text(c, "Dodirni sliku • Slušaj • Nauči", 540, 1730, 31, muted, false, Paint.Align.CENTER);
        drawSoundButton(c, 965, 90);
    }

    private void drawGame(Canvas c) {
        fill(c, cream, 0, 0, W, H);
        fill(c, navy, 0, 0, W, 170);
        circle(c, Color.WHITE, 75, 84, 47);
        drawBack(c, 75, 84, navy);
        rounded(c, Color.rgb(51, 79, 105), 150, 39, 795, 130, 46);
        rounded(c, orange, 150, 39, 150 + 645f * (scene + 1) / 10f, 130, 46);
        text(c, (scene + 1) + " OD 10", 473, 101, 32, Color.WHITE, true, Paint.Align.CENTER);
        drawSoundButton(c, 990, 84);

        roundedShadow(c, Color.WHITE, 60, 205, 1020, 435, 44);
        circle(c, orange, 150, 320, 61);
        drawSpeaker(c, 150, 320, Color.WHITE);
        text(c, TITLES[scene], 238, 276, 29, orange, true, Paint.Align.LEFT);
        fitText(c, QUESTIONS[scene], 238, 341, 715, 43, navy);
        text(c, "Dodirni zvučnik da ponoviš pitanje", 238, 397, 25, muted, false, Paint.Align.LEFT);

        long shakeAge = SystemClock.uptimeMillis() - shakeStart;
        float shake = shakeAge < 650 ? (float) Math.sin(shakeAge / 30.0) * (1f - shakeAge / 650f) * 22f : 0f;
        c.save();
        c.translate(shake, 0);
        drawImageCard(c, IMAGE, 54);
        c.restore();

        if (answered) drawSuccessOverlay(c);

        for (int i = 0; i < 10; i++) {
            int color = i < scene ? green : (i == scene ? orange : Color.rgb(218, 224, 226));
            circle(c, color, 205 + i * 74, 1655, i == scene ? 16 : 10);
            if (i < scene) drawCheck(c, 205 + i * 74, 1655, Color.WHITE, .55f);
        }

        if (answered) drawNextButton(c);
        else {
            rounded(c, Color.rgb(239, 231, 214), 275, 1695, 805, 1808, 54);
            drawSpeaker(c, 350, 1752, navy);
            text(c, "PONOVI PITANJE", 585, 1764, 28, navy, true, Paint.Align.CENTER);
        }
        text(c, "POKUŠAJI: " + totalAttempts, 540, 1870, 28, muted, true, Paint.Align.CENTER);
    }

    private void drawSuccessOverlay(Canvas c) {
        long age = SystemClock.uptimeMillis() - celebrationStart;
        float pop = Math.min(1f, age / 360f);
        pop = 1f - (1f - pop) * (1f - pop);
        float ring = 42 + pop * 48;
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(15);
        p.setColor(Color.WHITE);
        p.setShadowLayer(14, 0, 4, Color.argb(120, 0, 0, 0));
        c.drawCircle(tapX, tapY, ring, p);
        p.setShadowLayer(0, 0, 0, 0);
        p.setColor(green);
        p.setStrokeWidth(10);
        c.drawCircle(tapX, tapY, ring, p);
        p.setStyle(Paint.Style.FILL);
        circle(c, green, tapX + ring * .66f, tapY - ring * .66f, 35);
        drawCheck(c, tapX + ring * .66f, tapY - ring * .66f, Color.WHITE, 1f);
        drawConfetti(c, age);
    }

    private void drawNextButton(Canvas c) {
        double t = (SystemClock.uptimeMillis() - celebrationStart) / 260.0;
        float pulse = 1f + .035f * (float) Math.sin(t);
        float arrowShift = 8f * (float) Math.sin(t * 1.3);
        c.save();
        c.scale(pulse, pulse, 540, 1757);
        roundedShadow(c, green, 105, 1685, 975, 1830, 64);
        text(c, SHORT_ANSWERS[scene], 300, 1744, 27, Color.WHITE, true, Paint.Align.CENTER);
        text(c, scene == 9 ? "VIDI REZULTAT" : "SLEDEĆI ZADATAK", 610, 1792, 30, Color.WHITE, true, Paint.Align.CENTER);
        circle(c, Color.WHITE, 895 + arrowShift, 1757, 50);
        drawForward(c, 895 + arrowShift, 1757, green);
        c.restore();
    }

    private void drawComplete(Canvas c) {
        fill(c, sky, 0, 0, W, H);
        drawCloud(c, 145, 195, 1.05f);
        drawCloud(c, 920, 360, .78f);
        roundedShadow(c, Color.WHITE, 70, 215, 1010, 1585, 70);
        text(c, totalAttempts == 10 ? "SAVRŠENO!" : "BRAVO!", 540, 390, 91, orange, true, Paint.Align.CENTER);
        text(c, "ZAVRŠIO SI SVIH 10 ZADATAKA", 540, 475, 32, navy, true, Paint.Align.CENTER);

        circle(c, yellow, 540, 720, 170);
        star(c, 540, 720, 120, Color.WHITE);
        text(c, "10 ZADATAKA", 540, 980, 35, muted, true, Paint.Align.CENTER);
        text(c, "IZ " + totalAttempts + " POKUŠAJA", 540, 1062, 61, navy, true, Paint.Align.CENTER);
        String result = totalAttempts == 10 ? "SVAKA ČAST, MAJSTORE!"
                : totalAttempts <= 13 ? "ODLIČAN REZULTAT!"
                : "POKUŠAJ PONOVO I BUDI JOŠ BOLJI!";
        fitTextCentered(c, result, 540, 1165, 790, 41, totalAttempts == 10 ? green : orange);

        float pulse = 1f + .025f * (float) Math.sin((SystemClock.uptimeMillis() - screenStart) / 260.0);
        c.save();
        c.scale(pulse, pulse, 540, 1375);
        roundedShadow(c, orange, 170, 1285, 910, 1465, 85);
        drawReplay(c, 315, 1375, Color.WHITE);
        text(c, "IGRAJ PONOVO", 610, 1400, 44, Color.WHITE, true, Paint.Align.CENTER);
        c.restore();
        text(c, "Vežbom postajemo sve bolji!", 540, 1705, 32, navy, true, Paint.Align.CENTER);
        drawConfetti(c, SystemClock.uptimeMillis() - screenStart);
        drawSoundButton(c, 965, 90);
    }

    private void drawImageCard(Canvas c, RectF destination, float radius) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);
        p.setShadowLayer(18, 0, 8, Color.argb(55, 19, 42, 61));
        c.drawRoundRect(destination, radius, radius, p);
        p.setShadowLayer(0, 0, 0, 0);
        if (sceneBitmap == null) return;
        c.save();
        path.reset();
        path.addRoundRect(destination, radius, radius, Path.Direction.CW);
        c.clipPath(path);
        c.drawBitmap(sceneBitmap, source, destination, p);
        c.restore();
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(7);
        p.setColor(Color.WHITE);
        c.drawRoundRect(destination, radius, radius, p);
        p.setStyle(Paint.Style.FILL);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) return true;
        float x = (event.getX() - offsetX) / scale;
        float y = (event.getY() - offsetY) / scale;

        if (distance(x, y, 970, screen == GAME ? 84 : 90) < 80) {
            soundEnabled = !soundEnabled;
            if (!soundEnabled) activity.stopVoice();
            else replayCurrentVoice();
            invalidate();
            return true;
        }

        if (screen == HOME) {
            if (inside(x, y, 140, 1400, 940, 1660)) startGame();
            return true;
        }

        if (screen == COMPLETE) {
            if (inside(x, y, 130, 1245, 950, 1505)) startGame();
            return true;
        }

        if (distance(x, y, 75, 84) < 72) {
            goHome();
            return true;
        }
        if (inside(x, y, 60, 195, 1020, 445) || (!answered && inside(x, y, 250, 1670, 830, 1835))) {
            activity.playVoice(QUESTION_AUDIO[scene]);
            return true;
        }
        if (answered && inside(x, y, 70, 1650, 1010, 1860)) {
            nextScene();
            return true;
        }
        if (!answered && IMAGE.contains(x, y)) checkAnswer(x, y);
        return true;
    }

    private void startGame() {
        cancelScheduledVoice();
        activity.stopVoice();
        screen = GAME;
        scene = 0;
        totalAttempts = 0;
        answered = false;
        screenStart = SystemClock.uptimeMillis();
        loadScene(scene);
        scheduleVoice(QUESTION_AUDIO[scene], 500);
        invalidate();
    }

    private void nextScene() {
        cancelScheduledVoice();
        activity.stopVoice();
        if (scene == 9) {
            screen = COMPLETE;
            answered = false;
            screenStart = SystemClock.uptimeMillis();
            scheduleVoice(finalAudio(), 550);
        } else {
            scene++;
            answered = false;
            screenStart = SystemClock.uptimeMillis();
            loadScene(scene);
            scheduleVoice(QUESTION_AUDIO[scene], 520);
        }
        invalidate();
    }

    private int finalAudio() {
        if (totalAttempts < 10 || totalAttempts > 20) return R.raw.final_many;
        int id = getResources().getIdentifier("final_" + totalAttempts, "raw", getContext().getPackageName());
        return id == 0 ? R.raw.final_many : id;
    }

    private void replayCurrentVoice() {
        if (screen == HOME) activity.playVoice(R.raw.welcome);
        else if (screen == COMPLETE) activity.playVoice(finalAudio());
        else activity.playVoice(answered ? ANSWER_AUDIO[scene] : QUESTION_AUDIO[scene]);
    }

    private void checkAnswer(float x, float y) {
        totalAttempts++;
        float nx = (x - IMAGE.left) / IMAGE.width();
        float ny = (y - IMAGE.top) / IMAGE.height();
        if (isCorrect(scene, nx, ny)) {
            answered = true;
            tapX = x;
            tapY = y;
            celebrationStart = SystemClock.uptimeMillis();
            activity.stopVoice();
            activity.successSound();
            scheduleVoice(ANSWER_AUDIO[scene], 420);
            scheduleReminder();
        } else {
            shakeStart = SystemClock.uptimeMillis();
            activity.stopVoice();
            activity.wrongSound();
            scheduleVoice(R.raw.wrong, 250);
        }
        invalidate();
    }

    private boolean isCorrect(int which, float x, float y) {
        switch (which) {
            case 0:
                return circleHit(x, y, .17f, .68f, .15f)
                        || circleHit(x, y, .36f, .73f, .17f)
                        || circleHit(x, y, .81f, .72f, .16f);
            case 1: return normRect(x, y, .52f, .25f, .98f, .74f);
            case 2: return normRect(x, y, .01f, .12f, .65f, .60f);
            case 3: return normRect(x, y, .04f, .14f, .97f, .39f);
            case 4: return normRect(x, y, .01f, .13f, .69f, .57f);
            case 5: return normRect(x, y, .00f, .27f, .23f, .61f);
            case 6: return normRect(x, y, .01f, .19f, .65f, .65f);
            case 7: return normRect(x, y, .01f, .19f, .69f, .61f);
            case 8: return normRect(x, y, .76f, .42f, .98f, .68f);
            default: return normRect(x, y, .25f, .29f, .68f, .73f);
        }
    }

    private boolean normRect(float x, float y, float l, float t, float r, float b) {
        return x >= l && x <= r && y >= t && y <= b;
    }

    private boolean circleHit(float x, float y, float cx, float cy, float radius) {
        float dx = x - cx;
        float dy = y - cy;
        return dx * dx + dy * dy <= radius * radius;
    }

    private void drawConfetti(Canvas c, long age) {
        if (age > 4200 && screen != COMPLETE) return;
        float fall = (age % 3600) / 3600f;
        int[] colors = {orange, yellow, green, red, Color.rgb(91, 154, 222), Color.rgb(170, 98, 199)};
        for (int i = 0; i < 32; i++) {
            float x = 42 + ((i * 137) % 995);
            float base = ((i * 83) % 720);
            float y = (base + fall * 900 + i * 7) % 920 + 170;
            float sway = 17f * (float) Math.sin(age / 220.0 + i);
            p.setColor(colors[i % colors.length]);
            p.setStyle(Paint.Style.FILL);
            c.save();
            c.rotate((age / 13f + i * 31) % 180, x + sway, y);
            c.drawRoundRect(x + sway - 8, y - 15, x + sway + 8, y + 15, 5, 5, p);
            c.restore();
        }
    }

    private void drawSoundButton(Canvas c, float x, float y) {
        circle(c, soundEnabled ? Color.WHITE : Color.rgb(233, 105, 94), x, y, 47);
        drawSpeaker(c, x - 2, y, soundEnabled ? navy : Color.WHITE);
        if (!soundEnabled) {
            p.setColor(Color.WHITE);
            p.setStrokeWidth(7);
            p.setStyle(Paint.Style.STROKE);
            c.drawLine(x - 25, y - 27, x + 28, y + 28, p);
            p.setStyle(Paint.Style.FILL);
        }
    }

    private void drawSpeaker(Canvas c, float x, float y, int color) {
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);
        c.drawRoundRect(x - 30, y - 13, x - 12, y + 13, 5, 5, p);
        path.reset();
        path.moveTo(x - 12, y - 14);
        path.lineTo(x + 10, y - 31);
        path.lineTo(x + 10, y + 31);
        path.lineTo(x - 12, y + 14);
        path.close();
        c.drawPath(path, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(6);
        c.drawArc(new RectF(x - 2, y - 24, x + 38, y + 24), -58, 116, false, p);
        p.setStyle(Paint.Style.FILL);
    }

    private void drawPlay(Canvas c, float x, float y, int color) {
        path.reset();
        path.moveTo(x - 27, y - 38);
        path.lineTo(x + 42, y);
        path.lineTo(x - 27, y + 38);
        path.close();
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);
        c.drawPath(path, p);
    }

    private void drawBack(Canvas c, float x, float y, int color) {
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(10);
        c.drawLine(x + 18, y - 25, x - 14, y, p);
        c.drawLine(x - 14, y, x + 18, y + 25, p);
        p.setStyle(Paint.Style.FILL);
    }

    private void drawForward(Canvas c, float x, float y, int color) {
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(11);
        c.drawLine(x - 18, y - 25, x + 15, y, p);
        c.drawLine(x + 15, y, x - 18, y + 25, p);
        p.setStyle(Paint.Style.FILL);
    }

    private void drawReplay(Canvas c, float x, float y, int color) {
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(10);
        c.drawArc(new RectF(x - 35, y - 35, x + 35, y + 35), -55, 285, false, p);
        p.setStyle(Paint.Style.FILL);
        path.reset();
        path.moveTo(x + 18, y - 44);
        path.lineTo(x + 49, y - 37);
        path.lineTo(x + 30, y - 12);
        path.close();
        c.drawPath(path, p);
    }

    private void drawCheck(Canvas c, float x, float y, int color, float size) {
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(8 * size);
        c.drawLine(x - 18 * size, y, x - 5 * size, y + 14 * size, p);
        c.drawLine(x - 5 * size, y + 14 * size, x + 22 * size, y - 18 * size, p);
        p.setStyle(Paint.Style.FILL);
    }

    private void drawCloud(Canvas c, float x, float y, float s) {
        circle(c, Color.argb(225, 255, 255, 255), x - 55 * s, y + 12 * s, 40 * s);
        circle(c, Color.argb(225, 255, 255, 255), x, y - 10 * s, 55 * s);
        circle(c, Color.argb(225, 255, 255, 255), x + 60 * s, y + 12 * s, 42 * s);
        rounded(c, Color.argb(225, 255, 255, 255), x - 92 * s, y + 5 * s, x + 100 * s, y + 48 * s, 22 * s);
    }

    private void star(Canvas c, float cx, float cy, float radius, int color) {
        path.reset();
        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5;
            float r = (i % 2 == 0) ? radius : radius * .45f;
            float x = cx + (float) Math.cos(a) * r;
            float y = cy + (float) Math.sin(a) * r;
            if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
        }
        path.close();
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        c.drawPath(path, p);
    }

    private void fitText(Canvas c, String value, float x, float baseline, float maxWidth, float maxSize, int color) {
        float size = maxSize;
        setText(size, true, color, Paint.Align.LEFT);
        while (p.measureText(value) > maxWidth && size > 28) {
            size -= 1;
            setText(size, true, color, Paint.Align.LEFT);
        }
        c.drawText(value, x, baseline, p);
    }

    private void fitTextCentered(Canvas c, String value, float x, float baseline, float maxWidth, float maxSize, int color) {
        float size = maxSize;
        setText(size, true, color, Paint.Align.CENTER);
        while (p.measureText(value) > maxWidth && size > 24) {
            size -= 1;
            setText(size, true, color, Paint.Align.CENTER);
        }
        c.drawText(value, x, baseline, p);
    }

    private void text(Canvas c, String value, float x, float y, float size, int color, boolean bold, Paint.Align align) {
        setText(size, bold, color, align);
        c.drawText(value, x, y, p);
    }

    private void setText(float size, boolean bold, int color, Paint.Align align) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        p.setTextSize(size);
        p.setTextAlign(align);
        p.setTypeface(bold ? Typeface.create("sans-serif", Typeface.BOLD)
                : Typeface.create("sans-serif", Typeface.NORMAL));
    }

    private void fill(Canvas c, int color, float l, float t, float r, float b) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        c.drawRect(l, t, r, b, p);
    }

    private void rounded(Canvas c, int color, float l, float t, float r, float b, float radius) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        c.drawRoundRect(new RectF(l, t, r, b), radius, radius, p);
    }

    private void roundedShadow(Canvas c, int color, float l, float t, float r, float b, float radius) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        p.setShadowLayer(16, 0, 8, Color.argb(65, 15, 39, 56));
        c.drawRoundRect(new RectF(l, t, r, b), radius, radius, p);
        p.setShadowLayer(0, 0, 0, 0);
    }

    private void circle(Canvas c, int color, float x, float y, float radius) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        c.drawCircle(x, y, radius, p);
    }

    private boolean inside(float x, float y, float l, float t, float r, float b) {
        return x >= l && x <= r && y >= t && y <= b;
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
