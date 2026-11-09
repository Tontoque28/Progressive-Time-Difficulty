package com.tontoque28.progressivetimedifficulty;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = ProgressiveTimeDifficultyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EnvironmentalDamageHandler {

    private static final Set<String> ENVIRONMENTAL = new HashSet<>(Arrays.asList(
        "fall", "drown", "starve", "outOfWorld", "inWall", "lightningBolt",
        "inFire", "onFire", "lava", "hotFloor", "cactus", "sweetBerryBush",
        "anvil", "fallingBlock", "wither", "explosion", "explosion.player",
        "badRespawnPoint", "fireworks", "flyIntoWall", "magic", "indirectMagic",
        "thorns", "cramming"
    ));

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntityLiving().level.isClientSide) return;

        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            DamageSource source = event.getSource();
            if (isEnvironmentalDamage(source)) {
                applyEnvironmentalScaling(event, player, source);
            }
        }

        if (event.getSource().getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getSource().getEntity();
            if (attacker.getPersistentData().contains(DifficultyHandler.NBT_KEY_MAGIC_DMG_MULT)) {
                applyHybridDamage(event, attacker);
            }
        }
    }

    private static void applyHybridDamage(LivingHurtEvent event, LivingEntity attacker) {
        if ("magic".equals(event.getSource().getMsgId())) return;

        double magicMult = attacker.getPersistentData().getDouble(DifficultyHandler.NBT_KEY_MAGIC_DMG_MULT);
        if (magicMult <= 0) return;

        float damage = event.getAmount();

        float magicDamage = (float) (damage * (magicMult / 5.0) * 0.5);

        if (magicDamage > 0) {
            event.getEntityLiving().invulnerableTime = 0;
            event.getEntityLiving().hurt(DamageSource.MAGIC, magicDamage);
        }
    }

    private static void applyEnvironmentalScaling(LivingHurtEvent event, PlayerEntity player, DamageSource source) {
        long gameTime = player.level.getGameTime();
        long playerTime = 0;

        if (player instanceof ServerPlayerEntity) {
            playerTime = player.getPersistentData().getLong(DifficultyHandler.NBT_KEY_PLAYER_TIME);
        }

        long additionalTime = 0;
        if (player.level instanceof ServerWorld) {
            additionalTime = DifficultySavedData.get((ServerWorld) player.level).getAdditionalTime();
        }

        double factor = DifficultyHandler.calculateDifficultyFactor(player.level, gameTime + playerTime + additionalTime);

        if (factor <= 0) return;

        float multiplier = 1.0f + (float) (factor * 2.0);

        boolean isHardcore = player.level instanceof ServerWorld && DifficultySavedData.get((ServerWorld) player.level).isHardcoreMode();

        if (isHardcore) {
            multiplier += (float) (factor * 1.5);
        }

        String id = source.getMsgId();
        if ("lava".equals(id) || "inFire".equals(id) || "onFire".equals(id)) {
            multiplier *= 1.5f;
        }

        if (isHardcore && "fall".equals(id)) {
            multiplier *= 1.25f;
        }

        float originalDamage = event.getAmount();
        float newDamage = originalDamage * multiplier;

        event.setAmount(newDamage);
    }

    private static boolean isEnvironmentalDamage(DamageSource source) {
        return ENVIRONMENTAL.contains(source.getMsgId());
    }
}
