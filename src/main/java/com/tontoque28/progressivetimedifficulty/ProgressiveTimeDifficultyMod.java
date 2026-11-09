package com.tontoque28.progressivetimedifficulty;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = ProgressiveTimeDifficultyMod.MODID, name = "Progressive Time Difficulty", version = "1.1.7", acceptableRemoteVersions = "*", acceptedMinecraftVersions = "[1.12.2]")
public class ProgressiveTimeDifficultyMod {
    public static final String MODID = "progressivetimedifficulty";

    public static boolean DEV_MODE = false;

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new ModCommands());
    }
}
