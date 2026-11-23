package com.example.hotlinecamera.mixin;

import com.example.hotlinecamera.HotlineCameraClient;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    // The F5 cycle normally goes FIRST_PERSON -> THIRD_PERSON_BACK -> THIRD_PERSON_FRONT -> FIRST_PERSON.
    // Intercept only the camera change from the vanilla key handler to add a fourth state.
    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/Options;setCameraType(Lnet/minecraft/client/CameraType;)V"))
    private void hotlinecamera$cycle(Options options, CameraType next) {
        if (HotlineCameraClient.topDown) {
            HotlineCameraClient.topDown = false;
            options.setCameraType(CameraType.FIRST_PERSON);
        } else if (options.getCameraType() == CameraType.THIRD_PERSON_FRONT && next == CameraType.FIRST_PERSON) {
            HotlineCameraClient.topDown = true;
            // Third-person keeps the player model visible; CameraMixin overrides the viewpoint.
            options.setCameraType(CameraType.THIRD_PERSON_BACK);
        } else {
            options.setCameraType(next);
        }
    }
}
