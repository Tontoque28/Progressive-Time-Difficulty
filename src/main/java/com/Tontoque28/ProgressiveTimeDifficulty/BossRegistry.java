package com.Tontoque28.ProgressiveTimeDifficulty;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

public class BossRegistry {

    private static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("forge", "bosses"));
    private static final Set<String> KNOWN_BOSS_IDS = new HashSet<>();

    static {
        // Mowzie's Mobs
        KNOWN_BOSS_IDS.add("mowziesmobs:frostmaw");
        KNOWN_BOSS_IDS.add("mowziesmobs:ferrous_wroughtnaut");
        KNOWN_BOSS_IDS.add("mowziesmobs:naga");
        KNOWN_BOSS_IDS.add("mowziesmobs:barako");
        
        // Twilight Forest
        KNOWN_BOSS_IDS.add("twilightforest:naga");
        KNOWN_BOSS_IDS.add("twilightforest:lich");
        KNOWN_BOSS_IDS.add("twilightforest:hydra");
        KNOWN_BOSS_IDS.add("twilightforest:ur_ghast");
        KNOWN_BOSS_IDS.add("twilightforest:snow_queen");
        KNOWN_BOSS_IDS.add("twilightforest:minoshroom");
        KNOWN_BOSS_IDS.add("twilightforest:knight_phantom");
        KNOWN_BOSS_IDS.add("twilightforest:alpha_yeti");
        
        // Cataclysm
        KNOWN_BOSS_IDS.add("cataclysm:ender_guardian");
        KNOWN_BOSS_IDS.add("cataclysm:netherite_monstrosity");
        KNOWN_BOSS_IDS.add("cataclysm:ignis");
        KNOWN_BOSS_IDS.add("cataclysm:the_harbinger");
        KNOWN_BOSS_IDS.add("cataclysm:the_leviathan");
        KNOWN_BOSS_IDS.add("cataclysm:ancient_remnant");
        
        // Born in Chaos
        KNOWN_BOSS_IDS.add("born_in_chaos_v1:nightmare_stalker");
        KNOWN_BOSS_IDS.add("born_in_chaos_v1:supreme_bonescaller");
        KNOWN_BOSS_IDS.add("born_in_chaos_v1:missioner");
        KNOWN_BOSS_IDS.add("born_in_chaos_v1:lifestealer");
        KNOWN_BOSS_IDS.add("born_in_chaos_v1:fallen_chaos_knight");
        
        // Ice and Fire
        KNOWN_BOSS_IDS.add("iceandfire:fire_dragon");
        KNOWN_BOSS_IDS.add("iceandfire:ice_dragon");
        KNOWN_BOSS_IDS.add("iceandfire:lightning_dragon");
        KNOWN_BOSS_IDS.add("iceandfire:cyclops");
        KNOWN_BOSS_IDS.add("iceandfire:gorgon");
        KNOWN_BOSS_IDS.add("iceandfire:hydra");
        
        // Aquamirae
        KNOWN_BOSS_IDS.add("aquamirae:captain_cornelia");
        
        // Bosses of Mass Destruction
        KNOWN_BOSS_IDS.add("bomd:night_lich");
        KNOWN_BOSS_IDS.add("bomd:obsidilith");
        KNOWN_BOSS_IDS.add("bomd:gauntlet");
        KNOWN_BOSS_IDS.add("bomd:void_blossom");
        
        // Stalwart Dungeons
        KNOWN_BOSS_IDS.add("stalwart_dungeons:awful_ghast");
        KNOWN_BOSS_IDS.add("stalwart_dungeons:nether_keeper");
        
        // Rotten Creatures
        KNOWN_BOSS_IDS.add("rottencreatures:immortal");

        // Block Factory's Bosses (Estimado)
        KNOWN_BOSS_IDS.add("block_factorys_bosses:red_creeper_boss");
        KNOWN_BOSS_IDS.add("block_factorys_bosses:blue_creeper_boss");
        
        // Call From The Depth (Estimado)
        KNOWN_BOSS_IDS.add("callfromthedepth:m_boss");
    }

    public static boolean isBoss(Entity entity) {
        if (entity.getType().is(BOSS_TAG)) {
            return true;
        }
        
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (id != null) {
            String idString = id.toString();
            
            if (KNOWN_BOSS_IDS.contains(idString)) {
                return true;
            }
            
            // Detección heurística para mods desconocidos de la lista
            if (idString.startsWith("block_factorys_bosses:") || 
                idString.startsWith("wardens_plus:") ||
                idString.startsWith("threateningly_mobs:")) {
                return true; // Asumir que todas las entidades de estos mods son peligrosas/jefes
            }
        }

        return entity instanceof WitherBoss || 
               entity instanceof EnderDragon || 
               entity instanceof Warden || 
               entity instanceof IronGolem || 
               entity instanceof ElderGuardian;
    }
}
