package dev.victor.livecamera;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Состояние и настройки "живой" камеры. Все числа для подстройки — в блоке SETTINGS.
 */
public final class LiveCameraState {
    // ===================== SETTINGS =====================
    /** ГЛАВНЫЙ РЕГУЛЯТОР живости: 0 = почти ваниль, 1 = по умолчанию, 1.5-2 = очень живо. */
    public static final double LIVELINESS = 1.0;

    /** Смещение камеры вправо (в блоках), когда смотришь прямо. */
    public static final double SHOULDER_OFFSET = 0.8;
    /** Смещение при резком повороте мыши влево (0 = за спиной, <0 = левее). */
    public static final double LEFT_TURN_OFFSET = -0.2;
    /** Насколько дальше уходит вправо при повороте мыши вправо. */
    public static final double RIGHT_TURN_EXTRA = 0.7;
    /** Скорость поворота (град/сек), при которой эффект максимальный. */
    public static final double FULL_EFFECT_SPEED = 180.0;

    /** "Пружина" бокового смещения: жёсткость и демпфирование (меньше демпфирование = больше покачивания). */
    public static final double SPRING_STIFFNESS = 55.0;
    public static final double SPRING_DAMPING = 8.0;

    /** Запаздывание камеры по повороту: градусов на каждый град/сек и максимум в градусах. */
    public static final double YAW_LAG_FACTOR = 0.03;
    public static final double MAX_YAW_LAG = 10.0;

    /** Дистанция камеры (в ваниле 4.0) и добавка при беге. */
    public static final double BASE_DISTANCE = 4.0;
    public static final double SPRINT_EXTRA_DISTANCE = 1.0;

    /** Покачивание при ходьбе (вертикаль и бока), "дыхание" в покое, отставание при прыжке/падении. */
    public static final double WALK_BOB_VERTICAL = 0.06;
    public static final double WALK_BOB_SIDE = 0.05;
    public static final double BREATH_AMPLITUDE = 0.03;
    public static final double FALL_LAG = 0.45;
    // ====================================================

    public static boolean enabled = true;

    private static boolean initialized = false;
    private static float lastYaw;
    private static long lastNanos;
    private static double time;

    private static double turnSpeed;
    private static double side = SHOULDER_OFFSET, sideVel;
    private static double yawLag, yawLagVel;
    private static double extraDistance;
    private static double moveFactor, bobPhase;
    private static double fallOffset;

    private static double sideBob, verticalBob, breath;

    /** Итоговое боковое смещение (вправо), в блоках. */
    public static double side() { return side + sideBob; }
    /** Дистанция камеры назад. */
    public static double distance() { return BASE_DISTANCE + extraDistance; }
    /** Вертикальное смещение (дыхание + шаги + прыжок/падение). */
    public static double vertical() { return breath + verticalBob + fallOffset; }
    /** Добавка к повороту камеры (градусы) — камера чуть "отстаёт" при повороте. */
    public static float yawLag() { return (float) yawLag; }

    /** Вызывается один раз за кадр из CameraMixin. */
    public static void update(Entity e, float tickDelta) {
        long now = System.nanoTime();
        float yaw = e.getYaw(tickDelta);

        if (!initialized) {
            initialized = true;
            lastYaw = yaw;
            lastNanos = now;
        }

        double dt = MathHelper.clamp((now - lastNanos) / 1.0e9, 0.0005, 0.1);
        lastNanos = now;
        time += dt;
        double L = LIVELINESS;

        // В майнкрафте: мышь вправо -> yaw растёт, влево -> уменьшается.
        double instSpeed = MathHelper.clamp(MathHelper.wrapDegrees(yaw - lastYaw) / dt, -1500.0, 1500.0);
        lastYaw = yaw;
        turnSpeed += (instSpeed - turnSpeed) * (1.0 - Math.exp(-dt * 12.0));

        double t = MathHelper.clamp(turnSpeed / FULL_EFFECT_SPEED, -1.0, 1.0);
        double target;
        if (t < 0) { // влево -> камера "встаёт" за спину персонажа
            double k = -t;
            target = SHOULDER_OFFSET * (1.0 - k) + LEFT_TURN_OFFSET * k;
        } else {     // вправо -> ещё дальше за плечо
            target = SHOULDER_OFFSET + RIGHT_TURN_EXTRA * L * t;
        }
        double lagTarget = MathHelper.clamp(-turnSpeed * YAW_LAG_FACTOR * L, -MAX_YAW_LAG, MAX_YAW_LAG);

        // Пружины (с подшагами для устойчивости)
        int steps = Math.max(1, (int) Math.ceil(dt / 0.01));
        double h = dt / steps;
        for (int i = 0; i < steps; i++) {
            sideVel += (SPRING_STIFFNESS * (target - side) - SPRING_DAMPING * sideVel) * h;
            side += sideVel * h;
            yawLagVel += (70.0 * (lagTarget - yawLag) - 10.0 * yawLagVel) * h;
            yawLag += yawLagVel * h;
        }

        // Движение: ходьба/бег
        Vec3d vel = e.getVelocity();
        double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z); // блоков за тик
        double moveTarget = e.isOnGround() ? MathHelper.clamp(hSpeed / 0.22, 0.0, 1.3) : 0.3;
        moveFactor += (moveTarget - moveFactor) * (1.0 - Math.exp(-dt * 6.0));
        bobPhase += dt * 11.0 * moveFactor;

        verticalBob = Math.sin(bobPhase * 2.0) * WALK_BOB_VERTICAL * moveFactor * L;
        sideBob = Math.sin(bobPhase) * WALK_BOB_SIDE * moveFactor * L;
        breath = Math.sin(time * 1.6) * BREATH_AMPLITUDE * L * (1.0 - Math.min(1.0, moveFactor));

        // Прыжок/падение: камера чуть отстаёт по вертикали
        double fallTarget = MathHelper.clamp(-vel.y * FALL_LAG * L, -0.4, 0.6);
        fallOffset += (fallTarget - fallOffset) * (1.0 - Math.exp(-dt * 8.0));

        // Бег -> камера отъезжает
        double distTarget = e.isSprinting() ? SPRINT_EXTRA_DISTANCE * L : 0.0;
        extraDistance += (distTarget - extraDistance) * (1.0 - Math.exp(-dt * 3.0));
    }

    public static void reset() {
        initialized = false;
        turnSpeed = 0;
        side = SHOULDER_OFFSET; sideVel = 0;
        yawLag = 0; yawLagVel = 0;
        extraDistance = 0; moveFactor = 0; fallOffset = 0;
        sideBob = 0; verticalBob = 0; breath = 0;
    }

    private LiveCameraState() {}
}
