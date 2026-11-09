package com.tontoque28.progressivetimedifficulty.mixin;

import com.tontoque28.progressivetimedifficulty.EnvironmentalDamageHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
    private float ptd$hurt(float amount, ServerLevel level, DamageSource source) {
        return EnvironmentalDamageHandler.onLivingHurt((LivingEntity) (Object) this, source, amount);
    }
}
