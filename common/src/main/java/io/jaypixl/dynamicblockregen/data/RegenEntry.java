package io.jaypixl.dynamicblockregen.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record RegenEntry(
        BlockState state,
        int weight,
        Pair<Integer, Integer> regenTime
) {

    public static final Codec<RegenEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(

            ResourceLocation.CODEC.fieldOf("block")
                    .forGetter(e -> BuiltInRegistries.BLOCK.getKey(e.state().getBlock())),

            Codec.unboundedMap(Codec.STRING, Codec.STRING)
                    .optionalFieldOf("blockstate", Map.of())
                    .forGetter(e -> {
                        Map<String, String> props = new HashMap<>();
                        e.state().getValues().forEach((prop, value) ->
                                props.put(prop.getName(), value.toString())
                        );
                        return props;
                    }),

            Codec.INT.fieldOf("weight")
                    .forGetter(RegenEntry::weight),

            Codec.INT.listOf().comapFlatMap(list -> {
                        if (list.size() != 2) {
                            return DataResult.error(() -> "regen_time must have exactly 2 numbers");
                        }
                        return DataResult.success(Pair.of(list.get(0), list.get(1)));
                    }, pair -> List.of(pair.getFirst(), pair.getSecond()))
                    .fieldOf("regen_time")
                    .forGetter(RegenEntry::regenTime)

    ).apply(instance, (blockId, props, weight, regenTime) -> {

        Block block = BuiltInRegistries.BLOCK.get(blockId);

        BlockState state = block.defaultBlockState();

        for (var entry : props.entrySet()) {

            Property<?> property = block.getStateDefinition().getProperty(entry.getKey());

            if (property == null) {
                // Optional: log warning
                continue;
            }

            state = applyProperty(state, property, entry.getValue());
        }

        return new RegenEntry(state, weight, regenTime);
    }));

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState applyProperty(
            BlockState state,
            Property<?> property,
            String value
    ) {
        return property.getValue(value)
                .map(v -> setValue(state, (Property) property, v))
                .orElse(state);
    }

    @SuppressWarnings({"unchecked"})
    private static <T extends Comparable<T>> BlockState setValue(
            BlockState state,
            Property<T> property,
            Comparable<?> value
    ) {
        return state.setValue(property, (T) value);
    }
}