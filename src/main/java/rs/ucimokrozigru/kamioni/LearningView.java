package rs.ucimokrozigru.kamioni;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import java.util.Random;

public class LearningView extends View {
    private static final float W = 1080f;
    private static final float H = 1920f;
    private static final int HOME = 0, GAME = 1, COMPLETE = 2;

    private static final String[] TITLES = {
            "KIPER", "TERETNI KAMION", "KIPER", "VATROGASNI KAMION",
            "MEŠALICA", "ŠLEP KAMION", "SMEĆAR", "CISTERNA",
            "DOSTAVNI KAMION", "PRONAĐI KAMION"
    };
    private static final String[] QUESTIONS = {
            "Gde su točkovi? Dodirni jedan točak.",
            "Gde je kabina kamiona?",
            "Pronađi sanduk u kome se nosi teret.",
            "Gde su merdevine vatrogasnog kamiona?",
            "Pronađi veliki bubanj mešalice.",
            "Gde je kuka šlep kamiona?",
            "Pronađi zeleni kontejner kamiona smećara.",
            "Gde je velika cisterna?",
            "Pronađi prednje svetlo kamiona.",
            "Koje od ovih vozila je kamion?"
    };
    private static final String[] ANSWERS = {
            "Bravo! To je točak.", "Bravo! To je kabina.",
            "Odlično! To je sanduk za teret.", "Bravo! To su merdevine.",
            "Tačno! To je bubanj mešalice.", "Odlično! Pronašao si kuku.",
            "Bravo! To je kontejner za smeće.", "Tačno! To je cisterna.",
            "Bravo! To je prednje svetlo.", "Sjajno! Pronašao si kamion."
    };

    private final MainActivity activity;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final Random random = new Random(7);
    private int screen = HOME;
    private int scene = 0;
    private int stars = 0;
    private boolean answered = false;
    private boolean soundEnabled = true;
    private float scale = 1f, offsetX = 0f, offsetY = 0f;
    private float celebration = 0f;
    private float shake = 0f;

    private final int navy = Color.rgb(24, 50, 79);
    private final int orange = Color.rgb(255, 138, 52);
    private final int cream = Color.rgb(255, 247, 230);
    private final int yellow = Color.rgb(247, 196, 64);
    private final int sky = Color.rgb(152, 221, 239);
    private final int green = Color.rgb(65, 174, 116);
    private final int red = Color.rgb(237, 82, 82);

    public LearningView(Context context) {
        super(context);
        activity = (MainActivity) context;
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setContentDescription("Interaktivna knjiga Učimo kroz igru – Kamioni");
    }

    public boolean isSoundEnabled() { return soundEnabled; }

    public void onVoiceReady() {
        if (screen == HOME) postDelayed(new Runnable() {
            @Override public void run() {
                activity.speak("Dobro došli! Učimo kroz igru.");
            }
        }, 350);
    }

    public boolean goHome() {
        if (screen != HOME) {
            screen = HOME;
            answered = false;
            invalidate();
            activity.speak("Učimo kroz igru. Kamioni.");
            return true;
        }
        return false;
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
    }

    private void drawHome(Canvas c) {
        fill(c, cream, 0, 0, W, H);
        fill(c, sky, 0, 0, W, 530);
        drawCloud(c, 150, 170, 1f);
        drawCloud(c, 830, 270, .75f);
        text(c, "UČIMO KROZ", 540, 145, 62, navy, true, Paint.Align.CENTER);
        text(c, "IGRU", 540, 240, 108, orange, true, Paint.Align.CENTER);
        rounded(c, Color.WHITE, 125, 305, 955, 515, 52);
        text(c, "KAMIONI", 540, 400, 72, navy, true, Paint.Align.CENTER);
        text(c, "Interaktivna knjiga za najmlađe", 540, 468, 31, Color.rgb(80, 104, 125), false, Paint.Align.CENTER);

        c.save();
        c.translate(0, 135);
        drawDumpTruck(c, orange, yellow);
        c.restore();

        rounded(c, orange, 190, 1460, 890, 1635, 85);
        triangle(c, Color.WHITE, 450, 1503, 450, 1593, 535, 1548);
        text(c, "POČNI IGRU", 600, 1573, 49, Color.WHITE, true, Paint.Align.CENTER);
        text(c, "10 zabavnih zadataka", 540, 1712, 34, navy, true, Paint.Align.CENTER);
        text(c, "Dodirni sliku • Slušaj • Nauči", 540, 1765, 29, Color.rgb(86, 106, 122), false, Paint.Align.CENTER);
        drawSoundButton(c, 935, 105);
    }

