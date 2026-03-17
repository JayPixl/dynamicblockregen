package io.jaypixl.dynamicblockregen.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.jaypixl.dynamicblockregen.level.RegenBlockSavedData;
import io.jaypixl.dynamicblockregen.level.RegenScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;

public class RegenPools {

    private static Map<String, Pool> POOLS = Map.of();

    public static List<RegenEntry> get(String pool) {
        Pool p = POOLS.get(pool);
        return p != null ? p.entries() : List.of();
    }

    public static Pool getPool(String pool) {
        return POOLS.get(pool);
    }

    public static void replace(Map<String, Pool> newPools) {
        POOLS = Map.copyOf(newPools);
    }

    public static boolean exists(String pool) {
        return POOLS.containsKey(pool);
    }

    public static Map<String, Pool> getAll() {
        return POOLS;
    }

    public static final String DEFAULT_POOL = "default";

    // --- logic below unchanged except using entries() ---

    public static void refreshPools(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            RegenBlockSavedData data = RegenBlockSavedData.get(level);

            for (var entry : data.getAll().entrySet()) {
                validateBlock(level, entry.getKey(), entry.getValue());
            }
        }
    }

    public static void restartPools(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            RegenBlockSavedData data = RegenBlockSavedData.get(level);

            for (var entry : data.getAll().entrySet()) {
                RegenScheduler.regen(level, entry.getKey(), entry.getValue());
            }
        }
    }
//    }
//
//    private static Map<String, PoolEntry> POOLS = Map.of();
//
//    public static List<RegenEntry> get(String pool) {
//        return POOLS.getOrDefault(pool, List.of());
//    }
//
//    public static void replace(Map<String, List<RegenEntry>> newPools) {
//        POOLS = Map.copyOf(newPools);
//    }
//
//    public static boolean exists(String pool) {
//        return POOLS.containsKey(pool);
//    }
//
//    public static Map<String, List<RegenEntry>> getAll() {
//        return POOLS;
//    }
//
//    public static String DEFAULT_POOL = "default";
//
    public static RegenEntry getWeightedRandom(RandomSource random, List<RegenEntry> pool) {

        int totalWeight = 0;

        for (RegenEntry entry : pool)
            totalWeight += entry.weight();

        int r = random.nextInt(totalWeight);

        for (RegenEntry entry : pool) {
            r -= entry.weight();

            if (r < 0)
                return entry;
        }

        return pool.getFirst();
    }

    private static void validateBlock(ServerLevel level, BlockPos pos, String poolId) {

        List<RegenEntry> pool = RegenPools.get(poolId);

        if (pool == null || pool.isEmpty())
            return;

        BlockState state = level.getBlockState(pos);

        boolean needsRegen = !isBlockValid(state, pool);

        if (!needsRegen)
            return;

        RegenScheduler.regen(level, pos, poolId);
    }

    private static boolean isBlockValid(BlockState state, List<RegenEntry> pool) {

        for (RegenEntry entry : pool) {

            Block block = entry.state().getBlock();

            if (state.is(block))
                return true;
        }

        return false;
    }

    public static class Pool {

        private final Config config;
        private final List<RegenEntry> entries;

        public Pool(Config config, List<RegenEntry> entries) {
            this.config = config;
            this.entries = entries;
        }

        public Config getConfig() {
            return config;
        }

        public List<RegenEntry> entries() {
            return entries;
        }
    }

    public static class Config {

        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        BuiltInRegistries.BLOCK.byNameCodec()
                                .xmap(Block::defaultBlockState, BlockState::getBlock)
                                .optionalFieldOf("placeholder", Blocks.BEDROCK.defaultBlockState())
                                .forGetter(Config::getPlaceholder)
                ).apply(instance, Config::new)
        );

        private final BlockState placeholder;

        public Config(BlockState placeholder) {
            this.placeholder = placeholder;
        }

        public BlockState getPlaceholder() {
            return placeholder;
        }

        public static Config DEFAULT = new Config(Blocks.BEDROCK.defaultBlockState());
    }
}