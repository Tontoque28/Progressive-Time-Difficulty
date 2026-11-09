package com.tontoque28.progressivetimedifficulty;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EnvironmentalDamageHandler {

    private static final Set<String> ENVIRONMENTAL = new HashSet<>(Arrays.asList(
        "fall", "drown", "starve", "outOfWorld", "inWall", "lightningBolt",
        "inFire", "onFire", "lava", "hotFloor", "cactus", "sweetBerryBush",
        "anvil", "fallingBlock", "wither", "explosion", "explosion.player",
        "badRespawnPoint", "fireworks", "flyIntoWall", "magic", "indirectMagic",
        "thorns", "cramming"
    ));

    public static float onLivingHurt(LivingEntity entity, DamageSource source, float amount) {
        if (entity.level.isClientSide) return amount;
        if (entity.isInvulnerableTo(source)) return amount;

        float result = amount;

        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (isEnvironmentalDamage(source)) {
                result = applyEnvironmentalScaling(result, player, source);
            }
        }

        if (source.getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) source.getEntity();
            if (PersistentDataHolder.of(attacker).contains(DifficultyHandler.NBT_KEY_MAGIC_DMG_MULT)) {
                applyHybridDamage(entity, source, attacker, result);
            }
        }

        return result;
    }

    private static void applyHybridDamage(LivingEntity victim, DamageSource source, LivingEntity attacker, float damage) {
        if ("magic".equals(source.getMsgId())) return;

        double magicMult = PersistentDataHolder.of(attacker).getDouble(DifficultyHandler.NBT_KEY_MAGIC_DMG_MULT);
        if (magicMult <= 0) return;

        float magicDamage = (float) (damage * (magicMult / 5.0) * 0.5);

        if (magicDamage > 0) {
            victim.invulnerableTime = 0;
            victim.hurt(DamageSource.MAGIC, magicDamage);
        }
    }

    private static float applyEnvironmentalScaling(float originalDamage, Player player, DamageSource source) {
        long gameTime = player.level.getGameTime();
        long playerTime = 0;

        if (player instanceof ServerPlayer) {
            playerTime = PersistentDataHolder.of(player).getLong(DifficultyHandler.NBT_KEY_PLAYER_TIME);
        }

        long additionalTime = 0;
        if (player.level instanceof ServerLevel) {
            additionalTime = DifficultySavedData.get((ServerLevel) player.level).getAdditionalTime();
        }

        double factor = DifficultyHandler.calculateDifficultyFactor(player.level, gameTime + playerTime + additionalTime);

        if (factor <= 0) return originalDamage;

        float multiplier = 1.0f + (float) (factor * 2.0);

        boolean isHardcore = player.level instanceof ServerLevel && DifficultySavedData.get((ServerLevel) player.level).isHardcoreMode();

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

        return originalDamage * multiplier;
    }

    private static boolean isEnvironmentalDamage(DamageSource source) {
        return ENVIRONMENTAL.contains(source.getMsgId());
    }
}