    private void drawGame(Canvas c) {
        fill(c, cream, 0, 0, W, H);
        fill(c, navy, 0, 0, W, 175);
        circle(c, Color.WHITE, 82, 87, 48);
        text(c, "‹", 82, 105, 72, navy, true, Paint.Align.CENTER);
        rounded(c, Color.rgb(49, 77, 105), 165, 40, 760, 134, 45);
        rounded(c, orange, 165, 40, 165 + 595f * (scene + 1) / QUESTIONS.length, 134, 45);
        text(c, (scene + 1) + " / " + QUESTIONS.length, 462, 104, 34, Color.WHITE, true, Paint.Align.CENTER);
        drawSoundButton(c, 980, 87);

        rounded(c, Color.WHITE, 60, 220, 1020, 430, 46);
        circle(c, orange, 150, 325, 58);
        drawSpeaker(c, 150, 325, Color.WHITE);
        text(c, TITLES[scene], 238, 285, 30, orange, true, Paint.Align.LEFT);
        fitText(c, QUESTIONS[scene], 238, 345, 710, 43, navy);

        c.save();
        c.translate(shake, 0);
        rounded(c, Color.WHITE, 45, 475, 1035, 1395, 54);
        drawScene(c, scene);
        c.restore();

        text(c, "Dodirni tačan deo slike", 540, 1470, 31, Color.rgb(91, 109, 125), false, Paint.Align.CENTER);
        for (int i = 0; i < QUESTIONS.length; i++) {
            int color = i < scene ? green : (i == scene ? orange : Color.rgb(220, 224, 226));
            circle(c, color, 200 + i * 75, 1540, i == scene ? 16 : 11);
        }

        if (answered) {
            rounded(c, green, 95, 1600, 985, 1785, 52);
            text(c, "★  " + ANSWERS[scene], 540, 1680, 37, Color.WHITE, true, Paint.Align.CENTER);
            text(c, scene == QUESTIONS.length - 1 ? "ZAVRŠI" : "SLEDEĆE  ›", 540, 1740, 33, Color.WHITE, true, Paint.Align.CENTER);
            drawConfetti(c);
        } else {
            rounded(c, Color.rgb(236, 229, 215), 300, 1620, 780, 1735, 55);
            text(c, "🔊  PONOVI PITANJE", 540, 1692, 28, navy, true, Paint.Align.CENTER);
        }
        text(c, "★ " + stars, 540, 1855, 32, yellow, true, Paint.Align.CENTER);
    }

    private void drawComplete(Canvas c) {
        fill(c, sky, 0, 0, W, H);
        drawCloud(c, 180, 210, 1.1f);
        drawCloud(c, 860, 390, .8f);
        rounded(c, Color.WHITE, 80, 250, 1000, 1550, 70);
        text(c, "BRAVO!", 540, 450, 106, orange, true, Paint.Align.CENTER);
        text(c, "Završio si knjigu", 540, 535, 42, navy, true, Paint.Align.CENTER);
        circle(c, yellow, 540, 780, 175);
        star(c, 540, 780, 125, Color.WHITE);
        text(c, "OSVOJENO", 540, 1050, 34, Color.rgb(92, 108, 122), true, Paint.Align.CENTER);
        text(c, stars + " ZVEZDICA", 540, 1130, 60, navy, true, Paint.Align.CENTER);
        rounded(c, orange, 190, 1280, 890, 1455, 85);
        text(c, "IGRAJ PONOVO", 540, 1390, 47, Color.WHITE, true, Paint.Align.CENTER);
        text(c, "Učimo kroz igru", 540, 1685, 38, navy, true, Paint.Align.CENTER);
        text(c, "Mala pitanja • Velika otkrića", 540, 1745, 29, Color.rgb(73, 99, 119), false, Paint.Align.CENTER);
        drawConfetti(c);
        drawSoundButton(c, 960, 100);
    }

