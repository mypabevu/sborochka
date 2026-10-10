package dev.victor.topdowncamera.mixin;

import dev.victor.topdowncamera.TopDownState;
import net.minecraft.client.Mouse;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow private double cursorDeltaX;
    @Shadow private double cursorDeltaY;

    /** Вместо поворота головы мышь двигает виртуальный курсор. */
    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void topdown$updateMouse(double timeDelta, CallbackInfo ci) {
        if (!TopDownState.isActive()) return;
        Window w = MinecraftClient.getInstance().getWindow();
        TopDownState.moveCursor(this.cursorDeltaX, this.cursorDeltaY, w.getWidth(), w.getHeight());
        this.cursorDeltaX = 0.0;
        this.cursorDeltaY = 0.0;
        ci.cancel();
    }

    /** Ctrl + колёсико = приближение/отдаление. Обычное колёсико = смена предметов, как в ванили. */
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void topdown$scroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!TopDownState.isActive() || !Screen.hasControlDown()) return;
        if (vertical != 0.0) TopDownState.zoomBy(Math.signum(vertical));
        ci.cancel();
    }
}
