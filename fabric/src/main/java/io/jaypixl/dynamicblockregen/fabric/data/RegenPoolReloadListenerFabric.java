package io.jaypixl.dynamicblockregen.fabric.data;

import io.jaypixl.dynamicblockregen.DBRMod;
import io.jaypixl.dynamicblockregen.data.RegenPoolReloadListener;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class RegenPoolReloadListenerFabric extends RegenPoolReloadListener implements IdentifiableResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(DBRMod.MOD_ID, "pools");
    }
}
