package io.jaypixl.dynamicblockregen.level;

import io.jaypixl.dynamicblockregen.data.RegenEntry;
import io.jaypixl.dynamicblockregen.data.RegenPools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegenScheduler {

    private static final Map<Long, List<RegenTask>> TASKS = new HashMap<>();

    public static void schedule(ServerLevel level, BlockPos pos, RegenEntry entry, int delay) {

        long executeTick = level.getServer().getTickCount() + delay;

        TASKS
                .computeIfAbsent(executeTick, t -> new ArrayList<>())
                .add(new RegenTask(level.dimension(), pos.immutable(), entry));
    }

    public static void regen(ServerLevel level, BlockPos pos, String poolId) {

        RegenPools.Pool pool = RegenPools.getPool(poolId);

        level.setBlock(pos, pool.getConfig().getPlaceholder(), 3);

        RegenEntry entry = RegenPools.getWeightedRandom(level.random, RegenPools.get(poolId));

        int min = entry.regenTime().getFirst();
        int max = entry.regenTime().getSecond();

        int delay = level.random.nextInt(max - min + 1) + min;
        schedule(level, pos, entry, delay);
    }

    public static void tick(MinecraftServer server) {

        long tick = server.getTickCount();

        List<RegenTask> tasks = TASKS.remove(tick);

        if (tasks == null)
            return;

        for (RegenTask task : tasks) {

            ServerLevel level = server.getLevel(task.dimension());

            if (level == null)
                continue;

            if (!level.isLoaded(task.pos()))
                continue;

            placeBlock(level, task);
        }
    }

    private static void placeBlock(ServerLevel level, RegenTask task) {

        RegenEntry entry = task.entry();

        BlockState state = entry.state();

        level.setBlock(task.pos(), state, 3);
    }
}