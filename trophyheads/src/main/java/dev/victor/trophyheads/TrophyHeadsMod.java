package dev.victor.trophyheads;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * После смерти игрока из него выпадает голова-трофей с названием "Трофей головы <ник>".
 * Работает на сервере — игрокам ставить мод не нужно.
 */
public class TrophyHeadsMod implements ModInitializer {
    // ===================== SETTINGS =====================
    /** true = голова выпадает только если игрока убил другой игрок (PvP). */
    public static final boolean ONLY_PLAYER_KILLS = false;
    /** Добавлять в описание предмета строку "Убит: <кем>". */
    public static final boolean SHOW_KILLER_LORE = true;
    // ====================================================

    @Override
    public void onInitialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;

            Entity attacker = damageSource.getAttacker();
            if (ONLY_PLAYER_KILLS && !(attacker instanceof ServerPlayerEntity)) return;

            ServerWorld world = (ServerWorld) player.getEntityWorld();
            player.dropStack(world, createTrophy(player, attacker));
        });
    }

    private static ItemStack createTrophy(ServerPlayerEntity victim, Entity attacker) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);

        // Скин игрока на голове
        head.set(DataComponentTypes.PROFILE, ProfileComponent.ofStatic(victim.getGameProfile()));

        // Название: "Трофей головы <ник>"
        String nick = victim.getName().getString();
        MutableText name = Text.literal("Трофей головы ").formatted(Formatting.GOLD)
                .append(Text.literal(nick).formatted(Formatting.YELLOW))
                .styled(style -> style.withItalic(false));
        head.set(DataComponentTypes.CUSTOM_NAME, name);

        // Описание: кем убит
        if (SHOW_KILLER_LORE && attacker != null) {
            Text lore = Text.literal("Убит: ").formatted(Formatting.GRAY)
                    .append(attacker.getName().copy().formatted(Formatting.RED))
                    .styled(style -> style.withItalic(false));
            head.set(DataComponentTypes.LORE, new LoreComponent(List.of(lore)));
        }

        return head;
    }
}
