package dev.victor.livecamera.mixin;

import dev.victor.livecamera.LiveCameraState;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private Vec3 position;
    @Shadow private float xRot;
    @Shadow private float yRot;

    @Inject(method = "setup", at = @At("TAIL"))
    private void livecamera$setup(BlockGetter level, Entity entity, boolean detached,
                                  boolean mirrored, float partialTick, CallbackInfo ci) {
        if (!LiveCameraState.enabled || !detached || mirrored) {
            if (!detached) LiveCameraState.reset();
            return;
        }

        LiveCameraState.update(entity, partialTick);

        double yawRad = Math.toRadians(this.yRot);
        double pitchRad = Math.toRadians(this.xRot);

        Vec3 right = new Vec3(-Math.cos(yawRad), 0.0, -Math.sin(yawRad));
        Vec3 forward = new Vec3(
                -Math.sin(yawRad) * Math.cos(pitchRad),
                -Math.sin(pitchRad),
                Math.cos(yawRad) * Math.cos(pitchRad));

        Vec3 head = entity.getEyePosition(partialTick).add(0.0, LiveCameraState.breath(), 0.0);

        Vec3 sidePos = clip(level, entity, head, head.add(right.scale(LiveCameraState.side())));
        Vec3 finalPos = clip(level, entity, sidePos, sidePos.subtract(forward.scale(LiveCameraState.distance())));

        this.position = finalPos;
    }

    private static Vec3 clip(BlockGetter level, Entity entity, Vec3 from, Vec3 to) {
        BlockHitResult hit = level.clip(new ClipContext(
                from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));
        if (hit.getType() == HitResult.Type.MISS) return to;

        Vec3 delta = hit.getLocation().subtract(from);
        double len = delta.length();
        if (len < 1.0e-4) return from;
        double keep = Math.max(0.0, len - 0.25) / len;
        return from.add(delta.scale(keep));
    }
}
