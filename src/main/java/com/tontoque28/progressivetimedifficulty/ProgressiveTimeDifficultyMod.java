package com.tontoque28.progressivetimedifficulty;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ProgressiveTimeDifficultyMod.MODID)
public class ProgressiveTimeDifficultyMod {
    public static final String MODID = "progressivetimedifficulty";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static boolean DEV_MODE = false;

    public ProgressiveTimeDifficultyMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}
