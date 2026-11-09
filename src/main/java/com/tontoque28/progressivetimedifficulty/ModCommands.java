package com.tontoque28.progressivetimedifficulty;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModCommands extends CommandBase {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
        "add_days", "set_day", "remove_days", "hardcore", "dev_mode", "player",
        "add_ticks", "set_ticks", "sync", "info", "update", "force_overcharge"
    );

    @Override
    public String getName() {
        return "progresstd";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/progresstd <add_days|set_day|remove_days|hardcore|dev_mode|player|add_ticks|set_ticks|sync|info|update|force_overcharge>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, SUBCOMMANDS);
        }
        if (args.length == 2 && ("hardcore".equals(args[0]) || "dev_mode".equals(args[0]))) {
            return getListOfStringsMatchingLastWord(args, "true", "false");
        }
        if (args.length == 2 && "player".equals(args[0])) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        if (args.length == 3 && "player".equals(args[0])) {
            return getListOfStringsMatchingLastWord(args, "add_days", "set_day", "info");
        }
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            throw new WrongUsageException(getUsage(sender));
        }
        World level = sender.getEntityWorld();
        switch (args[0]) {
            case "add_days":
                requireArgs(sender, args, 2);
                addDays(sender, level, parseInt(args[1]));
                break;
            case "set_day":
                requireArgs(sender, args, 2);
                setDay(sender, level, parseInt(args[1], 0));
                break;
            case "remove_days":
                requireArgs(sender, args, 2);
                removeDays(sender, level, parseInt(args[1]));
                break;
            case "hardcore":
                requireArgs(sender, args, 2);
                setHardcoreMode(sender, level, parseBoolean(args[1]));
                break;
            case "dev_mode":
                requireArgs(sender, args, 2);
                setDevMode(sender, level, parseBoolean(args[1]));
                break;
            case "player":
                playerCommand(server, sender, args);
                break;
            case "add_ticks":
                requireArgs(sender, args, 2);
                addTime(sender, level, parseLong(args[1]));
                break;
            case "set_ticks":
                requireArgs(sender, args, 2);
                setTime(sender, level, parseLong(args[1]));
                break;
            case "sync":
                syncTime(sender, level);
                break;
            case "info":
                showInfo(sender, level);
                break;
            case "update":
                updateEntities(sender, level);
                break;
            case "force_overcharge":
                forceOvercharge(sender, level);
                break;
            default:
                throw new WrongUsageException(getUsage(sender));
        }
    }

    private void requireArgs(ICommandSender sender, String[] args, int count) throws WrongUsageException {
        if (args.length != count) {
            throw new WrongUsageException(getUsage(sender));
        }
    }

    private void success(ICommandSender sender, String message) {
        notifyCommandListener(sender, this, "%s", message);
    }

    private void playerCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 3) {
            throw new WrongUsageException(getUsage(sender));
        }
        EntityPlayerMP target = getPlayer(server, sender, args[1]);
        World level = sender.getEntityWorld();
        switch (args[2]) {
            case "add_days":
                requireArgs(sender, args, 4);
                addPlayerDays(sender, level, target, parseInt(args[3]));
                break;
            case "set_day":
                requireArgs(sender, args, 4);
                setPlayerDay(sender, level, target, parseInt(args[3], 0));
                break;
            case "info":
                requireArgs(sender, args, 3);
                showPlayerInfo(sender, target);
                break;
            default:
                throw new WrongUsageException(getUsage(sender));
        }
    }

    private void setDevMode(ICommandSender sender, World level, boolean active) {
        DifficultySavedData data = DifficultySavedData.get(level);

        data.setDevMode(active);

        String status = active ? "§aENABLED" : "§cDISABLED";
        success(sender, "§6[PTD] Developer Mode: " + status);
    }

    private void setHardcoreMode(ICommandSender sender, World level, boolean active) {
        DifficultySavedData data = DifficultySavedData.get(level);

        data.setHardcoreMode(active);
        updateEntities(sender, level);

        String status = active ? "§cENABLED (1.5% per day)" : "§aDISABLED (Dynamic World Difficulty)";
        success(sender, "§6[PTD] Hardcore Mode: " + status);
    }

    private void forceOvercharge(ICommandSender sender, World level) throws CommandException {
        if (!ProgressiveTimeDifficultyMod.DEV_MODE) {
            throw new CommandException("Dev Mode is disabled. Use '/progresstd dev_mode true' to enable.");
        }

        Entity source = sender.getCommandSenderEntity();
        if (!(source instanceof EntityPlayerMP)) {
            throw new CommandException("Only players can use this command.");
        }
        EntityPlayerMP player = (EntityPlayerMP) source;

        Vec3d viewVec = player.getLook(1.0F);
        Vec3d eyePos = player.getPositionEyes(1.0F);
        Vec3d targetVec = eyePos.addVector(viewVec.x * 5, viewVec.y * 5, viewVec.z * 5);
        AxisAlignedBB box = new AxisAlignedBB(eyePos.x, eyePos.y, eyePos.z, targetVec.x, targetVec.y, targetVec.z).grow(1.0);

        List<EntityLivingBase> entities = player.world.getEntitiesWithinAABB(EntityLivingBase.class, box, e -> e != player);

        if (entities.isEmpty()) {
            throw new CommandException("No entity found in front of you.");
        }

        EntityLivingBase target = entities.get(0);
        target.getEntityData().setBoolean(DifficultyHandler.NBT_KEY_FORCE_OVERCHARGE, true);
        DifficultyHandler.updateEntityBuffs(target);

        success(sender, "§c[PTD] Forced OVERCHARGE on " + target.getName());
    }

    private void addPlayerDays(ICommandSender sender, World level, EntityPlayerMP target, int days) {
        long ticksToAdd = days * 24000L;

        long current = target.getEntityData().getLong(DifficultyHandler.NBT_KEY_PLAYER_TIME);
        target.getEntityData().setLong(DifficultyHandler.NBT_KEY_PLAYER_TIME, current + ticksToAdd);

        updateEntities(sender, level);
        success(sender, "§a[PTD] Added " + days + " days to player " + target.getName());
    }

    private void setPlayerDay(ICommandSender sender, World level, EntityPlayerMP target, int day) {
        long ticks = day * 24000L;

        target.getEntityData().setLong(DifficultyHandler.NBT_KEY_PLAYER_TIME, ticks);

        updateEntities(sender, level);
        success(sender, "§a[PTD] Set player " + target.getName() + " time to Day " + day);
    }

    private void showPlayerInfo(ICommandSender sender, EntityPlayerMP target) {
        long playerTime = target.getEntityData().getLong(DifficultyHandler.NBT_KEY_PLAYER_TIME);
        long playerDays = playerTime / 24000L;

        sender.sendMessage(new TextComponentString(
            String.format("§6[PTD] Player Info (%s):\n§7- Personal Time: %d ticks\n§e- Personal Days: %d",
            target.getName(), playerTime, playerDays)
        ));
    }

    private void addDays(ICommandSender sender, World level, int days) {
        long ticksToAdd = days * 24000L;

        DifficultySavedData data = DifficultySavedData.get(level);

        data.addTime(ticksToAdd);
        updateEntities(sender, level);

        success(sender, "§a[PTD] Added " + days + " days (" + ticksToAdd + " ticks) to GLOBAL difficulty.");
    }

    private void setDay(ICommandSender sender, World level, int targetDay) {
        long targetTicks = targetDay * 24000L;

        long currentWorldTime = level.getTotalWorldTime();
        long requiredOffset = targetTicks - currentWorldTime;

        DifficultySavedData data = DifficultySavedData.get(level);
        data.setAdditionalTime(requiredOffset);
        updateEntities(sender, level);

        success(sender, "§a[PTD] Global difficulty forced to Day " + targetDay + ".");
    }

    private void removeDays(ICommandSender sender, World level, int days) {
        long ticksToRemove = days * 24000L;

        DifficultySavedData data = DifficultySavedData.get(level);

        long currentOffset = data.getAdditionalTime();
        data.setAdditionalTime(currentOffset - ticksToRemove);
        updateEntities(sender, level);

        success(sender, "§c[PTD] Removed " + days + " days from GLOBAL difficulty.");
    }

    private void addTime(ICommandSender sender, World level, long time) {
        DifficultySavedData data = DifficultySavedData.get(level);

        data.addTime(time);
        updateEntities(sender, level);
        success(sender, "Added " + time + " ticks.");
    }

    private void setTime(ICommandSender sender, World level, long time) {
        DifficultySavedData data = DifficultySavedData.get(level);

        data.setAdditionalTime(time);
        updateEntities(sender, level);
        success(sender, "Set offset to " + time + " ticks.");
    }

    private void syncTime(ICommandSender sender, World level) {
        DifficultySavedData data = DifficultySavedData.get(level);

        data.setAdditionalTime(0);
        updateEntities(sender, level);

        success(sender, "Difficulty synchronized with World Time (Offset = 0).");
    }

    private void showInfo(ICommandSender sender, World level) {
        long gameTime = level.getTotalWorldTime();
        DifficultySavedData data = DifficultySavedData.get(level);
        long additionalTime = data.getAdditionalTime();
        long totalEffectiveTime = gameTime + additionalTime;

        double factor = DifficultyHandler.calculateDifficultyFactor(level, totalEffectiveTime);
        long effectiveDays = Math.max(0, totalEffectiveTime) / 24000L;

        String mode = data.isHardcoreMode() ? "§cHARDCORE" : "§aDYNAMIC (" + level.getDifficulty().name().toLowerCase() + ")";
        String dev = ProgressiveTimeDifficultyMod.DEV_MODE ? "§e[DEV ON]" : "";

        sender.sendMessage(new TextComponentString(
            String.format("§6[PTD] Global Status %s:\n§7- Mode: %s\n§7- World Time: %d\n§7- Global Offset: %d\n§e- Effective Global Time: %d (Day %d)\n§c- Base Difficulty Factor: %.2f%%",
            dev, mode, gameTime, additionalTime, totalEffectiveTime, effectiveDays, factor * 100)
        ));
    }

    private void updateEntities(ICommandSender sender, World level) {
        int count = 0;

        for (Entity entity : new ArrayList<>(level.loadedEntityList)) {
            if (DifficultyHandler.updateEntityBuffs(entity)) {
                count++;
            }
        }

        success(sender, "§e[PTD] Updated buffs for " + count + " entities.");
    }
}
