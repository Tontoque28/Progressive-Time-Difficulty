package com.tontoque28.progressivetimedifficulty;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class EnvironmentalDamageHandler {

    public static float onLivingHurt(LivingEntity entity, DamageSource source, float amount) {
        if (entity.level().isClientSide) return amount;
        if (entity.isInvulnerableTo(source)) return amount;

        float result = amount;

        if (entity instanceof Player player) {
            if (isEnvironmentalDamage(source)) {
                result = applyEnvironmentalScaling(result, player, source);
            }
        }

        if (source.getEntity() instanceof LivingEntity attacker) {
            if (PersistentDataHolder.of(attacker).contains(DifficultyHandler.NBT_KEY_MAGIC_DMG_MULT)) {
                applyHybridDamage(entity, source, attacker, result);
            }
        }

        return result;
    }

    private static void applyHybridDamage(LivingEntity victim, DamageSource source, LivingEntity attacker, float damage) {
        if (source.is(DamageTypes.MAGIC)) return;

        double magicMult = PersistentDataHolder.of(attacker).getDouble(DifficultyHandler.NBT_KEY_MAGIC_DMG_MULT);
        if (magicMult <= 0) return;

        float magicDamage = (float) (damage * (magicMult / 5.0) * 0.5);

        if (magicDamage > 0) {
            victim.invulnerableTime = 0;
            victim.hurt(attacker.damageSources().magic(), magicDamage);
        }
    }

    private static float applyEnvironmentalScaling(float originalDamage, Player player, DamageSource source) {
        long gameTime = player.level().getLevelData().getGameTime();
        long playerTime = 0;

        if (player instanceof ServerPlayer serverPlayer) {
            playerTime = PersistentDataHolder.of(serverPlayer).getLong(DifficultyHandler.NBT_KEY_PLAYER_TIME);
        }

        long additionalTime = 0;
        if (player.level() instanceof ServerLevel serverLevel) {
            additionalTime = DifficultySavedData.get(serverLevel).getAdditionalTime();
        }

        double factor = DifficultyHandler.calculateDifficultyFactor(player.level(), gameTime + playerTime + additionalTime);

        if (factor <= 0) return originalDamage;

        float multiplier = 1.0f + (float) (factor * 2.0);

        boolean isHardcore = player.level() instanceof ServerLevel sl && DifficultySavedData.get(sl).isHardcoreMode();

        if (isHardcore) {
            multiplier += (float) (factor * 1.5);
        }

        if (source.is(DamageTypes.LAVA) || source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE)) {
            multiplier *= 1.5f;
        }

        if (isHardcore && source.is(DamageTypes.FALL)) {
            multiplier *= 1.25f;
        }

        return originalDamage * multiplier;
    }

    private static boolean isEnvironmentalDamage(DamageSource source) {
        if (source.is(DamageTypes.FALL)) return true;
        if (source.is(DamageTypes.DROWN)) return true;
        if (source.is(DamageTypes.STARVE)) return true;

        if (source.getMsgId().equals("outOfWorld")) return true;

        if (source.is(DamageTypes.IN_WALL)) return true;
        if (source.is(DamageTypes.FREEZE)) return true;
        if (source.is(DamageTypes.LIGHTNING_BOLT)) return true;

        if (source.is(DamageTypes.IN_FIRE)) return true;
        if (source.is(DamageTypes.ON_FIRE)) return true;
        if (source.is(DamageTypes.LAVA)) return true;
        if (source.is(DamageTypes.HOT_FLOOR)) return true;

        if (source.is(DamageTypes.CACTUS)) return true;
        if (source.is(DamageTypes.SWEET_BERRY_BUSH)) return true;
        if (source.is(DamageTypes.FALLING_ANVIL)) return true;
        if (source.is(DamageTypes.FALLING_STALACTITE)) return true;
        if (source.is(DamageTypes.STALAGMITE)) return true;
        if (source.is(DamageTypes.WITHER)) return true;

        if (source.is(DamageTypes.EXPLOSION)) return true;
        if (source.is(DamageTypes.PLAYER_EXPLOSION)) return true;
        if (source.is(DamageTypes.BAD_RESPAWN_POINT)) return true;
        if (source.is(DamageTypes.FIREWORKS)) return true;

        if (source.is(DamageTypes.FLY_INTO_WALL)) return true;
        if (source.is(DamageTypes.MAGIC)) return true;
        if (source.is(DamageTypes.INDIRECT_MAGIC)) return true;
        if (source.is(DamageTypes.THORNS)) return true;
        if (source.is(DamageTypes.CRAMMING)) return true;

        return false;
    }
}