    private void drawScene(Canvas c, int which) {
        fill(c, Color.rgb(224, 244, 249), 75, 505, 1005, 1185);
        fill(c, Color.rgb(175, 218, 133), 75, 1185, 1005, 1365);
        circle(c, yellow, 865, 620, 72);
        drawCloud(c, 230, 635, .65f);
        fill(c, Color.rgb(116, 125, 132), 75, 1125, 1005, 1315);
        dashedRoad(c, 1205);
        switch (which) {
            case 0: drawDumpTruck(c, orange, yellow); break;
            case 1: drawCargoTruck(c); break;
            case 2: drawDumpTruck(c, Color.rgb(75, 151, 210), orange); break;
            case 3: drawFireTruck(c); break;
            case 4: drawMixer(c); break;
            case 5: drawTowTruck(c); break;
            case 6: drawGarbageTruck(c); break;
            case 7: drawTanker(c); break;
            case 8: drawDeliveryTruck(c); break;
            default: drawVehicleChoice(c); break;
        }
    }

    private void baseWheels(Canvas c, float x1, float x2, float y) {
        circle(c, Color.rgb(37, 49, 59), x1, y, 92);
        circle(c, Color.rgb(37, 49, 59), x2, y, 92);
        circle(c, Color.rgb(190, 201, 208), x1, y, 39);
        circle(c, Color.rgb(190, 201, 208), x2, y, 39);
        circle(c, navy, x1, y, 13);
        circle(c, navy, x2, y, 13);
    }

    private void drawDumpTruck(Canvas c, int body, int bed) {
        path.reset();
        path.moveTo(150, 760); path.lineTo(565, 760); path.lineTo(520, 1015);
        path.lineTo(205, 1015); path.close(); path(c, bed, path);
        rounded(c, body, 565, 790, 890, 1060, 24);
        path.reset(); path.moveTo(650, 700); path.lineTo(805, 700); path.lineTo(890, 820);
        path.lineTo(650, 820); path.close(); path(c, body, path);
        rounded(c, sky, 680, 730, 800, 820, 12);
        rounded(c, Color.rgb(48, 60, 71), 140, 1000, 920, 1100, 18);
        circle(c, yellow, 870, 1000, 28);
        baseWheels(c, 330, 760, 1100);
    }

    private void drawCargoTruck(Canvas c) {
        rounded(c, Color.rgb(75, 151, 210), 130, 680, 610, 1035, 28);
        for (int i = 0; i < 4; i++) line(c, Color.rgb(55, 125, 182), 180 + i * 100, 710, 180 + i * 100, 1000, 12);
        rounded(c, orange, 610, 790, 905, 1060, 24);
        path.reset(); path.moveTo(680, 700); path.lineTo(810, 700); path.lineTo(905, 815); path.lineTo(680, 815); path.close(); path(c, orange, path);
        rounded(c, sky, 708, 730, 810, 808, 10);
        rounded(c, Color.rgb(48, 60, 71), 120, 1010, 930, 1095, 18);
        baseWheels(c, 315, 775, 1100);
    }

    private void drawFireTruck(Canvas c) {
        rounded(c, red, 150, 800, 880, 1060, 28);
        rounded(c, Color.rgb(211, 54, 54), 170, 720, 530, 840, 18);
        for (int i = 0; i < 3; i++) rounded(c, Color.rgb(246, 217, 92), 205 + i * 100, 755, 275 + i * 100, 810, 8);
        rounded(c, Color.WHITE, 250, 585, 815, 635, 18);
        line(c, red, 280, 610, 230, 705, 15); line(c, red, 785, 610, 835, 700, 15);
        for (int i = 0; i < 8; i++) line(c, red, 310 + i * 58, 590, 310 + i * 58, 630, 8);
        rounded(c, sky, 690, 740, 825, 835, 12);
        circle(c, Color.rgb(55, 140, 220), 720, 692, 24); circle(c, red, 780, 692, 24);
        rounded(c, Color.rgb(48, 60, 71), 130, 1010, 910, 1095, 18);
        baseWheels(c, 315, 760, 1100);
    }

    private void drawMixer(Canvas c) {
        rounded(c, yellow, 570, 800, 900, 1060, 25);
        rounded(c, sky, 670, 725, 800, 820, 12);
        c.save(); c.rotate(-18, 430, 850);
        oval(c, Color.rgb(245, 148, 63), 230, 650, 640, 1010);
        ovalStroke(c, Color.WHITE, 230, 650, 640, 1010, 18);
        line(c, Color.WHITE, 320, 690, 430, 990, 22); line(c, Color.WHITE, 455, 680, 560, 955, 22);
        c.restore();
        rounded(c, Color.rgb(48, 60, 71), 135, 1010, 925, 1095, 18);
        baseWheels(c, 330, 770, 1100);
    }

