package com.tontoque28.progressivetimedifficulty;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ProgressiveTimeDifficultyMod.MODID)
public class ProgressiveTimeDifficultyMod {
    public static final String MODID = "progressivetimedifficulty";

    public static boolean DEV_MODE = false;

    public ProgressiveTimeDifficultyMod(FMLJavaModLoadingContext context) {
        RegisterCommandsEvent.BUS.addListener(ProgressiveTimeDifficultyMod::onRegisterCommands);
        DifficultyHandler.register();
        EnvironmentalDamageHandler.register();
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}
