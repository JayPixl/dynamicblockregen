package io.jaypixl.dynamicblockregen.level;

import io.jaypixl.dynamicblockregen.data.RegenEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;


public record RegenTask(
        ResourceKey<Level> dimension,
        BlockPos pos,
        RegenEntry entry
) {}