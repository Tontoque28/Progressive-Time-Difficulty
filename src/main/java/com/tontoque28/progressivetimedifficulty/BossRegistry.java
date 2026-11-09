package com.tontoque28.progressivetimedifficulty;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityElderGuardian;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class BossRegistry {

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
        KNOWN_BOSS_IDS.add("block_factorys_bosses:red_creeper_boss");
        KNOWN_BOSS_IDS.add("block_factorys_bosses:blue_creeper_boss");
        KNOWN_BOSS_IDS.add("callfromthedepth:m_boss");
    }

    public static boolean isBoss(Entity entity) {
        ResourceLocation id = EntityList.getKey(entity);
        if (id != null) {
            String idString = id.toString();

            if (KNOWN_BOSS_IDS.contains(idString)) {
                return true;
            }

            if (idString.startsWith("block_factorys_bosses:") ||
                idString.startsWith("wardens_plus:") ||
                idString.startsWith("threateningly_mobs:")) {
                return true;
            }
        }

        return entity instanceof EntityWither ||
               entity instanceof EntityDragon ||
               entity instanceof EntityIronGolem ||
               entity instanceof EntityElderGuardian;
    }
}
