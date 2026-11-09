package com.tontoque28.progressivetimedifficulty.mixin;

import com.tontoque28.progressivetimedifficulty.DifficultyHandler;
import com.tontoque28.progressivetimedifficulty.EnvironmentalDamageHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract Player getLastHurtByPlayer();

    @Inject(method = "tick", at = @At("TAIL"))
    private void ptd$tick(CallbackInfo ci) {
        DifficultyHandler.onLivingTick((LivingEntity) (Object) this);
    }

    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
    private float ptd$hurt(float amount, ServerLevel level, DamageSource source) {
        return EnvironmentalDamageHandler.onLivingHurt((LivingEntity) (Object) this, source, amount);
    }

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void ptd$lootStart(ServerLevel level, DamageSource source, CallbackInfo ci) {
        DifficultyHandler.LOOT_VICTIM.set((LivingEntity) (Object) this);
    }

    @Inject(method = "dropAllDeathLoot", at = @At("RETURN"))
    private void ptd$lootEnd(ServerLevel level, DamageSource source, CallbackInfo ci) {
        DifficultyHandler.LOOT_VICTIM.remove();
    }

    @ModifyArg(method = "dropExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"), index = 2)
    private int ptd$experience(int original) {
        return DifficultyHandler.adjustExperience((LivingEntity) (Object) this, this.getLastHurtByPlayer(), original);
    }
}
