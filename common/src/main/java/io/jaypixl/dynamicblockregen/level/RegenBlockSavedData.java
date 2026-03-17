package io.jaypixl.dynamicblockregen.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RegenBlockSavedData extends SavedData {

    private final Map<BlockPos, String> entries = new HashMap<>();

    public Map<BlockPos, String> getEntries() {
        return entries;
    }

    public static RegenBlockSavedData create() {
        return new RegenBlockSavedData();
    }

    public static RegenBlockSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {

        RegenBlockSavedData data = RegenBlockSavedData.create();

        ListTag list = tag.getList("blocks", Tag.TAG_COMPOUND);

        for (Tag t : list) {
            CompoundTag entry = (CompoundTag) t;

            BlockPos pos = new BlockPos(
                    entry.getInt("x"),
                    entry.getInt("y"),
                    entry.getInt("z")
            );

            String pool = entry.getString("pool");

            data.entries.put(pos, pool);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();

        for (var entry : entries.entrySet()) {
            BlockPos pos = entry.getKey();
            String pool = entry.getValue();

            CompoundTag blockTag = new CompoundTag();

            blockTag.putInt("x", pos.getX());
            blockTag.putInt("y", pos.getY());
            blockTag.putInt("z", pos.getZ());
            blockTag.putString("pool", pool);

            list.add(blockTag);
        }

        tag.put("blocks", list);

        return tag;
    }

    public void add(BlockPos pos, String pool) {
        entries.put(pos.immutable(), pool);
        setDirty();
    }

    public void remove(BlockPos pos) {
        entries.remove(pos);
        setDirty();
    }

    public boolean isRegenBlock(BlockPos pos) {
        return entries.containsKey(pos);
    }

    public String getPool(BlockPos pos) {
        return entries.get(pos);
    }

    public Map<BlockPos, String> getAll() {
        return entries;
    }

    public int removePool(String pool) {

        int removed = 0;

        Iterator<Map.Entry<BlockPos, String>> it = entries.entrySet().iterator();

        while (it.hasNext()) {
            var entry = it.next();

            if (entry.getValue().equals(pool)) {
                it.remove();
                removed++;
            }
        }

        setDirty();

        return removed;
    }

    public static RegenBlockSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(RegenBlockSavedData::create,
                RegenBlockSavedData::load, null),
                "dynamicblockregen"
        );
    }
}