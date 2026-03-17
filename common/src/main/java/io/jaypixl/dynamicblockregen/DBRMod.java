package io.jaypixl.dynamicblockregen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DBRMod {
    public static final String MOD_ID = "dynamicblockregen";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        //ReloadListenerRegistry.register(PackType.SERVER_DATA, new RegenPoolReloadListener());
    }
}