    private void drawTowTruck(Canvas c) {
        rounded(c, Color.rgb(74, 158, 210), 135, 840, 880, 1060, 25);
        path.reset(); path.moveTo(170, 825); path.lineTo(575, 725); path.lineTo(625, 795); path.lineTo(235, 900); path.close(); path(c, Color.rgb(86, 103, 116), path);
        rounded(c, orange, 615, 760, 880, 1055, 24);
        rounded(c, sky, 690, 720, 805, 820, 12);
        line(c, navy, 600, 760, 775, 625, 22); line(c, navy, 775, 625, 820, 820, 14);
        line(c, navy, 820, 820, 820, 920, 10); arc(c, navy, 785, 900, 850, 975, 0, 210, 12);
        rounded(c, Color.rgb(48, 60, 71), 130, 1010, 915, 1095, 18);
        baseWheels(c, 320, 755, 1100);
    }

    private void drawGarbageTruck(Canvas c) {
        rounded(c, green, 150, 700, 610, 1025, 35);
        path.reset(); path.moveTo(150, 760); path.lineTo(210, 690); path.lineTo(560, 690); path.lineTo(625, 995); path.lineTo(150, 995); path.close(); path(c, green, path);
        line(c, Color.rgb(43, 138, 89), 260, 720, 300, 980, 15); line(c, Color.rgb(43, 138, 89), 430, 720, 470, 980, 15);
        rounded(c, yellow, 610, 790, 895, 1055, 24);
        rounded(c, sky, 690, 730, 810, 820, 12);
        rounded(c, Color.rgb(48, 60, 71), 130, 1010, 925, 1095, 18);
        baseWheels(c, 320, 770, 1100);
    }

    private void drawTanker(Canvas c) {
        oval(c, Color.rgb(225, 229, 232), 145, 690, 655, 1005);
        rounded(c, Color.rgb(158, 169, 178), 190, 680, 595, 720, 15);
        line(c, Color.rgb(158, 169, 178), 285, 715, 285, 985, 12);
        line(c, Color.rgb(158, 169, 178), 515, 715, 515, 985, 12);
        rounded(c, orange, 625, 790, 900, 1055, 24);
        rounded(c, sky, 700, 730, 815, 820, 12);
        rounded(c, Color.rgb(48, 60, 71), 125, 1010, 925, 1095, 18);
        baseWheels(c, 325, 765, 1100);
    }

    private void drawDeliveryTruck(Canvas c) {
        rounded(c, Color.rgb(132, 91, 180), 130, 700, 610, 1035, 28);
        text(c, "PAKET", 370, 880, 55, Color.WHITE, true, Paint.Align.CENTER);
        rounded(c, orange, 610, 790, 905, 1060, 24);
        path.reset(); path.moveTo(675, 700); path.lineTo(810, 700); path.lineTo(905, 815); path.lineTo(675, 815); path.close(); path(c, orange, path);
        rounded(c, sky, 705, 730, 810, 808, 10);
        circle(c, yellow, 870, 990, 35);
        rounded(c, Color.rgb(48, 60, 71), 120, 1010, 930, 1095, 18);
        baseWheels(c, 315, 775, 1100);
    }

    private void drawVehicleChoice(Canvas c) {
        // automobil
        rounded(c, Color.rgb(82, 156, 220), 105, 850, 360, 1015, 35);
        path.reset(); path.moveTo(160, 850); path.lineTo(215, 775); path.lineTo(310, 775); path.lineTo(350, 850); path.close(); path(c, Color.rgb(82, 156, 220), path);
        circle(c, navy, 165, 1020, 48); circle(c, navy, 310, 1020, 48);
        // kamion u sredini
        rounded(c, orange, 405, 735, 650, 1010, 25);
        rounded(c, yellow, 650, 830, 835, 1010, 20);
        rounded(c, sky, 690, 775, 785, 850, 9);
        circle(c, navy, 485, 1025, 58); circle(c, navy, 740, 1025, 58);
        // autobus
        rounded(c, green, 865, 720, 995, 1015, 28);
        for (int i = 0; i < 3; i++) rounded(c, sky, 880, 750 + i * 70, 980, 800 + i * 70, 8);
        circle(c, navy, 900, 1020, 45); circle(c, navy, 965, 1020, 45);
        text(c, "AUTO", 230, 1145, 25, navy, true, Paint.Align.CENTER);
        text(c, "KAMION", 620, 1145, 25, navy, true, Paint.Align.CENTER);
        text(c, "AUTOBUS", 930, 1145, 25, navy, true, Paint.Align.CENTER);
    }

