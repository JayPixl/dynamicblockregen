package io.jaypixl.dynamicblockregen.fabric;

import io.jaypixl.dynamicblockregen.commands.ModCommands;
import io.jaypixl.dynamicblockregen.events.ModEventHandler;
import io.jaypixl.dynamicblockregen.fabric.data.RegenPoolReloadListenerFabric;
import io.jaypixl.dynamicblockregen.level.RegenBlockSavedData;
import io.jaypixl.dynamicblockregen.level.RegenScheduler;
import net.fabricmc.api.ModInitializer;

import io.jaypixl.dynamicblockregen.DBRMod;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionResult;

import java.util.concurrent.atomic.AtomicBoolean;

public final class DBRModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Server tick
        ServerTickEvents.END_SERVER_TICK.register(RegenScheduler::tick);

        // Server started
        ServerLifecycleEvents.SERVER_STARTED.register(ModEventHandler::handleServerStarted);

        // Right click block
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            if (!(world instanceof ServerLevel level))
                return InteractionResult.PASS;

            if (!(player instanceof ServerPlayer serverPlayer))
                return InteractionResult.PASS;

            AtomicBoolean cancelled = new AtomicBoolean(false);

            ModEventHandler.handleUseItem(
                    level,
                    hitResult.getBlockPos(),
                    serverPlayer,
                    player.getItemInHand(hand),
                    () -> { cancelled.set(true); }
            );
            if (cancelled.get()) {
                return InteractionResult.SUCCESS_NO_ITEM_USED;
            } else {
                return InteractionResult.PASS;
            }
        });

        // Commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ModCommands.register(dispatcher);
        });

        // Block Break Listener
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {

            if (!(world instanceof ServerLevel level))
                return true;

            RegenBlockSavedData data = RegenBlockSavedData.get(level);

            if (!data.isRegenBlock(pos))
                return true;

            ModEventHandler.handleBlockBreak(level, pos, player);

            return false; // cancels normal break
        });

        // Datapack reload listener
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RegenPoolReloadListenerFabric());

        // Run Common Setup
        DBRMod.init();
    }
}
