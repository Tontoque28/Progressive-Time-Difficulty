package com.tontoque28.progressivetimedifficulty;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(ProgressiveTimeDifficultyMod.MODID)
public class ProgressiveTimeDifficultyMod {
    public static final String MODID = "progressivetimedifficulty";

    public static boolean DEV_MODE = false;

    public ProgressiveTimeDifficultyMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}
