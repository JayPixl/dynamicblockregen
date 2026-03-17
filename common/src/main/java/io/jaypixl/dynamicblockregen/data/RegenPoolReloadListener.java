package io.jaypixl.dynamicblockregen.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.jaypixl.dynamicblockregen.DBRMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;import java.util.ArrayList;

import java.util.List;
import java.util.Map;

public class RegenPoolReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();

    public RegenPoolReloadListener() {
        super(GSON, "pools");
    }

    public static final Codec<RegenPools.Pool> POOL_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RegenPools.Config.CODEC.optionalFieldOf("config", RegenPools.Config.DEFAULT)
                            .forGetter(RegenPools.Pool::getConfig),

                    RegenEntry.CODEC.listOf()
                            .fieldOf("entries")
                            .forGetter(RegenPools.Pool::entries)
            ).apply(instance, RegenPools.Pool::new)
    );

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons,
                         ResourceManager manager,
                         ProfilerFiller profiler) {

        DBRMod.LOGGER.info("LOADING DATAPACKS FOR DBR: {}", jsons.size());

        Map<String, RegenPools.Pool> pools = new HashMap<>();

        for (var entry : jsons.entrySet()) {

            ResourceLocation fileId = entry.getKey();
            JsonElement json = entry.getValue();

            DBRMod.LOGGER.info("LOADED {}", fileId);

            RegenPools.Pool pool = POOL_CODEC
                    .parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error ->
                            DBRMod.LOGGER.error("Failed parsing {}: {}", fileId, error)
                    )
                    .orElse(null);

            if (pool == null) continue;

            // file: data/dynamicblockregen/<pool>.json
            String path = fileId.getPath();

            String poolId = path.substring(path.lastIndexOf("/") + 1)
                    .replace(".json", "");

            pools.put(poolId, pool);
        }

        RegenPools.replace(pools);

        DBRMod.LOGGER.info("Loaded {} regen pools", pools.size());
    }
}
