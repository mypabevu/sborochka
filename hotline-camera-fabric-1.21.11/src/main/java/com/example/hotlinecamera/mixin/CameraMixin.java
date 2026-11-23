package com.example.hotlinecamera.mixin;

import com.example.hotlinecamera.HotlineCameraClient;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow protected abstract void setPosition(double x, double y, double z);

    @Inject(method = "setup", at = @At("TAIL"))
    private void hotlinecamera$overhead(BlockGetter level, Entity entity, boolean detached,
                                        boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        if (!HotlineCameraClient.topDown || !detached) return;
        double x = entity.xOld + (entity.getX() - entity.xOld) * partialTick;
        double y = entity.yOld + (entity.getY() - entity.yOld) * partialTick;
        double z = entity.zOld + (entity.getZ() - entity.zOld) * partialTick;
        setRotation(0.0f, 90.0f);
        setPosition(x, y + 14.0, z);
    }
}
