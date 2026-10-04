package dev.victor.livecamera;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.lwjgl.glfw.GLFW;

@Mod("livecamera")
public class LiveCameraForge {
    public static KeyMapping toggleKey;

    public LiveCameraForge(FMLJavaModLoadingContext context) {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        context.getModEventBus().addListener(LiveCameraForge::registerKeys);
        MinecraftForge.EVENT_BUS.addListener(LiveCameraForge::onClientTick);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        toggleKey = new KeyMapping("key.livecamera.toggle", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K, "key.categories.livecamera");
        event.register(toggleKey);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || toggleKey == null) return;
        while (toggleKey.consumeClick()) {
            LiveCameraState.enabled = !LiveCameraState.enabled;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.translatable(
                        LiveCameraState.enabled ? "livecamera.enabled" : "livecamera.disabled"), true);
            }
        }
    }
}
