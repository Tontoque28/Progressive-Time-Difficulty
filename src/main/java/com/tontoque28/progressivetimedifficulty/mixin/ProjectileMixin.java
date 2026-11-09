package com.tontoque28.progressivetimedifficulty.mixin;

import com.tontoque28.progressivetimedifficulty.DifficultyHandler;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {

    @Inject(method = "onHit", at = @At("HEAD"))
    private void ptd$onHit(HitResult result, CallbackInfo ci) {
        DifficultyHandler.onProjectileImpact((Projectile) (Object) this);
    }
}
