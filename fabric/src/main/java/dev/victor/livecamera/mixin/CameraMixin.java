package dev.victor.livecamera.mixin;

import dev.victor.livecamera.LiveCameraState;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private Vec3d pos;
    @Shadow private float yaw;
    @Shadow private float pitch;

    @Inject(method = "update", at = @At("TAIL"))
    private void livecamera$update(BlockView area, Entity focusedEntity, boolean thirdPerson,
                                   boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (!LiveCameraState.enabled || !thirdPerson || inverseView) {
            // первое лицо или вид спереди — не вмешиваемся, но сбрасываем состояние
            if (!thirdPerson) LiveCameraState.reset();
            return;
        }

        LiveCameraState.update(focusedEntity, tickDelta);

        double yawRad = Math.toRadians(this.yaw);
        double pitchRad = Math.toRadians(this.pitch);

        // Вектор "вправо" относительно взгляда и вектор взгляда
        Vec3d right = new Vec3d(-Math.cos(yawRad), 0.0, -Math.sin(yawRad));
        Vec3d forward = new Vec3d(
                -Math.sin(yawRad) * Math.cos(pitchRad),
                -Math.sin(pitchRad),
                Math.cos(yawRad) * Math.cos(pitchRad));

        Vec3d head = focusedEntity.getCameraPosVec(tickDelta).add(0.0, LiveCameraState.breath(), 0.0);

        // 1) сдвиг в сторону (с защитой от стен), 2) отъезд назад (с защитой от стен)
        Vec3d sidePos = clip(area, focusedEntity, head, head.add(right.multiply(LiveCameraState.side())));
        Vec3d finalPos = clip(area, focusedEntity, sidePos, sidePos.subtract(forward.multiply(LiveCameraState.distance())));

        this.pos = finalPos;
    }

    private static Vec3d clip(BlockView area, Entity entity, Vec3d from, Vec3d to) {
        BlockHitResult hit = area.raycast(new RaycastContext(
                from, to, RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, entity));
        if (hit.getType() == HitResult.Type.MISS) return to;

        Vec3d delta = hit.getPos().subtract(from);
        double len = delta.length();
        if (len < 1.0e-4) return from;
        double keep = Math.max(0.0, len - 0.25) / len; // небольшой отступ от стены
        return from.add(delta.multiply(keep));
    }
}
