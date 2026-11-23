package com.example.hotlinecamera.mixin;

import com.example.hotlinecamera.HotlineCameraClient;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private Entity entity;

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Inject(method = "setup", at = @At("TAIL"))
    private void hotlinecamera$overhead(CallbackInfo ci) {
        if (!HotlineCameraClient.topDown || this.entity == null) {
            return;
        }

        setRotation(0.0f, 90.0f);

        setPosition(
            this.entity.getX(),
            this.entity.getY() + 14.0,
            this.entity.getZ()
        );
    }
}
