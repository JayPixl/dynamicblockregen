package io.jaypixl.dynamicblockregen.item;

import io.jaypixl.dynamicblockregen.data.RegenPools;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RegenWandUtil {

    public static final String TAG_NAME = "dbr_pool_name";

    public static boolean isWand(ItemStack stack) {
        return stack.is(Items.BLAZE_ROD);
    }

    public static ItemStack setWand(ItemStack stack, String pool) {

        ItemStack newStack = stack.copy();

        // --- STORE POOL (Custom Data) ---
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_NAME, pool);

        newStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        // --- CUSTOM NAME ---
        Component name = Component.literal("Regen Wand")
                .withStyle(Style.EMPTY
                        .withColor(ChatFormatting.GOLD)
                        .withItalic(false));

        newStack.set(DataComponents.CUSTOM_NAME, name);
        newStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        // --- LORE ---
        Component poolLine = Component.literal("Pool: " + pool)
                .withStyle(ChatFormatting.GRAY);

        Component hintLine = Component.literal("Right-click to assign blocks")
                .withStyle(ChatFormatting.DARK_GRAY);

        newStack.set(DataComponents.LORE, new ItemLore(List.of(poolLine, hintLine)));

        return newStack;
    }

    public static ItemStack getWand() {
        return getWand(RegenPools.DEFAULT_POOL);
    }

    public static ItemStack getWand(String pool) {
        return setWand(new ItemStack(Items.BLAZE_ROD), pool);
    }

    @Nullable
    public static String getPool(ItemStack stack) {

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;

        CompoundTag tag = data.copyTag();

        return tag.getString(TAG_NAME);
    }
}