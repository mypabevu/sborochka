package dev.victor.livecamera;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class LiveCameraClient implements ClientModInitializer {
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("livecamera", "main"));

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.livecamera.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                LiveCameraState.enabled = !LiveCameraState.enabled;
                if (client.player != null) {
                    client.player.sendMessage(Text.translatable(
                            LiveCameraState.enabled ? "livecamera.enabled" : "livecamera.disabled"), true);
                }
            }
        });
    }
}