    private boolean isTarget(float x, float y) {
        switch (scene) {
            case 0: return inCircle(x, y, 330, 1100, 120) || inCircle(x, y, 760, 1100, 120);
            case 1: return inRect(x, y, 600, 680, 935, 1080);
            case 2: return inRect(x, y, 125, 720, 590, 1040);
            case 3: return inRect(x, y, 210, 540, 855, 720);
            case 4: return inCircle(x, y, 430, 840, 245);
            case 5: return inCircle(x, y, 820, 920, 100);
            case 6: return inRect(x, y, 120, 650, 630, 1030);
            case 7: return inRect(x, y, 120, 650, 680, 1030);
            case 8: return inCircle(x, y, 870, 990, 70);
            default: return inRect(x, y, 390, 690, 850, 1100);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) return true;
        float x = (event.getX() - offsetX) / scale;
        float y = (event.getY() - offsetY) / scale;
        if (inCircle(x, y, 970, screen == GAME ? 87 : 100, 65)) {
            soundEnabled = !soundEnabled;
            if (!soundEnabled) activity.speak(""); else activity.speak("Zvuk je uključen.");
            invalidate();
            return true;
        }
        if (screen == HOME) {
            if (inRect(x, y, 150, 1420, 930, 1680)) startGame();
        } else if (screen == COMPLETE) {
            if (inRect(x, y, 150, 1240, 930, 1500)) startGame();
        } else {
            if (inCircle(x, y, 82, 87, 65)) { goHome(); return true; }
            if (inCircle(x, y, 150, 325, 80) || (!answered && inRect(x, y, 260, 1580, 820, 1780))) {
                activity.speak(QUESTIONS[scene]); return true;
            }
            if (answered && inRect(x, y, 70, 1570, 1010, 1815)) {
                nextScene(); return true;
            }
            if (!answered && y > 475 && y < 1395) {
                if (isTarget(x, y)) correct(); else wrong();
            }
        }
        return true;
    }

    private void startGame() {
        screen = GAME; scene = 0; stars = 0; answered = false; celebration = 0;
        invalidate(); postDelayed(new Runnable() {
            @Override public void run() { activity.speak(QUESTIONS[scene]); }
        }, 350);
    }

    private void correct() {
        answered = true; stars++; activity.successSound(); activity.speak(ANSWERS[scene]);
        ValueAnimator a = ValueAnimator.ofFloat(0, 1);
        a.setDuration(650); a.setInterpolator(new OvershootInterpolator());
        a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                celebration = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        a.start();
    }

    private void wrong() {
        activity.wrongSound(); activity.speak("Pokušaj ponovo.");
        ValueAnimator a = ValueAnimator.ofFloat(0, -20, 20, -13, 13, 0);
        a.setDuration(380);
        a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                shake = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        a.start();
    }

    private void nextScene() {
        if (scene == QUESTIONS.length - 1) {
            screen = COMPLETE; activity.speak("Bravo! Završio si knjigu i osvojio deset zvezdica!");
        } else {
            scene++; answered = false; celebration = 0; invalidate();
            postDelayed(new Runnable() {
                @Override public void run() { activity.speak(QUESTIONS[scene]); }
            }, 280);
        }
        invalidate();
    }

