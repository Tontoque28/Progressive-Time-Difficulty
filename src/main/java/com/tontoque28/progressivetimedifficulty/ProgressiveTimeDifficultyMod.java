package com.tontoque28.progressivetimedifficulty;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public class ProgressiveTimeDifficultyMod implements ModInitializer {
    public static final String MODID = "progressivetimedifficulty";

    public static boolean DEV_MODE = false;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> ModCommands.register(dispatcher));
        ServerEntityEvents.ENTITY_LOAD.register(DifficultyHandler::onEntityJoinLevel);
        ServerWorldEvents.LOAD.register((server, world) -> DifficultyHandler.onLevelLoad(world));
        ServerTickEvents.END_SERVER_TICK.register(DifficultyHandler::onServerTick);
        ServerTickEvents.END_WORLD_TICK.register(DifficultyHandler::onLevelTick);
        UseEntityCallback.EVENT.register(DifficultyHandler::onEntityInteract);
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> DifficultyHandler.copyPlayerTime(oldPlayer, newPlayer));
    }
}
