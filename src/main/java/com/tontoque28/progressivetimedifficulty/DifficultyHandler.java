package com.tontoque28.progressivetimedifficulty;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.ExperienceBottleEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.WanderingTraderEntity;
import net.minecraft.entity.monster.ElderGuardianEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ProgressiveTimeDifficultyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DifficultyHandler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String NBT_KEY_BUFFED = "ptd_buffed";
    private static final String NBT_KEY_INFLUENCER = "ptd_influencer";
    public static final String NBT_KEY_PLAYER_TIME = "ptd_player_time";
    public static final String NBT_KEY_CAN_OVERCHARGE = "ptd_can_overcharge";
    public static final String NBT_KEY_FORCE_OVERCHARGE = "ptd_force_overcharge";
    public static final String NBT_KEY_MAGIC_DMG_MULT = "ptd_magic_dmg_mult";
    private static final String NBT_KEY_DIFFICULTY_FACTOR = "ptd_factor";
    private static final String NBT_KEY_SOUL_EATER_FACTOR = "ptd_soul_eater_factor";

    private static final UUID HP_MOD_UUID = UUID.fromString("c0d0b000-0000-0000-0000-000000000001");
    private static final UUID DMG_MOD_UUID = UUID.fromString("c0d0b000-0000-0000-0000-000000000002");
    private static final UUID ARMOR_MOD_UUID = UUID.fromString("c0d0b000-0000-0000-0000-000000000003");
    private static final UUID KB_MOD_UUID = UUID.fromString("c0d0b000-0000-0000-0000-000000000004");

    private static final List<EntityType<?>> BLACKLIST = Collections.singletonList(EntityType.CREEPER);

    private static Difficulty lastDifficulty = null;

    private static final ITag<EntityType<?>> BOSS_TAG = EntityTypeTags.createOptional(new ResourceLocation("forge", "bosses"));

    private static final Set<String> KNOWN_BOSS_IDS = new HashSet<>();
    static {

        KNOWN_BOSS_IDS.add("mowziesmobs:frostmaw");
        KNOWN_BOSS_IDS.add("mowziesmobs:ferrous_wroughtnaut");
        KNOWN_BOSS_IDS.add("mowziesmobs:naga");
        KNOWN_BOSS_IDS.add("mowziesmobs:barako");

        KNOWN_BOSS_IDS.add("twilightforest:naga");
        KNOWN_BOSS_IDS.add("twilightforest:lich");
        KNOWN_BOSS_IDS.add("twilightforest:hydra");
        KNOWN_BOSS_IDS.add("twilightforest:ur_ghast");
        KNOWN_BOSS_IDS.add("twilightforest:snow_queen");
        KNOWN_BOSS_IDS.add("twilightforest:minoshroom");
        KNOWN_BOSS_IDS.add("twilightforest:knight_phantom");
        KNOWN_BOSS_IDS.add("twilightforest:alpha_yeti");

        KNOWN_BOSS_IDS.add("cataclysm:ender_guardian");
        KNOWN_BOSS_IDS.add("cataclysm:netherite_monstrosity");
        KNOWN_BOSS_IDS.add("cataclysm:ignis");
        KNOWN_BOSS_IDS.add("cataclysm:the_harbinger");
        KNOWN_BOSS_IDS.add("cataclysm:the_leviathan");
        KNOWN_BOSS_IDS.add("cataclysm:ancient_remnant");

        KNOWN_BOSS_IDS.add("born_in_chaos_v1:nightmare_stalker");
        KNOWN_BOSS_IDS.add("born_in_chaos_v1:supreme_bonescaller");
        KNOWN_BOSS_IDS.add("born_in_chaos_v1:missioner");
        KNOWN_BOSS_IDS.add("born_in_chaos_v1:lifestealer");
        KNOWN_BOSS_IDS.add("born_in_chaos_v1:fallen_chaos_knight");

        KNOWN_BOSS_IDS.add("iceandfire:fire_dragon");
        KNOWN_BOSS_IDS.add("iceandfire:ice_dragon");
        KNOWN_BOSS_IDS.add("iceandfire:lightning_dragon");
        KNOWN_BOSS_IDS.add("iceandfire:cyclops");
        KNOWN_BOSS_IDS.add("iceandfire:gorgon");
        KNOWN_BOSS_IDS.add("iceandfire:hydra");

        KNOWN_BOSS_IDS.add("aquamirae:captain_cornelia");

        KNOWN_BOSS_IDS.add("bomd:night_lich");
        KNOWN_BOSS_IDS.add("bomd:obsidilith");
        KNOWN_BOSS_IDS.add("bomd:gauntlet");
        KNOWN_BOSS_IDS.add("bomd:void_blossom");

        KNOWN_BOSS_IDS.add("stalwart_dungeons:awful_ghast");
        KNOWN_BOSS_IDS.add("stalwart_dungeons:nether_keeper");

        KNOWN_BOSS_IDS.add("rottencreatures:immortal");
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isClientSide) return;
        if (event.getEntity() instanceof PlayerEntity) return;
        if (event.getEntity() instanceof LivingEntity) {
            updateEntityBuffs(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLevelLoad(WorldEvent.Load event) {
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld serverLevel = (ServerWorld) event.getWorld();
            if (serverLevel.dimension() == World.OVERWORLD) {
                lastDifficulty = serverLevel.getDifficulty();
            }
            DifficultySavedData.get(serverLevel);
            LOGGER.info("PTD: Loaded DifficultySavedData for level {} and synchronized DEV_MODE: {}", serverLevel.dimension().location(), ProgressiveTimeDifficultyMod.DEV_MODE);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level.isClientSide) {
            CompoundNBT data = event.player.getPersistentData();
            long currentTime = data.getLong(NBT_KEY_PLAYER_TIME);
            data.putLong(NBT_KEY_PLAYER_TIME, currentTime + 1);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isClientSide && event.world.dimension() == World.OVERWORLD) {
            ServerWorld serverLevel = (ServerWorld) event.world;
            Difficulty currentDiff = serverLevel.getDifficulty();

            if (lastDifficulty != null && lastDifficulty != currentDiff) {
                int count = 0;
                for (ServerWorld level : serverLevel.getServer().getAllLevels()) {
                    for (Entity entity : level.getAllEntities()) {
                        if (updateEntityBuffs(entity)) {
                            count++;
                        }
                    }
                }

                final int finalCount = count;
                serverLevel.getServer().getPlayerList().getPlayers().forEach(player -> {
                    if (player.hasPermissions(2)) {
                        send(player, String.format("§e[PTD] Difficulty changed to %s. Updated %d entities.", currentDiff.getDisplayName().getString(), finalCount));
                    }
                });
            }
            lastDifficulty = currentDiff;

            if (event.world.getGameTime() % 3600 == 0) {
                for (Entity entity : serverLevel.getAllEntities()) {
                    updateEntityBuffs(entity);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.level.isClientSide) return;
        if (entity instanceof PlayerEntity) return;

        if (entity.tickCount % 20 == 0) {
            CompoundNBT nbt = entity.getPersistentData();
            if (nbt.getBoolean(NBT_KEY_BUFFED)) {

                boolean canOvercharge = nbt.getBoolean(NBT_KEY_CAN_OVERCHARGE);
                boolean forceOvercharge = nbt.getBoolean(NBT_KEY_FORCE_OVERCHARGE);
                boolean isOverchargeActive = canOvercharge || forceOvercharge;

                boolean isActiveThreat = false;
                if (entity instanceof MobEntity) {
                    isActiveThreat = ((MobEntity) entity).getTarget() instanceof PlayerEntity;
                }

                if (isOverchargeActive && isActiveThreat) {
                    if (!entity.hasCustomName() || !entity.getCustomName().getString().equals("§c§lOVERCHARGED")) {
                        entity.setCustomName(new StringTextComponent("§c§lOVERCHARGED"));
                        entity.setCustomNameVisible(true);
                    }
                } else {
                    if (entity.hasCustomName() && entity.getCustomName().getString().equals("§c§lOVERCHARGED")) {
                        entity.setCustomName(null);
                        entity.setCustomNameVisible(false);
                    }
                }

                if (entity.level instanceof ServerWorld) {
                    ServerWorld serverLevel = (ServerWorld) entity.level;
                    if (nbt.getDouble(NBT_KEY_SOUL_EATER_FACTOR) > 0) {
                        serverLevel.sendParticles(ParticleTypes.WITCH, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(), 3, entity.getBbWidth() * 0.5, entity.getBbHeight() * 0.5, entity.getBbWidth() * 0.5, 0.05);
                    } else {
                        double factor = nbt.getDouble(NBT_KEY_DIFFICULTY_FACTOR);
                        if (factor >= 2.0) {
                            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, entity.getX(), entity.getY() + entity.getBbHeight() + 0.5, entity.getZ(), 1, 0.2, 0.2, 0.2, 0.02);
                        } else if (factor >= 1.0) {
                            serverLevel.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY() + entity.getBbHeight() + 0.5, entity.getZ(), 1, 0.2, 0.2, 0.2, 0.02);
                        } else if (factor >= 0.5) {
                            serverLevel.sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY() + entity.getBbHeight() + 0.5, entity.getZ(), 1, 0.2, 0.2, 0.2, 0.02);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!ProgressiveTimeDifficultyMod.DEV_MODE) return;
        if (event.getWorld().isClientSide) return;
        if (event.getHand() != Hand.MAIN_HAND) return;

        PlayerEntity player = event.getPlayer();
        if (player.getItemInHand(Hand.MAIN_HAND).getItem() == Items.STICK) {
            if (player.hasPermissions(2) || player.isCreative()) {
                Entity target = event.getTarget();
                if (target instanceof LivingEntity) {
                    showDebugInfo(player, (LivingEntity) target);
                    event.setCanceled(true);
                }
            }
        }
    }

    private static void send(PlayerEntity player, String message) {
        player.sendMessage(new StringTextComponent(message), Util.NIL_UUID);
    }

    private static int getBaseXp(LivingEntity target) {
        if (!(target instanceof MobEntity)) return 0;
        try {
            Object value = ObfuscationReflectionHelper.getPrivateValue(MobEntity.class, (MobEntity) target, "field_70728_aV");
            return value instanceof Integer ? (Integer) value : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private static void showDebugInfo(PlayerEntity player, LivingEntity target) {
        CompoundNBT nbt = target.getPersistentData();
        boolean isBuffed = nbt.getBoolean(NBT_KEY_BUFFED);
        String influencer = nbt.getString(NBT_KEY_INFLUENCER);

        double hpBase = 0, hpCurr = 0;
        double dmgBase = 0, dmgCurr = 0;
        double armBase = 0, armCurr = 0;

        ModifiableAttributeInstance hpAttr = target.getAttribute(Attributes.MAX_HEALTH);
        if (hpAttr != null) { hpBase = hpAttr.getBaseValue(); hpCurr = hpAttr.getValue(); }
        ModifiableAttributeInstance dmgAttr = target.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmgAttr != null) { dmgBase = dmgAttr.getBaseValue(); dmgCurr = dmgAttr.getValue(); }
        ModifiableAttributeInstance armorAttr = target.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) { armBase = armorAttr.getBaseValue(); armCurr = armorAttr.getValue(); }

        send(player, "§6[PTD Debug] §fEntity: " + target.getName().getString());

        ResourceLocation id = target.getType().getRegistryName();
        if (id != null) {
            send(player, "§8- ID: " + id.toString());
        }

        send(player, "§7- Buffed: " + (isBuffed ? "§aYES" : "§cNO"));
        if (isBuffed) {
            send(player, "§7- Influencer(s): §e" + influencer);
        }

        send(player, String.format("§7- HP: §8%.1f §7-> §a%.1f", hpBase, hpCurr));
        send(player, String.format("§7- Dmg: §8%.1f §7-> §c%.1f", dmgBase, dmgCurr));
        send(player, String.format("§7- Armor: §8%.1f §7-> §9%.1f", armBase, armCurr));

        long gameTime = target.level.getGameTime();
        long additionalTime = 0;
        boolean isHardcoreMode = false;
        if (target.level instanceof ServerWorld) {
            DifficultySavedData data = DifficultySavedData.get((ServerWorld) target.level);
            additionalTime = data.getAdditionalTime();
            isHardcoreMode = data.isHardcoreMode();
        }
        long playerTime = player.getPersistentData().getLong(NBT_KEY_PLAYER_TIME);

        long totalEffectiveTime = gameTime + additionalTime + playerTime;
        double factor = calculateDifficultyFactor(target.level, totalEffectiveTime);

        if (isBossEntity(target)) {
            factor *= 1.20;
            send(player, "§c[!] Boss Bonus Applied (+20%)");
            if (target instanceof IronGolemEntity) {
                factor *= 2.0;
                send(player, "§6[!] Iron Golem Balance Bonus Applied (x2)");
            }
        } else if (!(target instanceof IMob)) {
            factor *= 0.5;
        }

        send(player, String.format("§7- Your Local Factor: §b%.2f%%", factor * 100));
        send(player, "§7- Hardcore Mode: " + (isHardcoreMode ? "§cENABLED" : "§aDISABLED"));

        int baseXp = getBaseXp(target);
        int finalXp = (int) (baseXp * (1.0 + (factor * 1.2)));
        send(player, String.format("§7- XP: §8%d §7-> §d%d", baseXp, finalXp));

        if (nbt.contains(NBT_KEY_MAGIC_DMG_MULT)) {
            double magicMult = nbt.getDouble(NBT_KEY_MAGIC_DMG_MULT);
            send(player, String.format("§d- Magic Dmg Bonus: x%.2f", magicMult));
        }
        if (nbt.contains(NBT_KEY_SOUL_EATER_FACTOR)) {
            double soulEater = nbt.getDouble(NBT_KEY_SOUL_EATER_FACTOR);
            send(player, String.format("§5- Soul Eater Factor: +%.2f%%", soulEater * 100));
        }
    }

    public static boolean updateEntityBuffs(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        if (entity instanceof PlayerEntity) return false;
        LivingEntity livingEntity = (LivingEntity) entity;

        if (BLACKLIST.contains(livingEntity.getType())) {
            removeBuffs(livingEntity);
            return false;
        }

        ResourceLocation entityId = livingEntity.getType().getRegistryName();
        if (entityId != null && entityId.toString().equals("evilcraft:vengeance_spirit")) {
            removeBuffs(livingEntity);
            return false;
        }

        if ((livingEntity instanceof AnimalEntity || livingEntity instanceof WaterMobEntity ||
            livingEntity instanceof VillagerEntity || livingEntity instanceof WanderingTraderEntity) &&
            !(livingEntity instanceof IronGolemEntity)) {
            return false;
        }
        ModifiableAttributeInstance damageAttr = livingEntity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr == null || damageAttr.getBaseValue() <= 0) {
            return false;
        }

        CompoundNBT nbt = livingEntity.getPersistentData();
        if (!nbt.contains(NBT_KEY_CAN_OVERCHARGE)) {
            boolean roll = entity.level.random.nextFloat() < (1.0f / 15.0f);
            nbt.putBoolean(NBT_KEY_CAN_OVERCHARGE, roll);
        }

        long gameTime = entity.level.getGameTime();
        long playerTimeCalc = 0;
        long additionalTime = 0;
        String influencerName = "World";
        PlayerEntity influencingPlayer = null;

        if (entity.level instanceof ServerWorld) {
            ServerWorld serverLevel = (ServerWorld) entity.level;
            DifficultySavedData data = DifficultySavedData.get(serverLevel);
            additionalTime = data.getAdditionalTime();

            boolean canOvercharge = nbt.getBoolean(NBT_KEY_CAN_OVERCHARGE);
            boolean forceOvercharge = nbt.getBoolean(NBT_KEY_FORCE_OVERCHARGE);
            if (canOvercharge || forceOvercharge) {
                List<ServerPlayerEntity> nearbyPlayers = serverLevel.getPlayers(p -> p.distanceToSqr(livingEntity) < 128 * 128);
                if (!nearbyPlayers.isEmpty()) {
                    influencingPlayer = nearbyPlayers.get(0);
                    int count = 0;
                    for (ServerPlayerEntity p : nearbyPlayers) {
                        playerTimeCalc += p.getPersistentData().getLong(NBT_KEY_PLAYER_TIME);
                        count++;
                    }
                    if (count > 0) {
                        influencerName = count + " Players (Stacked)";
                    }
                }
            } else {
                PlayerEntity nearestPlayer = serverLevel.getNearestPlayer(livingEntity, 256.0);
                if (nearestPlayer == null) {
                     List<ServerPlayerEntity> players = serverLevel.getPlayers(p -> p.distanceToSqr(livingEntity) < 256 * 256);
                     if (!players.isEmpty()) nearestPlayer = players.get(0);
                }

                if (nearestPlayer instanceof ServerPlayerEntity) {
                    ServerPlayerEntity serverPlayer = (ServerPlayerEntity) nearestPlayer;
                    influencingPlayer = serverPlayer;
                    playerTimeCalc = serverPlayer.getPersistentData().getLong(NBT_KEY_PLAYER_TIME);
                    influencerName = serverPlayer.getName().getString();
                }
            }
        }

        long totalEffectiveTime = gameTime + additionalTime + playerTimeCalc;
        double difficultyFactor = calculateDifficultyFactor(entity.level, totalEffectiveTime);

        double soulEaterFactor = 0.0;
        if (influencingPlayer != null && entity.level instanceof ServerWorld && DifficultySavedData.get((ServerWorld) entity.level).isHardcoreMode()) {
            float playerMaxHealth = influencingPlayer.getMaxHealth();
            if (playerMaxHealth > 20) {
                soulEaterFactor = ((playerMaxHealth - 20) / 2.0) * 0.05;
                if (soulEaterFactor > 0 && !nbt.contains(NBT_KEY_SOUL_EATER_FACTOR)) {
                    entity.level.playSound(null, entity.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundCategory.HOSTILE, 1.0f, 1.0f);
                }
            }
        }
        nbt.putDouble(NBT_KEY_SOUL_EATER_FACTOR, soulEaterFactor);

        if (isBossEntity(livingEntity)) {
            difficultyFactor *= 1.20;
            if (livingEntity instanceof IronGolemEntity) {
                difficultyFactor *= 2.0;
            }
        } else if (!(livingEntity instanceof IMob)) {
            difficultyFactor *= 0.5;
        }

        if (difficultyFactor > 0 || soulEaterFactor > 0) {
            applyBuffs(livingEntity, difficultyFactor, soulEaterFactor);
            livingEntity.getPersistentData().putString(NBT_KEY_INFLUENCER, influencerName);
            livingEntity.getPersistentData().putBoolean(NBT_KEY_BUFFED, true);
            livingEntity.getPersistentData().putDouble(NBT_KEY_DIFFICULTY_FACTOR, difficultyFactor);
            return true;
        } else {
            removeBuffs(livingEntity);
            return true;
        }
    }

    private static boolean isBossEntity(Entity entity) {
        if (entity.getType().is(BOSS_TAG)) {
            return true;
        }

        String entityId = String.valueOf(entity.getType().getRegistryName());
        if (KNOWN_BOSS_IDS.contains(entityId)) {
            return true;
        }

        return entity instanceof WitherEntity ||
               entity instanceof EnderDragonEntity ||
               entity instanceof IronGolemEntity ||
               entity instanceof ElderGuardianEntity;
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getEntity().level.isClientSide) return;

        if (event.getEntity() instanceof ExperienceBottleEntity) {
            ExperienceBottleEntity bottle = (ExperienceBottleEntity) event.getEntity();
            if (bottle.getOwner() instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) bottle.getOwner();
                long gameTime = player.level.getGameTime();
                long playerTime = player.getPersistentData().getLong(NBT_KEY_PLAYER_TIME);
                long additionalTime = 0;
                if (player.level instanceof ServerWorld) {
                    additionalTime = DifficultySavedData.get((ServerWorld) player.level).getAdditionalTime();
                }

                long totalEffectiveTime = gameTime + additionalTime + playerTime;
                double difficultyFactor = calculateDifficultyFactor(player.level, totalEffectiveTime);

                if (difficultyFactor <= 0) return;

                int bonusXp = (int) (7 * (difficultyFactor * 5.0));
                if (bonusXp > 0) {
                    player.level.addFreshEntity(new ExperienceOrbEntity(player.level, bottle.getX(), bottle.getY(), bottle.getZ(), bonusXp));
                }
            }
        } else if (event.getEntity() instanceof AbstractArrowEntity) {
            AbstractArrowEntity arrow = (AbstractArrowEntity) event.getEntity();
            if (arrow.getOwner() instanceof LivingEntity) {
                LivingEntity shooter = (LivingEntity) arrow.getOwner();
                CompoundNBT nbt = shooter.getPersistentData();
                if (nbt.getBoolean(NBT_KEY_BUFFED)) {
                    double damageBuff = getModifierValue(shooter, Attributes.ATTACK_DAMAGE);
                    if (damageBuff > 0) {
                        double originalDamage = arrow.getBaseDamage();
                        arrow.setBaseDamage(originalDamage * (1.0 + damageBuff));
                    }
                }
            }
        }
    }

    private static double getModifierValue(LivingEntity entity, Attribute attribute) {
        ModifiableAttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            AttributeModifier mod = instance.getModifier(DMG_MOD_UUID);
            if (mod != null) return mod.getAmount();
        }
        return 0.0;
    }

    @SubscribeEvent
    public static void onLootingLevel(LootingLevelEvent event) {
        if (event.getDamageSource() != null && event.getDamageSource().getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getDamageSource().getEntity();
            LivingEntity target = event.getEntityLiving();
            ModifiableAttributeInstance damageAttr = target.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damageAttr != null && damageAttr.getBaseValue() > 0) {
                long gameTime = target.level.getGameTime();
                long playerTime = 0;
                if (player instanceof ServerPlayerEntity) {
                    playerTime = player.getPersistentData().getLong(NBT_KEY_PLAYER_TIME);
                }
                long additionalTime = 0;
                if (target.level instanceof ServerWorld) {
                    additionalTime = DifficultySavedData.get((ServerWorld) target.level).getAdditionalTime();
                }
                double factor = calculateDifficultyFactor(target.level, gameTime + playerTime + additionalTime);
                int extraLooting = Math.min(3, (int) factor);
                if (extraLooting > 0) {
                    event.setLootingLevel(event.getLootingLevel() + extraLooting);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.level.isClientSide) return;

        if (entity instanceof PlayerEntity) return;

        ModifiableAttributeInstance damageAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr == null || damageAttr.getBaseValue() <= 0) return;

        PlayerEntity player = event.getAttackingPlayer();
        if (player == null) return;

        long gameTime = entity.level.getGameTime();
        long playerTime = player.getPersistentData().getLong(NBT_KEY_PLAYER_TIME);
        long additionalTime = 0;
        if (entity.level instanceof ServerWorld) {
            additionalTime = DifficultySavedData.get((ServerWorld) entity.level).getAdditionalTime();
        }

        long totalEffectiveTime = gameTime + additionalTime + playerTime;
        double difficultyFactor = calculateDifficultyFactor(entity.level, totalEffectiveTime);

        if (difficultyFactor <= 0) return;

        int originalXp = event.getOriginalExperience();
        int newXp = (int) (originalXp * (1.0 + (difficultyFactor * 1.2)));

        event.setDroppedExperience(newXp);
    }

    public static double calculateDifficultyFactor(World level, long totalTime) {
        long effectiveTime = Math.max(0, totalTime);
        long days = effectiveTime / 24000;

        double percentagePerDay = 0.0075;

        if (level instanceof ServerWorld) {
            DifficultySavedData data = DifficultySavedData.get((ServerWorld) level);

            if (data.isHardcoreMode()) {
                percentagePerDay = 0.012;
            } else {
                switch (level.getDifficulty()) {
                    case PEACEFUL:
                        percentagePerDay = 0.0;
                        break;
                    case EASY:
                        percentagePerDay = 0.005;
                        break;
                    case NORMAL:
                        percentagePerDay = 0.0075;
                        break;
                    case HARD:
                        percentagePerDay = 0.01;
                        break;
                }
            }
        }

        return days * percentagePerDay;
    }

    private static void applyBuffs(LivingEntity entity, double factor, double soulEaterFactor) {
        boolean isHardcore = false;
        if (entity.level instanceof ServerWorld) {
            isHardcore = DifficultySavedData.get((ServerWorld) entity.level).isHardcoreMode();
        }

        ModifiableAttributeInstance healthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
        double baseHealth = 20.0;
        if (healthAttr != null) {
            baseHealth = healthAttr.getBaseValue();
            healthAttr.removeModifier(HP_MOD_UUID);
            double healthMultiplier = (factor * 1.0) + (soulEaterFactor * 0.25);
            healthAttr.addPermanentModifier(new AttributeModifier(HP_MOD_UUID, "PTD Health Buff", healthMultiplier, AttributeModifier.Operation.MULTIPLY_TOTAL));
            if (entity.getHealth() < entity.getMaxHealth()) entity.setHealth(entity.getMaxHealth());
        }

        ModifiableAttributeInstance damageAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.removeModifier(DMG_MOD_UUID);

            double damageMultiplier = 0.0;
            if (isHardcore) {
                damageMultiplier = (factor * 2.5) + soulEaterFactor;
            } else {
                switch (entity.level.getDifficulty()) {
                    case EASY:
                        damageMultiplier = 0.0;
                        break;
                    case NORMAL:
                        damageMultiplier = factor * 0.5;
                        break;
                    case HARD:
                        damageMultiplier = factor;
                        break;
                    default:
                        break;
                }
            }

            if (isBossEntity(entity)) {
                damageMultiplier *= 1.30;
                if (isHardcore) {
                    damageMultiplier *= 1.05;
                }
            }

            double physicalMultiplier = Math.min(damageMultiplier, 5.0);
            double magicMultiplier = Math.max(0, damageMultiplier - 5.0);

            if (physicalMultiplier > 0) {
                damageAttr.addPermanentModifier(new AttributeModifier(DMG_MOD_UUID, "PTD Damage Buff", physicalMultiplier, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }

            if (magicMultiplier > 0) {
                entity.getPersistentData().putDouble(NBT_KEY_MAGIC_DMG_MULT, magicMultiplier);
            } else {
                entity.getPersistentData().remove(NBT_KEY_MAGIC_DMG_MULT);
            }
        }

        ModifiableAttributeInstance armorAttr = entity.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.removeModifier(ARMOR_MOD_UUID);

            double finalMaxHealth = entity.getMaxHealth();

            if (!isBossEntity(entity) && finalMaxHealth <= 200.0) {
                double extraHealth = baseHealth * (factor * 1.0);
                double armorBuff = extraHealth / 10.0;
                armorAttr.addPermanentModifier(new AttributeModifier(ARMOR_MOD_UUID, "PTD Armor Buff", armorBuff, AttributeModifier.Operation.ADDITION));
            }
        }

        ModifiableAttributeInstance kbResAttr = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbResAttr != null) {
            kbResAttr.removeModifier(KB_MOD_UUID);

            double finalMaxHealth = entity.getMaxHealth();

            if (!isBossEntity(entity) && finalMaxHealth <= 200.0) {
                double kbBuff = 0.0;
                if (isHardcore) {
                    kbBuff = Math.min(1.0, factor * 0.075);
                } else {
                    kbBuff = Math.min(1.0, factor * 0.05);
                }
                kbResAttr.addPermanentModifier(new AttributeModifier(KB_MOD_UUID, "PTD KB Res Buff", kbBuff, AttributeModifier.Operation.ADDITION));
            }
        }
    }

    private static void removeBuffs(LivingEntity entity) {
        ModifiableAttributeInstance healthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) healthAttr.removeModifier(HP_MOD_UUID);
        ModifiableAttributeInstance damageAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) damageAttr.removeModifier(DMG_MOD_UUID);
        ModifiableAttributeInstance armorAttr = entity.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) armorAttr.removeModifier(ARMOR_MOD_UUID);
        ModifiableAttributeInstance kbResAttr = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbResAttr != null) kbResAttr.removeModifier(KB_MOD_UUID);

        entity.getPersistentData().remove(NBT_KEY_MAGIC_DMG_MULT);
        entity.getPersistentData().remove(NBT_KEY_DIFFICULTY_FACTOR);
        entity.getPersistentData().remove(NBT_KEY_SOUL_EATER_FACTOR);

        if (entity.hasCustomName() && entity.getCustomName().getString().equals("§c§lOVERCHARGED")) {
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
        }
    }
}
