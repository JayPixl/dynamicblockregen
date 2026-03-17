package io.jaypixl.dynamicblockregen.events;

import io.jaypixl.dynamicblockregen.data.RegenEntry;
import io.jaypixl.dynamicblockregen.data.RegenPools;
import io.jaypixl.dynamicblockregen.item.RegenWandUtil;
import io.jaypixl.dynamicblockregen.level.RegenBlockSavedData;
import io.jaypixl.dynamicblockregen.level.RegenScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class ModEventHandler {
    public static void handleServerStarted(MinecraftServer server) {
        RegenPools.refreshPools(server);
    }

    public static void handleBlockBreak(ServerLevel level, BlockPos pos, Player player) {

        RegenBlockSavedData data = RegenBlockSavedData.get(level);

        String pool = data.getPool(pos);

        //player.sendSystemMessage(Component.literal("BLOCK BROKEN IN POOL " + pool));

        level.destroyBlock(pos, true, player);

        RegenScheduler.regen(level, pos, pool);
    }

    public static void handleUseItem(ServerLevel level, BlockPos pos, ServerPlayer player, ItemStack stack) {

        String pool = RegenWandUtil.getPool(stack);
        if (pool == null) return;

        if (!player.hasPermissions(2)) {
            player.sendSystemMessage(Component.literal("You do not have permission to use this!"));
            return;
        }

        RegenBlockSavedData data = RegenBlockSavedData.get(level);

        if (data.isRegenBlock(pos)) {

            String prevPool = data.getPool(pos);
            data.remove(pos);

            player.sendSystemMessage(
                    Component.literal("Removed position " + pos + " from pool " + prevPool)
            );

        } else {

            data.add(pos, pool);

            player.sendSystemMessage(
                    Component.literal("Added position " + pos + " to pool " + pool)
            );
        }
    }
}
