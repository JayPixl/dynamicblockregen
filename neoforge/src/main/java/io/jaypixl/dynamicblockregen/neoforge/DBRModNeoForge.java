package io.jaypixl.dynamicblockregen.neoforge;

import io.jaypixl.dynamicblockregen.neoforge.events.ModEventsNeoForge;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import io.jaypixl.dynamicblockregen.DBRMod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(DBRMod.MOD_ID)
public final class DBRModNeoForge {
    public DBRModNeoForge(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(ModEventsNeoForge.class);

        DBRMod.init();
    }
}