    private void drawConfetti(Canvas c) {
        int[] colors = {orange, yellow, green, red, Color.rgb(91, 132, 210)};
        p.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 26; i++) {
            float x = 55 + random.nextInt(970);
            float y = screen == COMPLETE ? 180 + random.nextInt(1450) : 1540 + random.nextInt(300);
            float s = (8 + random.nextInt(10)) * Math.max(.15f, celebration);
            p.setColor(colors[i % colors.length]);
            c.save(); c.rotate(i * 27, x, y); c.drawRect(x - s, y - s / 2, x + s, y + s / 2, p); c.restore();
        }
    }

    private void drawSoundButton(Canvas c, float x, float y) {
        circle(c, soundEnabled ? orange : Color.rgb(130, 145, 158), x, y, 48);
        drawSpeaker(c, x, y, Color.WHITE);
        if (!soundEnabled) line(c, Color.WHITE, x - 28, y - 28, x + 28, y + 28, 8);
    }

    private void drawSpeaker(Canvas c, float x, float y, int color) {
        p.setColor(color); p.setStyle(Paint.Style.FILL);
        c.drawRect(x - 30, y - 16, x - 12, y + 16, p);
        path.reset(); path.moveTo(x - 12, y - 16); path.lineTo(x + 10, y - 35); path.lineTo(x + 10, y + 35); path.lineTo(x - 12, y + 16); path.close(); c.drawPath(path, p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(7); c.drawArc(new RectF(x - 2, y - 28, x + 45, y + 28), -50, 100, false, p); p.setStyle(Paint.Style.FILL);
    }

    private void drawCloud(Canvas c, float x, float y, float s) {
        circle(c, Color.WHITE, x - 55 * s, y, 48 * s); circle(c, Color.WHITE, x, y - 25 * s, 65 * s); circle(c, Color.WHITE, x + 65 * s, y, 48 * s);
        rounded(c, Color.WHITE, x - 105 * s, y - 5 * s, x + 115 * s, y + 48 * s, 25 * s);
    }

    private void dashedRoad(Canvas c, float y) {
        for (int i = 0; i < 7; i++) rounded(c, Color.WHITE, 115 + i * 145, y, 200 + i * 145, y + 16, 8);
    }

    private void star(Canvas c, float x, float y, float r, int color) {
        path.reset();
        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5;
            float rr = i % 2 == 0 ? r : r * .43f;
            float px = x + (float) Math.cos(a) * rr, py = y + (float) Math.sin(a) * rr;
            if (i == 0) path.moveTo(px, py); else path.lineTo(px, py);
        }
        path.close(); path(c, color, path);
    }

    private void fitText(Canvas c, String value, float x, float y, float maxWidth, float size, int color) {
        p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); p.setTextSize(size);
        if (p.measureText(value) <= maxWidth) { text(c, value, x, y, size, color, true, Paint.Align.LEFT); return; }
        int split = value.lastIndexOf(' ', value.length() / 2);
        if (split < 1) split = value.indexOf(' ', value.length() / 2);
        text(c, value.substring(0, split), x, y - 20, size - 3, color, true, Paint.Align.LEFT);
        text(c, value.substring(split + 1), x, y + 32, size - 3, color, true, Paint.Align.LEFT);
    }

    private void text(Canvas c, String s, float x, float y, float size, int color, boolean bold, Paint.Align align) {
        p.setStyle(Paint.Style.FILL); p.setColor(color); p.setTextSize(size); p.setTextAlign(align);
        p.setTypeface(Typeface.create(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL));
        c.drawText(s, x, y, p);
    }

    private void fill(Canvas c, int color, float l, float t, float r, float b) { p.setStyle(Paint.Style.FILL); p.setColor(color); c.drawRect(l, t, r, b, p); }
    private void rounded(Canvas c, int color, float l, float t, float r, float b, float radius) { p.setStyle(Paint.Style.FILL); p.setColor(color); c.drawRoundRect(new RectF(l, t, r, b), radius, radius, p); }
    private void circle(Canvas c, int color, float x, float y, float radius) { p.setStyle(Paint.Style.FILL); p.setColor(color); c.drawCircle(x, y, radius, p); }
    private void oval(Canvas c, int color, float l, float t, float r, float b) { p.setStyle(Paint.Style.FILL); p.setColor(color); c.drawOval(new RectF(l, t, r, b), p); }
    private void ovalStroke(Canvas c, int color, float l, float t, float r, float b, float width) { p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(width); p.setColor(color); c.drawOval(new RectF(l, t, r, b), p); p.setStyle(Paint.Style.FILL); }
    private void line(Canvas c, int color, float x1, float y1, float x2, float y2, float width) { p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(width); p.setColor(color); c.drawLine(x1, y1, x2, y2, p); p.setStyle(Paint.Style.FILL); }
    private void arc(Canvas c, int color, float l, float t, float r, float b, float start, float sweep, float width) { p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(width); p.setColor(color); c.drawArc(new RectF(l, t, r, b), start, sweep, false, p); p.setStyle(Paint.Style.FILL); }
    private void path(Canvas c, int color, Path value) { p.setStyle(Paint.Style.FILL); p.setColor(color); c.drawPath(value, p); }
    private void triangle(Canvas c, int color, float x1, float y1, float x2, float y2, float x3, float y3) { path.reset(); path.moveTo(x1, y1); path.lineTo(x2, y2); path.lineTo(x3, y3); path.close(); path(c, color, path); }
    private boolean inRect(float x, float y, float l, float t, float r, float b) { return x >= l && x <= r && y >= t && y <= b; }
    private boolean inCircle(float x, float y, float cx, float cy, float r) { float dx = x - cx, dy = y - cy; return dx * dx + dy * dy <= r * r; }
}
