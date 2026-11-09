package com.tontoque28.progressivetimedifficulty;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

@Mod.EventBusSubscriber(modid = ProgressiveTimeDifficultyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DifficultyHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String NBT_KEY_BUFFED = "ptd_buffed";
    private static final String NBT_KEY_INFLUENCER = "ptd_influencer";
    public static final String NBT_KEY_PLAYER_TIME = "ptd_player_time";
    public static final String NBT_KEY_CAN_OVERCHARGE = "ptd_can_overcharge";
    public static final String NBT_KEY_FORCE_OVERCHARGE = "ptd_force_overcharge";
    public static final String NBT_KEY_MAGIC_DMG_MULT = "ptd_magic_dmg_mult";
    private static final String NBT_KEY_DIFFICULTY_FACTOR = "ptd_factor";
    private static final String NBT_KEY_SOUL_EATER_FACTOR = "ptd_soul_eater_factor";

    private static final ResourceLocation HP_MOD_ID = ResourceLocation.fromNamespaceAndPath(ProgressiveTimeDifficultyMod.MODID, "ptd_health_buff");
    private static final ResourceLocation DMG_MOD_ID = ResourceLocation.fromNamespaceAndPath(ProgressiveTimeDifficultyMod.MODID, "ptd_damage_buff");
    private static final ResourceLocation ARMOR_MOD_ID = ResourceLocation.fromNamespaceAndPath(ProgressiveTimeDifficultyMod.MODID, "ptd_armor_buff");
    private static final ResourceLocation KB_MOD_ID = ResourceLocation.fromNamespaceAndPath(ProgressiveTimeDifficultyMod.MODID, "ptd_kb_res_buff");

    private static final List<EntityType<?>> BLACKLIST = List.of(EntityType.CREEPER);

    private static Difficulty lastDifficulty = null;

    private static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("forge", "bosses"));

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
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (event.getEntity() instanceof Player) return;
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            updateEntityBuffs(livingEntity);
        }
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (serverLevel.dimension() == Level.OVERWORLD) {
                lastDifficulty = serverLevel.getDifficulty();
            }
            DifficultySavedData.get(serverLevel);
            LOGGER.info("PTD: Loaded DifficultySavedData for level {} and synchronized DEV_MODE: {}", serverLevel.dimension().location(), ProgressiveTimeDifficultyMod.DEV_MODE);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            CompoundTag data = event.player.getPersistentData();
            long currentTime = data.getLong(NBT_KEY_PLAYER_TIME);
            data.putLong(NBT_KEY_PLAYER_TIME, currentTime + 1);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide && event.level.dimension() == Level.OVERWORLD) {
            ServerLevel serverLevel = (ServerLevel) event.level;
            Difficulty currentDiff = serverLevel.getDifficulty();

            if (lastDifficulty != null && lastDifficulty != currentDiff) {
                int count = 0;
                for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
                    for (Entity entity : level.getAllEntities()) {
                        if (updateEntityBuffs(entity)) {
                            count++;
                        }
                    }
                }

                final int finalCount = count;
                serverLevel.getServer().getPlayerList().getPlayers().forEach(player -> {
                    if (player.hasPermissions(2)) {
                        Component message = Component.literal(String.format("§e[PTD] Difficulty changed to %s. Updated %d entities.", currentDiff.getDisplayName().getString(), finalCount));
                        player.sendSystemMessage(message);
                    }
                });
            }
            lastDifficulty = currentDiff;

            if (event.level.getGameTime() % 3600 == 0) {
                for (Entity entity : serverLevel.getAllEntities()) {
                    updateEntityBuffs(entity);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (entity instanceof Player) return;

        if (entity.tickCount % 20 == 0) {
            CompoundTag nbt = entity.getPersistentData();
            if (nbt.getBoolean(NBT_KEY_BUFFED)) {

                boolean canOvercharge = nbt.getBoolean(NBT_KEY_CAN_OVERCHARGE);
                boolean forceOvercharge = nbt.getBoolean(NBT_KEY_FORCE_OVERCHARGE);
                boolean isOverchargeActive = canOvercharge || forceOvercharge;

                boolean isActiveThreat = false;
                if (entity instanceof Mob mob) {
                    isActiveThreat = mob.getTarget() instanceof Player;
                }

                if (isOverchargeActive && isActiveThreat) {
                    if (!entity.hasCustomName() || !entity.getCustomName().getString().equals("§c§lOVERCHARGED")) {
                        entity.setCustomName(Component.literal("§c§lOVERCHARGED"));
                        entity.setCustomNameVisible(true);
                    }
                } else {
                    if (entity.hasCustomName() && entity.getCustomName().getString().equals("§c§lOVERCHARGED")) {
                        entity.setCustomName(null);
                        entity.setCustomNameVisible(false);
                    }
                }

                if (entity.level() instanceof ServerLevel serverLevel) {
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
        if (event.getLevel().isClientSide) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.STICK) {
            if (player.hasPermissions(2) || player.isCreative()) {
                Entity target = event.getTarget();
                if (target instanceof LivingEntity livingTarget) {
                    showDebugInfo(player, livingTarget);
                    event.setCanceled(true);
                }
            }
        }
    }

    private static void showDebugInfo(Player player, LivingEntity target) {
        CompoundTag nbt = target.getPersistentData();
        boolean isBuffed = nbt.getBoolean(NBT_KEY_BUFFED);
        String influencer = nbt.getString(NBT_KEY_INFLUENCER);

        double hpBase = 0, hpCurr = 0;
        double dmgBase = 0, dmgCurr = 0;
        double armBase = 0, armCurr = 0;

        AttributeInstance hpAttr = target.getAttribute(Attributes.MAX_HEALTH);
        if (hpAttr != null) { hpBase = hpAttr.getBaseValue(); hpCurr = hpAttr.getValue(); }
        AttributeInstance dmgAttr = target.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmgAttr != null) { dmgBase = dmgAttr.getBaseValue(); dmgCurr = dmgAttr.getValue(); }
        AttributeInstance armorAttr = target.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) { armBase = armorAttr.getBaseValue(); armCurr = armorAttr.getValue(); }

        player.sendSystemMessage(Component.literal("§6[PTD Debug] §fEntity: " + target.getName().getString()));

        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        if (id != null) {
            player.sendSystemMessage(Component.literal("§8- ID: " + id.toString()));
        }

        player.sendSystemMessage(Component.literal("§7- Buffed: " + (isBuffed ? "§aYES" : "§cNO")));
        if (isBuffed) {
            player.sendSystemMessage(Component.literal("§7- Influencer(s): §e" + influencer));
        }

        player.sendSystemMessage(Component.literal(String.format("§7- HP: §8%.1f §7-> §a%.1f", hpBase, hpCurr)));
        player.sendSystemMessage(Component.literal(String.format("§7- Dmg: §8%.1f §7-> §c%.1f", dmgBase, dmgCurr)));
        player.sendSystemMessage(Component.literal(String.format("§7- Armor: §8%.1f §7-> §9%.1f", armBase, armCurr)));

        long gameTime = target.level().getLevelData().getGameTime();
        long additionalTime = 0;
        boolean isHardcoreMode = false;
        if (target.level() instanceof ServerLevel sl) {
            DifficultySavedData data = DifficultySavedData.get(sl);
            additionalTime = data.getAdditionalTime();
            isHardcoreMode = data.isHardcoreMode();
        }
        long playerTime = player.getPersistentData().getLong(NBT_KEY_PLAYER_TIME);

        long totalEffectiveTime = gameTime + additionalTime + playerTime;
        double factor = calculateDifficultyFactor(target.level(), totalEffectiveTime);

        if (isBossEntity(target)) {
            factor *= 1.20;
            player.sendSystemMessage(Component.literal("§c[!] Boss Bonus Applied (+20%)"));
            if (target instanceof IronGolem) {
                factor *= 2.0;
                player.sendSystemMessage(Component.literal("§6[!] Iron Golem Balance Bonus Applied (x2)"));
            }
        } else if (!(target instanceof Enemy)) {
            factor *= 0.5;
        }

        player.sendSystemMessage(Component.literal(String.format("§7- Your Local Factor: §b%.2f%%", factor * 100)));
        player.sendSystemMessage(Component.literal("§7- Hardcore Mode: " + (isHardcoreMode ? "§cENABLED" : "§aDISABLED")));

        int baseXp = target.level() instanceof ServerLevel xpLevel ? target.getExperienceReward(xpLevel, player) : 0;
        int finalXp = (int) (baseXp * (1.0 + (factor * 1.2)));
        player.sendSystemMessage(Component.literal(String.format("§7- XP: §8%d §7-> §d%d", baseXp, finalXp)));

        if (nbt.contains(NBT_KEY_MAGIC_DMG_MULT)) {
            double magicMult = nbt.getDouble(NBT_KEY_MAGIC_DMG_MULT);
            player.sendSystemMessage(Component.literal(String.format("§d- Magic Dmg Bonus: x%.2f", magicMult)));
        }
        if (nbt.contains(NBT_KEY_SOUL_EATER_FACTOR)) {
            double soulEater = nbt.getDouble(NBT_KEY_SOUL_EATER_FACTOR);
            player.sendSystemMessage(Component.literal(String.format("§5- Soul Eater Factor: +%.2f%%", soulEater * 100)));
        }
    }

    public static boolean updateEntityBuffs(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) return false;
        if (entity instanceof Player) return false;

        if (BLACKLIST.contains(livingEntity.getType())) {
            removeBuffs(livingEntity);
            return false;
        }

        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(livingEntity.getType());
        if (entityId != null && entityId.toString().equals("evilcraft:vengeance_spirit")) {
            removeBuffs(livingEntity);
            return false;
        }

        if ((livingEntity instanceof Animal || livingEntity instanceof WaterAnimal ||
            livingEntity instanceof Villager || livingEntity instanceof WanderingTrader) &&
            !(livingEntity instanceof IronGolem)) {
            return false;
        }
        AttributeInstance damageAttr = livingEntity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr == null || damageAttr.getBaseValue() <= 0) {
            return false;
        }

        CompoundTag nbt = livingEntity.getPersistentData();
        if (!nbt.contains(NBT_KEY_CAN_OVERCHARGE)) {
            boolean roll = entity.level().random.nextFloat() < (1.0f / 15.0f);
            nbt.putBoolean(NBT_KEY_CAN_OVERCHARGE, roll);
        }

        long gameTime = entity.level().getLevelData().getGameTime();
        long playerTimeCalc = 0;
        long additionalTime = 0;
        String influencerName = "World";
        Player influencingPlayer = null;

        if (entity.level() instanceof ServerLevel serverLevel) {
            DifficultySavedData data = DifficultySavedData.get(serverLevel);
            additionalTime = data.getAdditionalTime();

            boolean canOvercharge = nbt.getBoolean(NBT_KEY_CAN_OVERCHARGE);
            boolean forceOvercharge = nbt.getBoolean(NBT_KEY_FORCE_OVERCHARGE);
            if (canOvercharge || forceOvercharge) {
                List<ServerPlayer> nearbyPlayers = serverLevel.getPlayers(p -> p.distanceToSqr(livingEntity) < 128 * 128);
                if (!nearbyPlayers.isEmpty()) {
                    influencingPlayer = nearbyPlayers.get(0);
                    int count = 0;
                    for (ServerPlayer p : nearbyPlayers) {
                        playerTimeCalc += p.getPersistentData().getLong(NBT_KEY_PLAYER_TIME);
                        count++;
                    }
                    if (count > 0) {
                        influencerName = count + " Players (Stacked)";
                    }
                }
            } else {
                Player nearestPlayer = serverLevel.getNearestPlayer(livingEntity, 256.0);
                if (nearestPlayer == null) {
                     List<ServerPlayer> players = serverLevel.getPlayers(p -> p.distanceToSqr(livingEntity) < 256 * 256);
                     if (!players.isEmpty()) nearestPlayer = players.get(0);
                }

                if (nearestPlayer instanceof ServerPlayer serverPlayer) {
                    influencingPlayer = serverPlayer;
                    playerTimeCalc = serverPlayer.getPersistentData().getLong(NBT_KEY_PLAYER_TIME);
                    influencerName = serverPlayer.getName().getString();
                }
            }
        }

        long totalEffectiveTime = gameTime + additionalTime + playerTimeCalc;
        double difficultyFactor = calculateDifficultyFactor(entity.level(), totalEffectiveTime);

        double soulEaterFactor = 0.0;
        if (influencingPlayer != null && entity.level() instanceof ServerLevel sl && DifficultySavedData.get(sl).isHardcoreMode()) {
            float playerMaxHealth = influencingPlayer.getMaxHealth();
            if (playerMaxHealth > 20) {
                soulEaterFactor = ((playerMaxHealth - 20) / 2.0) * 0.05;
                if (soulEaterFactor > 0 && !nbt.contains(NBT_KEY_SOUL_EATER_FACTOR)) {
                    entity.level().playSound(null, entity.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 1.0f, 1.0f);
                }
            }
        }
        nbt.putDouble(NBT_KEY_SOUL_EATER_FACTOR, soulEaterFactor);

        if (isBossEntity(livingEntity)) {
            difficultyFactor *= 1.20;
            if (livingEntity instanceof IronGolem) {
                difficultyFactor *= 2.0;
            }
        } else if (!(livingEntity instanceof Enemy)) {
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

        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        if (KNOWN_BOSS_IDS.contains(entityId)) {
            return true;
        }

        return entity instanceof WitherBoss ||
               entity instanceof EnderDragon ||
               entity instanceof Warden ||
               entity instanceof IronGolem ||
               entity instanceof ElderGuardian;
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile().level().isClientSide()) return;

        if (event.getProjectile() instanceof ThrownExperienceBottle bottle) {
            if (bottle.getOwner() instanceof Player player) {
                long gameTime = player.level().getLevelData().getGameTime();
                long playerTime = player.getPersistentData().getLong(NBT_KEY_PLAYER_TIME);
                long additionalTime = 0;
                if (player.level() instanceof ServerLevel serverLevel) {
                    additionalTime = DifficultySavedData.get(serverLevel).getAdditionalTime();
                }

                long totalEffectiveTime = gameTime + additionalTime + playerTime;
                double difficultyFactor = calculateDifficultyFactor(player.level(), totalEffectiveTime);

                if (difficultyFactor <= 0) return;

                int bonusXp = (int) (7 * (difficultyFactor * 5.0));
                if (bonusXp > 0) {
                    ExperienceOrb.award((ServerLevel) player.level(), bottle.position(), bonusXp);
                }
            }
        } else if (event.getProjectile() instanceof AbstractArrow arrow) {
            if (arrow.getOwner() instanceof LivingEntity shooter) {
                CompoundTag nbt = shooter.getPersistentData();
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

    private static double getModifierValue(LivingEntity entity, Holder<Attribute> attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            AttributeModifier mod = instance.getModifier(DMG_MOD_ID);
            if (mod != null) return mod.amount();
        }
        return 0.0;
    }

    @SubscribeEvent
    public static void onLootingLevel(LootingLevelEvent event) {
        if (event.getDamageSource() != null && event.getDamageSource().getEntity() instanceof Player player) {
            LivingEntity target = event.getEntity();
            AttributeInstance damageAttr = target.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damageAttr != null && damageAttr.getBaseValue() > 0) {
                long gameTime = target.level().getLevelData().getGameTime();
                long playerTime = 0;
                if (player instanceof ServerPlayer serverPlayer) {
                    playerTime = serverPlayer.getPersistentData().getLong(NBT_KEY_PLAYER_TIME);
                }
                long additionalTime = 0;
                if (target.level() instanceof ServerLevel serverLevel) {
                    additionalTime = DifficultySavedData.get(serverLevel).getAdditionalTime();
                }
                double factor = calculateDifficultyFactor(target.level(), gameTime + playerTime + additionalTime);
                int extraLooting = Math.min(3, (int) factor);
                if (extraLooting > 0) {
                    event.setLootingLevel(event.getLootingLevel() + extraLooting);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        if (entity instanceof Player) return;

        AttributeInstance damageAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr == null || damageAttr.getBaseValue() <= 0) return;

        Player player = event.getAttackingPlayer();
        if (player == null) return;

        long gameTime = entity.level().getLevelData().getGameTime();
        long playerTime = player.getPersistentData().getLong(NBT_KEY_PLAYER_TIME);
        long additionalTime = 0;
        if (entity.level() instanceof ServerLevel serverLevel) {
            additionalTime = DifficultySavedData.get(serverLevel).getAdditionalTime();
        }

        long totalEffectiveTime = gameTime + additionalTime + playerTime;
        double difficultyFactor = calculateDifficultyFactor(entity.level(), totalEffectiveTime);

        if (difficultyFactor <= 0) return;

        int originalXp = event.getOriginalExperience();
        int newXp = (int) (originalXp * (1.0 + (difficultyFactor * 1.2)));

        event.setDroppedExperience(newXp);
    }

    public static double calculateDifficultyFactor(Level level, long totalTime) {
        long effectiveTime = Math.max(0, totalTime);
        long days = effectiveTime / 24000;

        double percentagePerDay = 0.0075;

        if (level instanceof ServerLevel serverLevel) {
            DifficultySavedData data = DifficultySavedData.get(serverLevel);

            if (data.isHardcoreMode()) {
                percentagePerDay = 0.012;
            } else {
                switch (level.getDifficulty()) {
                    case PEACEFUL -> percentagePerDay = 0.0;
                    case EASY -> percentagePerDay = 0.005;
                    case NORMAL -> percentagePerDay = 0.0075;
                    case HARD -> percentagePerDay = 0.01;
                }
            }
        }

        return days * percentagePerDay;
    }

    private static void applyBuffs(LivingEntity entity, double factor, double soulEaterFactor) {
        boolean isHardcore = false;
        if (entity.level() instanceof ServerLevel sl) {
            isHardcore = DifficultySavedData.get(sl).isHardcoreMode();
        }

        AttributeInstance healthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
        double baseHealth = 20.0;
        if (healthAttr != null) {
            baseHealth = healthAttr.getBaseValue();
            healthAttr.removeModifier(HP_MOD_ID);
            double healthMultiplier = (factor * 1.0) + (soulEaterFactor * 0.25);
            healthAttr.addPermanentModifier(new AttributeModifier(HP_MOD_ID, healthMultiplier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            if (entity.getHealth() < entity.getMaxHealth()) entity.setHealth(entity.getMaxHealth());
        }

        AttributeInstance damageAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.removeModifier(DMG_MOD_ID);

            double damageMultiplier = 0.0;
            if (isHardcore) {
                damageMultiplier = (factor * 2.5) + soulEaterFactor;
            } else {
                switch (entity.level().getDifficulty()) {
                    case EASY -> damageMultiplier = 0.0;
                    case NORMAL -> damageMultiplier = factor * 0.5;
                    case HARD -> damageMultiplier = factor;
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
                damageAttr.addPermanentModifier(new AttributeModifier(DMG_MOD_ID, physicalMultiplier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }

            if (magicMultiplier > 0) {
                entity.getPersistentData().putDouble(NBT_KEY_MAGIC_DMG_MULT, magicMultiplier);
            } else {
                entity.getPersistentData().remove(NBT_KEY_MAGIC_DMG_MULT);
            }
        }

        AttributeInstance armorAttr = entity.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.removeModifier(ARMOR_MOD_ID);

            double finalMaxHealth = entity.getMaxHealth();

            if (!isBossEntity(entity) && finalMaxHealth <= 200.0) {
                double extraHealth = baseHealth * (factor * 1.0);
                double armorBuff = extraHealth / 10.0;
                armorAttr.addPermanentModifier(new AttributeModifier(ARMOR_MOD_ID, armorBuff, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        AttributeInstance kbResAttr = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbResAttr != null) {
            kbResAttr.removeModifier(KB_MOD_ID);

            double finalMaxHealth = entity.getMaxHealth();

            if (!isBossEntity(entity) && finalMaxHealth <= 200.0) {
                double kbBuff = 0.0;
                if (isHardcore) {
                    kbBuff = Math.min(1.0, factor * 0.075);
                } else {
                    kbBuff = Math.min(1.0, factor * 0.05);
                }
                kbResAttr.addPermanentModifier(new AttributeModifier(KB_MOD_ID, kbBuff, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    private static void removeBuffs(LivingEntity entity) {
        AttributeInstance healthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) healthAttr.removeModifier(HP_MOD_ID);
        AttributeInstance damageAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) damageAttr.removeModifier(DMG_MOD_ID);
        AttributeInstance armorAttr = entity.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) armorAttr.removeModifier(ARMOR_MOD_ID);
        AttributeInstance kbResAttr = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbResAttr != null) kbResAttr.removeModifier(KB_MOD_ID);

        entity.getPersistentData().remove(NBT_KEY_MAGIC_DMG_MULT);
        entity.getPersistentData().remove(NBT_KEY_DIFFICULTY_FACTOR);
        entity.getPersistentData().remove(NBT_KEY_SOUL_EATER_FACTOR);

        if (entity.hasCustomName() && entity.getCustomName().getString().equals("§c§lOVERCHARGED")) {
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
        }
    }
}
