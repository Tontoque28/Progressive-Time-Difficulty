package com.tontoque28.progressivetimedifficulty;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityElderGuardian;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ProgressiveTimeDifficultyMod.MODID)
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
    private static final String OVERCHARGED_NAME = "§c§lOVERCHARGED";

    private static final UUID HP_MOD_UUID = UUID.fromString("c0d0b000-0000-0000-0000-000000000001");
    private static final UUID DMG_MOD_UUID = UUID.fromString("c0d0b000-0000-0000-0000-000000000002");
    private static final UUID ARMOR_MOD_UUID = UUID.fromString("c0d0b000-0000-0000-0000-000000000003");
    private static final UUID KB_MOD_UUID = UUID.fromString("c0d0b000-0000-0000-0000-000000000004");

    private static final ResourceLocation CREEPER_ID = new ResourceLocation("minecraft", "creeper");

    private static EnumDifficulty lastDifficulty = null;

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
        if (event.getWorld().isRemote) return;
        if (event.getEntity() instanceof EntityPlayer) return;
        if (event.getEntity() instanceof EntityLivingBase) {
            updateEntityBuffs(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLevelLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        if (world instanceof WorldServer) {
            if (world.provider.getDimension() == 0) {
                lastDifficulty = world.getDifficulty();
            }
            DifficultySavedData.get(world);
            LOGGER.info("PTD: Loaded DifficultySavedData for level {} and synchronized DEV_MODE: {}", world.provider.getDimension(), ProgressiveTimeDifficultyMod.DEV_MODE);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntityPlayer().world.isRemote) return;
        long old = event.getOriginal().getEntityData().getLong(NBT_KEY_PLAYER_TIME);
        if (old > event.getEntityPlayer().getEntityData().getLong(NBT_KEY_PLAYER_TIME)) {
            event.getEntityPlayer().getEntityData().setLong(NBT_KEY_PLAYER_TIME, old);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.world.isRemote) {
            NBTTagCompound data = event.player.getEntityData();
            long currentTime = data.getLong(NBT_KEY_PLAYER_TIME);
            data.setLong(NBT_KEY_PLAYER_TIME, currentTime + 1);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isRemote && event.world.provider.getDimension() == 0) {
            WorldServer serverLevel = (WorldServer) event.world;
            EnumDifficulty currentDiff = serverLevel.getDifficulty();
            MinecraftServer server = serverLevel.getMinecraftServer();

            if (lastDifficulty != null && lastDifficulty != currentDiff) {
                int count = 0;
                for (WorldServer level : server.worlds) {
                    for (Entity entity : new ArrayList<>(level.loadedEntityList)) {
                        if (updateEntityBuffs(entity)) {
                            count++;
                        }
                    }
                }

                final int finalCount = count;
                for (EntityPlayerMP player : new ArrayList<>(server.getPlayerList().getPlayers())) {
                    if (player.canUseCommand(2, "")) {
                        send(player, String.format("§e[PTD] Difficulty changed to %s. Updated %d entities.", currentDiff.name().toLowerCase(), finalCount));
                    }
                }
            }
            lastDifficulty = currentDiff;

            if (event.world.getTotalWorldTime() % 3600 == 0) {
                for (Entity entity : new ArrayList<>(serverLevel.loadedEntityList)) {
                    updateEntityBuffs(entity);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) return;
        if (entity instanceof EntityPlayer) return;

        if (entity.ticksExisted % 20 == 0) {
            NBTTagCompound nbt = entity.getEntityData();
            if (nbt.getBoolean(NBT_KEY_BUFFED)) {

                boolean canOvercharge = nbt.getBoolean(NBT_KEY_CAN_OVERCHARGE);
                boolean forceOvercharge = nbt.getBoolean(NBT_KEY_FORCE_OVERCHARGE);
                boolean isOverchargeActive = canOvercharge || forceOvercharge;

                boolean isActiveThreat = false;
                if (entity instanceof EntityLiving) {
                    isActiveThreat = ((EntityLiving) entity).getAttackTarget() instanceof EntityPlayer;
                }

                if (isOverchargeActive && isActiveThreat) {
                    if (!entity.hasCustomName() || !entity.getCustomNameTag().equals(OVERCHARGED_NAME)) {
                        entity.setCustomNameTag(OVERCHARGED_NAME);
                        entity.setAlwaysRenderNameTag(true);
                    }
                } else {
                    if (entity.hasCustomName() && entity.getCustomNameTag().equals(OVERCHARGED_NAME)) {
                        entity.setCustomNameTag("");
                        entity.setAlwaysRenderNameTag(false);
                    }
                }

                if (entity.world instanceof WorldServer) {
                    WorldServer serverLevel = (WorldServer) entity.world;
                    if (nbt.getDouble(NBT_KEY_SOUL_EATER_FACTOR) > 0) {
                        serverLevel.spawnParticle(EnumParticleTypes.SPELL_WITCH, entity.posX, entity.posY + entity.height * 0.5, entity.posZ, 3, entity.width * 0.5, entity.height * 0.5, entity.width * 0.5, 0.05);
                    } else {
                        double factor = nbt.getDouble(NBT_KEY_DIFFICULTY_FACTOR);
                        if (factor >= 2.0) {
                            serverLevel.spawnParticle(EnumParticleTypes.CRIT_MAGIC, entity.posX, entity.posY + entity.height + 0.5, entity.posZ, 1, 0.2, 0.2, 0.2, 0.02);
                        } else if (factor >= 1.0) {
                            serverLevel.spawnParticle(EnumParticleTypes.FLAME, entity.posX, entity.posY + entity.height + 0.5, entity.posZ, 1, 0.2, 0.2, 0.2, 0.02);
                        } else if (factor >= 0.5) {
                            serverLevel.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, entity.posX, entity.posY + entity.height + 0.5, entity.posZ, 1, 0.2, 0.2, 0.2, 0.02);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!ProgressiveTimeDifficultyMod.DEV_MODE) return;
        if (event.getWorld().isRemote) return;
        if (event.getHand() != EnumHand.MAIN_HAND) return;

        EntityPlayer player = event.getEntityPlayer();
        if (player.getHeldItem(EnumHand.MAIN_HAND).getItem() == Items.STICK) {
            if (player.canUseCommand(2, "") || player.isCreative()) {
                Entity target = event.getTarget();
                if (target instanceof EntityLivingBase) {
                    showDebugInfo(player, (EntityLivingBase) target);
                    event.setCanceled(true);
                }
            }
        }
    }

    private static void send(EntityPlayer player, String message) {
        player.sendMessage(new TextComponentString(message));
    }

    private static int getBaseXp(EntityLivingBase target) {
        if (!(target instanceof EntityLiving)) return 0;
        try {
            Object value = ReflectionHelper.getPrivateValue(EntityLiving.class, (EntityLiving) target, "field_70728_aV", "experienceValue");
            return value instanceof Integer ? (Integer) value : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private static void showDebugInfo(EntityPlayer player, EntityLivingBase target) {
        NBTTagCompound nbt = target.getEntityData();
        boolean isBuffed = nbt.getBoolean(NBT_KEY_BUFFED);
        String influencer = nbt.getString(NBT_KEY_INFLUENCER);

        double hpBase = 0, hpCurr = 0;
        double dmgBase = 0, dmgCurr = 0;
        double armBase = 0, armCurr = 0;

        IAttributeInstance hpAttr = target.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (hpAttr != null) { hpBase = hpAttr.getBaseValue(); hpCurr = hpAttr.getAttributeValue(); }
        IAttributeInstance dmgAttr = target.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (dmgAttr != null) { dmgBase = dmgAttr.getBaseValue(); dmgCurr = dmgAttr.getAttributeValue(); }
        IAttributeInstance armorAttr = target.getEntityAttribute(SharedMonsterAttributes.ARMOR);
        if (armorAttr != null) { armBase = armorAttr.getBaseValue(); armCurr = armorAttr.getAttributeValue(); }

        send(player, "§6[PTD Debug] §fEntity: " + target.getName());

        ResourceLocation id = EntityList.getKey(target);
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

        long gameTime = target.world.getTotalWorldTime();
        long additionalTime = 0;
        boolean isHardcoreMode = false;
        if (target.world instanceof WorldServer) {
            DifficultySavedData data = DifficultySavedData.get(target.world);
            additionalTime = data.getAdditionalTime();
            isHardcoreMode = data.isHardcoreMode();
        }
        long playerTime = player.getEntityData().getLong(NBT_KEY_PLAYER_TIME);

        long totalEffectiveTime = gameTime + additionalTime + playerTime;
        double factor = calculateDifficultyFactor(target.world, totalEffectiveTime);

        if (isBossEntity(target)) {
            factor *= 1.20;
            send(player, "§c[!] Boss Bonus Applied (+20%)");
            if (target instanceof EntityIronGolem) {
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

        if (nbt.hasKey(NBT_KEY_MAGIC_DMG_MULT)) {
            double magicMult = nbt.getDouble(NBT_KEY_MAGIC_DMG_MULT);
            send(player, String.format("§d- Magic Dmg Bonus: x%.2f", magicMult));
        }
        if (nbt.hasKey(NBT_KEY_SOUL_EATER_FACTOR)) {
            double soulEater = nbt.getDouble(NBT_KEY_SOUL_EATER_FACTOR);
            send(player, String.format("§5- Soul Eater Factor: +%.2f%%", soulEater * 100));
        }
    }

    public static boolean updateEntityBuffs(Entity entity) {
        if (!(entity instanceof EntityLivingBase)) return false;
        if (entity instanceof EntityPlayer) return false;
        EntityLivingBase livingEntity = (EntityLivingBase) entity;

        ResourceLocation entityId = EntityList.getKey(livingEntity);

        if (CREEPER_ID.equals(entityId)) {
            removeBuffs(livingEntity);
            return false;
        }

        if (entityId != null && entityId.toString().equals("evilcraft:vengeance_spirit")) {
            removeBuffs(livingEntity);
            return false;
        }

        if ((livingEntity instanceof EntityAnimal || livingEntity instanceof EntityWaterMob ||
            livingEntity instanceof EntityVillager) &&
            !(livingEntity instanceof EntityIronGolem)) {
            return false;
        }
        IAttributeInstance damageAttr = livingEntity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (!(livingEntity instanceof EntityIronGolem) && (damageAttr == null || damageAttr.getBaseValue() <= 0)) {
            return false;
        }

        NBTTagCompound nbt = livingEntity.getEntityData();
        if (!nbt.hasKey(NBT_KEY_CAN_OVERCHARGE)) {
            boolean roll = entity.world.rand.nextFloat() < (1.0f / 15.0f);
            nbt.setBoolean(NBT_KEY_CAN_OVERCHARGE, roll);
        }

        long gameTime = entity.world.getTotalWorldTime();
        long playerTimeCalc = 0;
        long additionalTime = 0;
        String influencerName = "World";
        EntityPlayer influencingPlayer = null;

        if (entity.world instanceof WorldServer) {
            WorldServer serverLevel = (WorldServer) entity.world;
            DifficultySavedData data = DifficultySavedData.get(serverLevel);
            additionalTime = data.getAdditionalTime();

            boolean canOvercharge = nbt.getBoolean(NBT_KEY_CAN_OVERCHARGE);
            boolean forceOvercharge = nbt.getBoolean(NBT_KEY_FORCE_OVERCHARGE);
            if (canOvercharge || forceOvercharge) {
                List<EntityPlayerMP> nearbyPlayers = playersWithin(serverLevel, livingEntity, 128 * 128);
                if (!nearbyPlayers.isEmpty()) {
                    influencingPlayer = nearbyPlayers.get(0);
                    int count = 0;
                    for (EntityPlayerMP p : nearbyPlayers) {
                        playerTimeCalc += p.getEntityData().getLong(NBT_KEY_PLAYER_TIME);
                        count++;
                    }
                    if (count > 0) {
                        influencerName = count + " Players (Stacked)";
                    }
                }
            } else {
                EntityPlayer nearestPlayer = serverLevel.getClosestPlayerToEntity(livingEntity, 256.0);
                if (nearestPlayer == null) {
                    List<EntityPlayerMP> players = playersWithin(serverLevel, livingEntity, 256 * 256);
                    if (!players.isEmpty()) nearestPlayer = players.get(0);
                }

                if (nearestPlayer instanceof EntityPlayerMP) {
                    EntityPlayerMP serverPlayer = (EntityPlayerMP) nearestPlayer;
                    influencingPlayer = serverPlayer;
                    playerTimeCalc = serverPlayer.getEntityData().getLong(NBT_KEY_PLAYER_TIME);
                    influencerName = serverPlayer.getName();
                }
            }
        }

        long totalEffectiveTime = gameTime + additionalTime + playerTimeCalc;
        double difficultyFactor = calculateDifficultyFactor(entity.world, totalEffectiveTime);

        double soulEaterFactor = 0.0;
        if (influencingPlayer != null && entity.world instanceof WorldServer && DifficultySavedData.get(entity.world).isHardcoreMode()) {
            float playerMaxHealth = influencingPlayer.getMaxHealth();
            if (playerMaxHealth > 20) {
                soulEaterFactor = ((playerMaxHealth - 20) / 2.0) * 0.05;
                if (soulEaterFactor > 0 && !nbt.hasKey(NBT_KEY_SOUL_EATER_FACTOR)) {
                    entity.world.playSound(null, entity.getPosition(), SoundEvents.ENTITY_VEX_AMBIENT, SoundCategory.HOSTILE, 1.0f, 1.0f);
                }
            }
        }
        nbt.setDouble(NBT_KEY_SOUL_EATER_FACTOR, soulEaterFactor);

        if (isBossEntity(livingEntity)) {
            difficultyFactor *= 1.20;
            if (livingEntity instanceof EntityIronGolem) {
                difficultyFactor *= 2.0;
            }
        } else if (!(livingEntity instanceof IMob)) {
            difficultyFactor *= 0.5;
        }

        if (difficultyFactor > 0 || soulEaterFactor > 0) {
            applyBuffs(livingEntity, difficultyFactor, soulEaterFactor);
            livingEntity.getEntityData().setString(NBT_KEY_INFLUENCER, influencerName);
            livingEntity.getEntityData().setBoolean(NBT_KEY_BUFFED, true);
            livingEntity.getEntityData().setDouble(NBT_KEY_DIFFICULTY_FACTOR, difficultyFactor);
            return true;
        } else {
            removeBuffs(livingEntity);
            return true;
        }
    }

    private static List<EntityPlayerMP> playersWithin(WorldServer level, Entity entity, double distanceSq) {
        List<EntityPlayerMP> result = new ArrayList<>();
        for (EntityPlayer p : new ArrayList<>(level.playerEntities)) {
            if (p instanceof EntityPlayerMP && p.getDistanceSq(entity) < distanceSq) {
                result.add((EntityPlayerMP) p);
            }
        }
        return result;
    }

    private static boolean isBossEntity(Entity entity) {
        ResourceLocation id = EntityList.getKey(entity);
        if (id != null && KNOWN_BOSS_IDS.contains(id.toString())) {
            return true;
        }

        return entity instanceof EntityWither ||
               entity instanceof EntityDragon ||
               entity instanceof EntityIronGolem ||
               entity instanceof EntityElderGuardian;
    }

    @SubscribeEvent
    public static void onBottleImpact(ProjectileImpactEvent.Throwable event) {
        if (event.getEntity().world.isRemote) return;

        if (event.getThrowable() instanceof EntityExpBottle) {
            EntityExpBottle bottle = (EntityExpBottle) event.getThrowable();
            if (bottle.getThrower() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) bottle.getThrower();
                long gameTime = player.world.getTotalWorldTime();
                long playerTime = player.getEntityData().getLong(NBT_KEY_PLAYER_TIME);
                long additionalTime = 0;
                if (player.world instanceof WorldServer) {
                    additionalTime = DifficultySavedData.get(player.world).getAdditionalTime();
                }

                long totalEffectiveTime = gameTime + additionalTime + playerTime;
                double difficultyFactor = calculateDifficultyFactor(player.world, totalEffectiveTime);

                if (difficultyFactor <= 0) return;

                int bonusXp = (int) (7 * (difficultyFactor * 5.0));
                if (bonusXp > 0) {
                    player.world.spawnEntity(new EntityXPOrb(player.world, bottle.posX, bottle.posY, bottle.posZ, bonusXp));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onArrowImpact(ProjectileImpactEvent.Arrow event) {
        if (event.getEntity().world.isRemote) return;

        EntityArrow arrow = event.getArrow();
        if (arrow.shootingEntity instanceof EntityLivingBase) {
            EntityLivingBase shooter = (EntityLivingBase) arrow.shootingEntity;
            NBTTagCompound nbt = shooter.getEntityData();
            if (nbt.getBoolean(NBT_KEY_BUFFED)) {
                double damageBuff = getModifierValue(shooter, SharedMonsterAttributes.ATTACK_DAMAGE);
                if (damageBuff > 0) {
                    double originalDamage = arrow.getDamage();
                    arrow.setDamage(originalDamage * (1.0 + damageBuff));
                }
            }
        }
    }

    private static double getModifierValue(EntityLivingBase entity, IAttribute attribute) {
        IAttributeInstance instance = entity.getEntityAttribute(attribute);
        if (instance != null) {
            AttributeModifier mod = instance.getModifier(DMG_MOD_UUID);
            if (mod != null) return mod.getAmount();
        }
        return 0.0;
    }

    @SubscribeEvent
    public static void onLootingLevel(LootingLevelEvent event) {
        if (event.getDamageSource() != null && event.getDamageSource().getTrueSource() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getDamageSource().getTrueSource();
            EntityLivingBase target = event.getEntityLiving();
            IAttributeInstance damageAttr = target.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
            if (damageAttr != null && damageAttr.getBaseValue() > 0) {
                long gameTime = target.world.getTotalWorldTime();
                long playerTime = 0;
                if (player instanceof EntityPlayerMP) {
                    playerTime = player.getEntityData().getLong(NBT_KEY_PLAYER_TIME);
                }
                long additionalTime = 0;
                if (target.world instanceof WorldServer) {
                    additionalTime = DifficultySavedData.get(target.world).getAdditionalTime();
                }
                double factor = calculateDifficultyFactor(target.world, gameTime + playerTime + additionalTime);
                int extraLooting = Math.min(3, (int) factor);
                if (extraLooting > 0) {
                    event.setLootingLevel(event.getLootingLevel() + extraLooting);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) return;

        if (entity instanceof EntityPlayer) return;

        IAttributeInstance damageAttr = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (damageAttr == null || damageAttr.getBaseValue() <= 0) return;

        EntityPlayer player = event.getAttackingPlayer();
        if (player == null) return;

        long gameTime = entity.world.getTotalWorldTime();
        long playerTime = player.getEntityData().getLong(NBT_KEY_PLAYER_TIME);
        long additionalTime = 0;
        if (entity.world instanceof WorldServer) {
            additionalTime = DifficultySavedData.get(entity.world).getAdditionalTime();
        }

        long totalEffectiveTime = gameTime + additionalTime + playerTime;
        double difficultyFactor = calculateDifficultyFactor(entity.world, totalEffectiveTime);

        if (difficultyFactor <= 0) return;

        int originalXp = event.getOriginalExperience();
        int newXp = (int) (originalXp * (1.0 + (difficultyFactor * 1.2)));

        event.setDroppedExperience(newXp);
    }

    public static double calculateDifficultyFactor(World level, long totalTime) {
        long effectiveTime = Math.max(0, totalTime);
        long days = effectiveTime / 24000;

        double percentagePerDay = 0.0075;

        if (level instanceof WorldServer) {
            DifficultySavedData data = DifficultySavedData.get(level);

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

    private static void applyBuffs(EntityLivingBase entity, double factor, double soulEaterFactor) {
        boolean isHardcore = false;
        if (entity.world instanceof WorldServer) {
            isHardcore = DifficultySavedData.get(entity.world).isHardcoreMode();
        }

        IAttributeInstance healthAttr = entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        double baseHealth = 20.0;
        if (healthAttr != null) {
            baseHealth = healthAttr.getBaseValue();
            healthAttr.removeModifier(HP_MOD_UUID);
            double healthMultiplier = (factor * 1.0) + (soulEaterFactor * 0.25);
            healthAttr.applyModifier(new AttributeModifier(HP_MOD_UUID, "PTD Health Buff", healthMultiplier, 2));
            if (entity.getHealth() < entity.getMaxHealth()) entity.setHealth(entity.getMaxHealth());
        }

        IAttributeInstance damageAttr = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.removeModifier(DMG_MOD_UUID);

            double damageMultiplier = 0.0;
            if (isHardcore) {
                damageMultiplier = (factor * 2.5) + soulEaterFactor;
            } else {
                switch (entity.world.getDifficulty()) {
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
                damageAttr.applyModifier(new AttributeModifier(DMG_MOD_UUID, "PTD Damage Buff", physicalMultiplier, 2));
            }

            if (magicMultiplier > 0) {
                entity.getEntityData().setDouble(NBT_KEY_MAGIC_DMG_MULT, magicMultiplier);
            } else {
                entity.getEntityData().removeTag(NBT_KEY_MAGIC_DMG_MULT);
            }
        }

        IAttributeInstance armorAttr = entity.getEntityAttribute(SharedMonsterAttributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.removeModifier(ARMOR_MOD_UUID);

            double finalMaxHealth = entity.getMaxHealth();

            if (!isBossEntity(entity) && finalMaxHealth <= 200.0) {
                double extraHealth = baseHealth * (factor * 1.0);
                double armorBuff = extraHealth / 10.0;
                armorAttr.applyModifier(new AttributeModifier(ARMOR_MOD_UUID, "PTD Armor Buff", armorBuff, 0));
            }
        }

        IAttributeInstance kbResAttr = entity.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
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
                kbResAttr.applyModifier(new AttributeModifier(KB_MOD_UUID, "PTD KB Res Buff", kbBuff, 0));
            }
        }
    }

    private static void removeBuffs(EntityLivingBase entity) {
        IAttributeInstance healthAttr = entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (healthAttr != null) healthAttr.removeModifier(HP_MOD_UUID);
        IAttributeInstance damageAttr = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (damageAttr != null) damageAttr.removeModifier(DMG_MOD_UUID);
        IAttributeInstance armorAttr = entity.getEntityAttribute(SharedMonsterAttributes.ARMOR);
        if (armorAttr != null) armorAttr.removeModifier(ARMOR_MOD_UUID);
        IAttributeInstance kbResAttr = entity.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
        if (kbResAttr != null) kbResAttr.removeModifier(KB_MOD_UUID);

        entity.getEntityData().removeTag(NBT_KEY_MAGIC_DMG_MULT);
        entity.getEntityData().removeTag(NBT_KEY_DIFFICULTY_FACTOR);
        entity.getEntityData().removeTag(NBT_KEY_SOUL_EATER_FACTOR);

        if (entity.hasCustomName() && entity.getCustomNameTag().equals(OVERCHARGED_NAME)) {
            entity.setCustomNameTag("");
            entity.setAlwaysRenderNameTag(false);
        }
    }
}
