package com.tontoque28.progressivetimedifficulty;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = ProgressiveTimeDifficultyMod.MODID)
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
        if (event.getEntityLiving().world.isRemote) return;

        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            DamageSource source = event.getSource();
            if (isEnvironmentalDamage(source)) {
                applyEnvironmentalScaling(event, player, source);
            }
        }

        Entity trueSource = event.getSource().getTrueSource();
        if (trueSource instanceof EntityLivingBase) {
            EntityLivingBase attacker = (EntityLivingBase) trueSource;
            if (attacker.getEntityData().hasKey(DifficultyHandler.NBT_KEY_MAGIC_DMG_MULT)) {
                applyHybridDamage(event, attacker);
            }
        }
    }

    private static void applyHybridDamage(LivingHurtEvent event, EntityLivingBase attacker) {
        if ("magic".equals(event.getSource().damageType)) return;

        double magicMult = attacker.getEntityData().getDouble(DifficultyHandler.NBT_KEY_MAGIC_DMG_MULT);
        if (magicMult <= 0) return;

        float damage = event.getAmount();

        float magicDamage = (float) (damage * (magicMult / 5.0) * 0.5);

        if (magicDamage > 0) {
            event.getEntityLiving().hurtResistantTime = 0;
            event.getEntityLiving().attackEntityFrom(DamageSource.MAGIC, magicDamage);
        }
    }

    private static void applyEnvironmentalScaling(LivingHurtEvent event, EntityPlayer player, DamageSource source) {
        long gameTime = player.world.getTotalWorldTime();
        long playerTime = 0;

        if (player instanceof EntityPlayerMP) {
            playerTime = player.getEntityData().getLong(DifficultyHandler.NBT_KEY_PLAYER_TIME);
        }

        long additionalTime = 0;
        if (player.world instanceof WorldServer) {
            additionalTime = DifficultySavedData.get(player.world).getAdditionalTime();
        }

        double factor = DifficultyHandler.calculateDifficultyFactor(player.world, gameTime + playerTime + additionalTime);

        if (factor <= 0) return;

        float multiplier = 1.0f + (float) (factor * 2.0);

        boolean isHardcore = player.world instanceof WorldServer && DifficultySavedData.get(player.world).isHardcoreMode();

        if (isHardcore) {
            multiplier += (float) (factor * 1.5);
        }

        String id = source.damageType;
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
        return ENVIRONMENTAL.contains(source.damageType);
    }
}
