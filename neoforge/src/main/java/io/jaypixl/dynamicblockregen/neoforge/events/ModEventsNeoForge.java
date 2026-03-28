package io.jaypixl.dynamicblockregen.neoforge.events;

import io.jaypixl.dynamicblockregen.commands.ModCommands;
import io.jaypixl.dynamicblockregen.data.RegenEntry;
import io.jaypixl.dynamicblockregen.data.RegenPoolReloadListener;
import io.jaypixl.dynamicblockregen.data.RegenPools;
import io.jaypixl.dynamicblockregen.events.ModEventHandler;
import io.jaypixl.dynamicblockregen.item.RegenWandUtil;
import io.jaypixl.dynamicblockregen.level.RegenBlockSavedData;
import io.jaypixl.dynamicblockregen.level.RegenScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;

public class ModEventsNeoForge {
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        RegenScheduler.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level))
            return;

        BlockPos pos = event.getPos();
        Player player = event.getPlayer();

        RegenBlockSavedData data = RegenBlockSavedData.get(level);
        if (!data.isRegenBlock(pos))
            return;

        event.setCanceled(true);

        ModEventHandler.handleBlockBreak(level, pos, player);
    }


    @SubscribeEvent
    public static void onUseItem(PlayerInteractEvent.RightClickBlock event) {

        if (!(event.getLevel() instanceof ServerLevel level))
            return;

        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        ModEventHandler.handleUseItem(level, event.getPos(), player, event.getItemStack(), () -> event.setCanceled(true));
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        ModEventHandler.handleServerStarted(event.getServer());
    }

    @SubscribeEvent
    public static void registerDataListeners(AddReloadListenerEvent event) {
        event.addListener(new RegenPoolReloadListener());
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}
