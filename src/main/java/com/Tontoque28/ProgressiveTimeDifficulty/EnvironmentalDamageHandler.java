package com.Tontoque28.ProgressiveTimeDifficulty;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProgressiveTimeDifficultyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EnvironmentalDamageHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof Player player) {
            DamageSource source = event.getSource();
            if (isEnvironmentalDamage(source)) {
                applyEnvironmentalScaling(event, player, source);
            }
        }

        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (attacker.getPersistentData().contains(DifficultyHandler.NBT_KEY_MAGIC_DMG_MULT)) {
                applyHybridDamage(event, attacker);
            }
        }
    }

    private static void applyHybridDamage(LivingHurtEvent event, LivingEntity attacker) {
        if (event.getSource().is(DamageTypes.MAGIC)) return;

        double magicMult = attacker.getPersistentData().getDouble(DifficultyHandler.NBT_KEY_MAGIC_DMG_MULT);
        if (magicMult <= 0) return;

        float damage = event.getAmount();
        // ATENUACIÓN MÁGICA: Reducir el daño mágico al 50% para balancear el aumento del límite físico
        float magicDamage = (float) (damage * (magicMult / 5.0) * 0.5); 

        if (magicDamage > 0) {
            event.getEntity().invulnerableTime = 0;
            event.getEntity().hurt(attacker.damageSources().magic(), magicDamage);
        }
    }

    private static void applyEnvironmentalScaling(LivingHurtEvent event, Player player, DamageSource source) {
        long gameTime = player.level().getLevelData().getGameTime();
        long playerTime = 0;
        
        if (player instanceof ServerPlayer serverPlayer) {
            playerTime = serverPlayer.getPersistentData().getLong(DifficultyHandler.NBT_KEY_PLAYER_TIME);
        }
        
        long additionalTime = 0;
        if (player.level() instanceof ServerLevel serverLevel) {
            additionalTime = DifficultySavedData.get(serverLevel).getAdditionalTime();
        }
        
        double factor = DifficultyHandler.calculateDifficultyFactor(player.level(), gameTime + playerTime + additionalTime);
        
        if (factor <= 0) return;

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

        float originalDamage = event.getAmount();
        float newDamage = originalDamage * multiplier;
        
        event.setAmount(newDamage);
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
