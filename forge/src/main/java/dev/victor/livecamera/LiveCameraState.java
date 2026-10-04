package dev.victor.livecamera;

import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;

/**
 * Состояние и настройки "живой" камеры. Все числа для подстройки — ниже в блоке SETTINGS.
 */
public final class LiveCameraState {
    // ===================== SETTINGS =====================
    /** Насколько камера смещена вправо (в блоках), когда смотришь прямо. */
    public static final double SHOULDER_OFFSET = 0.65;
    /** Смещение, когда резко ведёшь мышь влево (0 = ровно за спиной, <0 = чуть левее). */
    public static final double LEFT_TURN_OFFSET = -0.05;
    /** Насколько дальше уходит вправо при повороте мыши вправо. */
    public static final double RIGHT_TURN_EXTRA = 0.35;
    /** Скорость поворота (град/сек), при которой эффект максимальный. */
    public static final double FULL_EFFECT_SPEED = 300.0;
    /** Скорость сглаживания: к центру (быстро) и обратно к плечу (медленно). */
    public static final double SMOOTH_TO_CENTER = 9.0;
    public static final double SMOOTH_TO_SHOULDER = 3.5;
    /** Базовая дистанция камеры (в ваниле 4.0) и добавка при беге. */
    public static final double BASE_DISTANCE = 4.0;
    public static final double SPRINT_EXTRA_DISTANCE = 0.6;
    /** Амплитуда "дыхания" камеры по вертикали. */
    public static final double BREATH_AMPLITUDE = 0.02;
    // ====================================================

    public static boolean enabled = true;

    private static boolean initialized = false;
    private static float lastYaw;
    private static long lastNanos;
    private static double turnSpeed;   // сглаженная скорость поворота, град/сек (<0 — влево)
    private static double side = SHOULDER_OFFSET;
    private static double extraDistance;
    private static double time;

    public static double side() { return side; }
    public static double distance() { return BASE_DISTANCE + extraDistance; }
    public static double breath() { return Math.sin(time * 1.6) * BREATH_AMPLITUDE; }

    /** Вызывается один раз за кадр из CameraMixin. */
    public static void update(Entity e, float tickDelta) {
        long now = System.nanoTime();
        float yaw = e.getViewYRot(tickDelta);

        if (!initialized) {
            initialized = true;
            lastYaw = yaw;
            lastNanos = now;
        }

        double dt = Mth.clamp((now - lastNanos) / 1.0e9, 0.0005, 0.1);
        lastNanos = now;
        time += dt;

        // В майнкрафте: мышь вправо -> yaw растёт, мышь влево -> yaw уменьшается.
        double instSpeed = Mth.wrapDegrees(yaw - lastYaw) / dt;
        lastYaw = yaw;
        turnSpeed += (instSpeed - turnSpeed) * (1.0 - Math.exp(-dt * 10.0));

        double t = Mth.clamp(turnSpeed / FULL_EFFECT_SPEED, -1.0, 1.0);
        double target;
        if (t < 0) { // ведём влево -> камера "встаёт" за спину персонажа
            double k = -t;
            target = SHOULDER_OFFSET * (1.0 - k) + LEFT_TURN_OFFSET * k;
        } else {     // ведём вправо -> чуть дальше за плечо
            target = SHOULDER_OFFSET + RIGHT_TURN_EXTRA * t;
        }

        double rate = target < side ? SMOOTH_TO_CENTER : SMOOTH_TO_SHOULDER;
        side += (target - side) * (1.0 - Math.exp(-dt * rate));

        double distTarget = e.isSprinting() ? SPRINT_EXTRA_DISTANCE : 0.0;
        extraDistance += (distTarget - extraDistance) * (1.0 - Math.exp(-dt * 3.0));
    }

    public static void reset() {
        initialized = false;
        turnSpeed = 0;
        side = SHOULDER_OFFSET;
        extraDistance = 0;
    }

    private LiveCameraState() {}
}
