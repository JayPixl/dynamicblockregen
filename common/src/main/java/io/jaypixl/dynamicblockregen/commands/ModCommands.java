package io.jaypixl.dynamicblockregen.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.jaypixl.dynamicblockregen.data.RegenPools;
import io.jaypixl.dynamicblockregen.item.RegenWandUtil;
import io.jaypixl.dynamicblockregen.level.RegenBlockSavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("dbr")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("wand")
                            .then(Commands.literal("set")
                                    .then(Commands.argument("pool", StringArgumentType.word())
                                            .executes(ModCommands::setWand)
                                    )
                            )
                            .then(Commands.literal("get")
                                    .executes(ModCommands::getWandSimple)
                                    .then(Commands.argument("pool", StringArgumentType.word())
                                            .executes(ModCommands::getWandPool)
                                    )
                            )
                        )
                        .then(Commands.literal("pools")
                                .executes(ModCommands::listPools)
                                .then(Commands.literal("list")
                                        .executes(ModCommands::listPools)
                                )
                                .then(Commands.literal("refresh")
                                        .executes(ModCommands::refreshPools)
                                )
                                .then(Commands.literal("restart")
                                        .executes(ModCommands::restartPools)
                                )
                                .then(Commands.literal("blocks")
                                        .then(Commands.argument("pool", StringArgumentType.word())
                                                .executes(ModCommands::listPoolBlockLocations)
                                        )
                                )
                                .then(Commands.literal("clear")
                                        .then(Commands.argument("pool", StringArgumentType.word())
                                                .executes(ModCommands::clearPool)
                                        )
                                )
                        )
        );
    }

    private static int setWand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();

        String poolName = StringArgumentType.getString(ctx, "pool");

        if (!RegenPools.exists(poolName)) {
            source.sendFailure(Component.literal("Pool does not exist."));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();

        if (!RegenWandUtil.isWand(stack)) {
            source.sendFailure(Component.literal("Hold a Regen Marker Wand."));
            return 0;
        }

        CompoundTag tag = new CompoundTag();

        tag.putString(RegenWandUtil.TAG_NAME, poolName);

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        source.sendSuccess(
                () -> Component.literal("Wand set to pool: " + poolName),
                false
        );

        return 1;
    }

    private static int getWandSimple(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();

        if (!player.addItem(RegenWandUtil.getWand())) {
            source.sendFailure(Component.literal("Error giving you a wand!"));
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("Regen Marker Wand set to pool " + RegenPools.DEFAULT_POOL),
                false
        );
        return 1;
    }

    private static int getWandPool(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();

        String poolName = StringArgumentType.getString(ctx, "pool");

        if (!RegenPools.exists(poolName)) {
            source.sendFailure(Component.literal("Pool does not exist."));
            return 0;
        }

        if (!player.addItem(RegenWandUtil.getWand(poolName))) {
            source.sendFailure(Component.literal("Error giving you a wand!"));
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("Regen Marker Wand set to pool " + poolName),
                false
        );
        return 1;
    }

    private static int refreshPools(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        RegenPools.refreshPools(ctx.getSource().getServer());

        source.sendSuccess(
                () -> Component.literal("Refreshed pools!"),
                false
        );
        return 1;
    }

    private static int restartPools(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        RegenPools.restartPools(ctx.getSource().getServer());

        source.sendSuccess(
                () -> Component.literal("Restarted pools!"),
                false
        );
        return 1;
    }

    private static int listPools(CommandContext<CommandSourceStack> ctx) {
        var pools = RegenPools.getAll();

        ctx.getSource().sendSuccess(
                () -> Component.literal("Current pools: " + pools.keySet()),
                false
        );

        return pools.size();
    }

    private static int listPoolBlockLocations(CommandContext<CommandSourceStack> ctx) {
        String pool = StringArgumentType.getString(ctx, "pool");

        ServerLevel level = ctx.getSource().getLevel();
        RegenBlockSavedData data = RegenBlockSavedData.get(level);

        if (!RegenPools.exists(pool)) {
            ctx.getSource().sendFailure(Component.literal("Pool does not exist."));
            return 0;
        }

        int count = 0;

        for (var entry : data.getAll().entrySet()) {
            if (entry.getValue().equals(pool)) {
                ctx.getSource().sendSuccess(
                        () -> Component.literal(entry.getKey().toShortString()),
                        false
                );
                count++;
            }
        }

        int finalCount = count;
        ctx.getSource().sendSuccess(
                () -> Component.literal("Total: " + finalCount),
                false
        );

        return count;
    }

    private static int clearPool(CommandContext<CommandSourceStack> ctx) {
        String pool = StringArgumentType.getString(ctx, "pool");

        ServerLevel level = ctx.getSource().getLevel();
        RegenBlockSavedData data = RegenBlockSavedData.get(level);

        int removed = data.removePool(pool);

        ctx.getSource().sendSuccess(
                () -> Component.literal("Removed " + removed + " blocks from pool " + pool),
                true
        );

        return removed;
    }
}
