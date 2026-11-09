package com.tontoque28.progressivetimedifficulty;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import java.util.List;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("progresstd")
            .requires(source -> source.hasPermission(2))

            .then(Commands.literal("add_days")
                .then(Commands.argument("days", IntegerArgumentType.integer())
                    .executes(ModCommands::addDays)))
            .then(Commands.literal("set_day")
                .then(Commands.argument("day", IntegerArgumentType.integer(0))
                    .executes(ModCommands::setDay)))
            .then(Commands.literal("remove_days")
                .then(Commands.argument("days", IntegerArgumentType.integer())
                    .executes(ModCommands::removeDays)))

            .then(Commands.literal("hardcore")
                .then(Commands.argument("active", BoolArgumentType.bool())
                    .executes(ModCommands::setHardcoreMode)))

            .then(Commands.literal("dev_mode")
                .then(Commands.argument("active", BoolArgumentType.bool())
                    .executes(ModCommands::setDevMode)))

            .then(Commands.literal("player")
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.literal("add_days")
                        .then(Commands.argument("days", IntegerArgumentType.integer())
                            .executes(ModCommands::addPlayerDays)))
                    .then(Commands.literal("set_day")
                        .then(Commands.argument("day", IntegerArgumentType.integer(0))
                            .executes(ModCommands::setPlayerDay)))
                    .then(Commands.literal("info")
                        .executes(ModCommands::showPlayerInfo))
                )
            )

            .then(Commands.literal("add_ticks")
                .then(Commands.argument("time", LongArgumentType.longArg())
                    .executes(ModCommands::addTime)))
            .then(Commands.literal("set_ticks")
                .then(Commands.argument("time", LongArgumentType.longArg())
                    .executes(ModCommands::setTime)))

            .then(Commands.literal("sync")
                .executes(ModCommands::syncTime))
            .then(Commands.literal("info")
                .executes(ModCommands::showInfo))
            .then(Commands.literal("update")
                .executes(ModCommands::updateEntities))

            .then(Commands.literal("force_overcharge")
                .executes(ModCommands::forceOvercharge));

        dispatcher.register(builder);
    }

    private static int setDevMode(CommandContext<CommandSource> context) {
        boolean active = BoolArgumentType.getBool(context, "active");
        ServerWorld level = context.getSource().getLevel();
        DifficultySavedData data = DifficultySavedData.get(level);

        data.setDevMode(active);

        String status = active ? "§aENABLED" : "§cDISABLED";
        context.getSource().sendSuccess(new StringTextComponent("§6[PTD] Developer Mode: " + status), true);
        return 1;
    }

    private static int setHardcoreMode(CommandContext<CommandSource> context) {
        boolean active = BoolArgumentType.getBool(context, "active");
        ServerWorld level = context.getSource().getLevel();
        DifficultySavedData data = DifficultySavedData.get(level);

        data.setHardcoreMode(active);
        updateEntities(context);

        String status = active ? "§cENABLED (1.5% per day)" : "§aDISABLED (Dynamic World Difficulty)";
        context.getSource().sendSuccess(new StringTextComponent("§6[PTD] Hardcore Mode: " + status), true);
        return 1;
    }

    private static int forceOvercharge(CommandContext<CommandSource> context) {
        if (!ProgressiveTimeDifficultyMod.DEV_MODE) {
            context.getSource().sendFailure(new StringTextComponent("Dev Mode is disabled. Use '/progresstd dev_mode true' to enable."));
            return 0;
        }

        Entity source = context.getSource().getEntity();
        if (!(source instanceof ServerPlayerEntity)) {
            context.getSource().sendFailure(new StringTextComponent("Only players can use this command."));
            return 0;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) source;

        Vector3d viewVec = player.getViewVector(1.0F);
        Vector3d eyePos = player.getEyePosition(1.0F);
        Vector3d targetVec = eyePos.add(viewVec.x * 5, viewVec.y * 5, viewVec.z * 5);
        AxisAlignedBB box = new AxisAlignedBB(eyePos, targetVec).inflate(1.0);

        List<LivingEntity> entities = player.level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player);

        if (entities.isEmpty()) {
            context.getSource().sendFailure(new StringTextComponent("No entity found in front of you."));
            return 0;
        }

        LivingEntity target = entities.get(0);
        target.getPersistentData().putBoolean(DifficultyHandler.NBT_KEY_FORCE_OVERCHARGE, true);
        DifficultyHandler.updateEntityBuffs(target);

        context.getSource().sendSuccess(new StringTextComponent("§c[PTD] Forced OVERCHARGE on " + target.getName().getString()), true);
        return 1;
    }

    private static int addPlayerDays(CommandContext<CommandSource> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity target = EntityArgument.getPlayer(context, "target");
        int days = IntegerArgumentType.getInteger(context, "days");
        long ticksToAdd = days * 24000L;

        long current = target.getPersistentData().getLong(DifficultyHandler.NBT_KEY_PLAYER_TIME);
        target.getPersistentData().putLong(DifficultyHandler.NBT_KEY_PLAYER_TIME, current + ticksToAdd);

        updateEntities(context);
        context.getSource().sendSuccess(new StringTextComponent("§a[PTD] Added " + days + " days to player " + target.getName().getString()), true);
        return 1;
    }

    private static int setPlayerDay(CommandContext<CommandSource> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity target = EntityArgument.getPlayer(context, "target");
        int day = IntegerArgumentType.getInteger(context, "day");
        long ticks = day * 24000L;

        target.getPersistentData().putLong(DifficultyHandler.NBT_KEY_PLAYER_TIME, ticks);

        updateEntities(context);
        context.getSource().sendSuccess(new StringTextComponent("§a[PTD] Set player " + target.getName().getString() + " time to Day " + day), true);
        return 1;
    }

    private static int showPlayerInfo(CommandContext<CommandSource> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity target = EntityArgument.getPlayer(context, "target");
        long playerTime = target.getPersistentData().getLong(DifficultyHandler.NBT_KEY_PLAYER_TIME);
        long playerDays = playerTime / 24000L;

        context.getSource().sendSuccess(new StringTextComponent(
            String.format("§6[PTD] Player Info (%s):\n§7- Personal Time: %d ticks\n§e- Personal Days: %d",
            target.getName().getString(), playerTime, playerDays)
        ), false);
        return 1;
    }

    private static int addDays(CommandContext<CommandSource> context) {
        int days = IntegerArgumentType.getInteger(context, "days");
        long ticksToAdd = days * 24000L;

        ServerWorld level = context.getSource().getLevel();
        DifficultySavedData data = DifficultySavedData.get(level);

        data.addTime(ticksToAdd);
        updateEntities(context);

        context.getSource().sendSuccess(new StringTextComponent("§a[PTD] Added " + days + " days (" + ticksToAdd + " ticks) to GLOBAL difficulty."), true);
        return 1;
    }

    private static int setDay(CommandContext<CommandSource> context) {
        int targetDay = IntegerArgumentType.getInteger(context, "day");
        long targetTicks = targetDay * 24000L;

        ServerWorld level = context.getSource().getLevel();
        long currentWorldTime = level.getGameTime();
        long requiredOffset = targetTicks - currentWorldTime;

        DifficultySavedData data = DifficultySavedData.get(level);
        data.setAdditionalTime(requiredOffset);
        updateEntities(context);

        context.getSource().sendSuccess(new StringTextComponent("§a[PTD] Global difficulty forced to Day " + targetDay + "."), true);
        return 1;
    }

    private static int removeDays(CommandContext<CommandSource> context) {
        int days = IntegerArgumentType.getInteger(context, "days");
        long ticksToRemove = days * 24000L;

        ServerWorld level = context.getSource().getLevel();
        DifficultySavedData data = DifficultySavedData.get(level);

        long currentOffset = data.getAdditionalTime();
        data.setAdditionalTime(currentOffset - ticksToRemove);
        updateEntities(context);

        context.getSource().sendSuccess(new StringTextComponent("§c[PTD] Removed " + days + " days from GLOBAL difficulty."), true);
        return 1;
    }

    private static int addTime(CommandContext<CommandSource> context) {
        long time = LongArgumentType.getLong(context, "time");
        ServerWorld level = context.getSource().getLevel();
        DifficultySavedData data = DifficultySavedData.get(level);

        data.addTime(time);
        updateEntities(context);
        context.getSource().sendSuccess(new StringTextComponent("Added " + time + " ticks."), true);
        return 1;
    }

    private static int setTime(CommandContext<CommandSource> context) {
        long time = LongArgumentType.getLong(context, "time");
        ServerWorld level = context.getSource().getLevel();
        DifficultySavedData data = DifficultySavedData.get(level);

        data.setAdditionalTime(time);
        updateEntities(context);
        context.getSource().sendSuccess(new StringTextComponent("Set offset to " + time + " ticks."), true);
        return 1;
    }

    private static int syncTime(CommandContext<CommandSource> context) {
        ServerWorld level = context.getSource().getLevel();
        DifficultySavedData data = DifficultySavedData.get(level);

        data.setAdditionalTime(0);
        updateEntities(context);

        context.getSource().sendSuccess(new StringTextComponent("Difficulty synchronized with World Time (Offset = 0)."), true);
        return 1;
    }

    private static int showInfo(CommandContext<CommandSource> context) {
        ServerWorld level = context.getSource().getLevel();
        long gameTime = level.getGameTime();
        DifficultySavedData data = DifficultySavedData.get(level);
        long additionalTime = data.getAdditionalTime();
        long totalEffectiveTime = gameTime + additionalTime;

        double factor = DifficultyHandler.calculateDifficultyFactor(level, totalEffectiveTime);
        long effectiveDays = Math.max(0, totalEffectiveTime) / 24000L;

        String mode = data.isHardcoreMode() ? "§cHARDCORE" : "§aDYNAMIC (" + level.getDifficulty().getKey() + ")";
        String dev = ProgressiveTimeDifficultyMod.DEV_MODE ? "§e[DEV ON]" : "";

        context.getSource().sendSuccess(new StringTextComponent(
            String.format("§6[PTD] Global Status %s:\n§7- Mode: %s\n§7- World Time: %d\n§7- Global Offset: %d\n§e- Effective Global Time: %d (Day %d)\n§c- Base Difficulty Factor: %.2f%%",
            dev, mode, gameTime, additionalTime, totalEffectiveTime, effectiveDays, factor * 100)
        ), false);
        return 1;
    }

    private static int updateEntities(CommandContext<CommandSource> context) {
        ServerWorld level = context.getSource().getLevel();
        int count = 0;

        for (Entity entity : level.getAllEntities()) {
            if (DifficultyHandler.updateEntityBuffs(entity)) {
                count++;
            }
        }

        int finalCount = count;
        context.getSource().sendSuccess(new StringTextComponent("§e[PTD] Updated buffs for " + finalCount + " entities."), true);
        return 1;
    }
}
