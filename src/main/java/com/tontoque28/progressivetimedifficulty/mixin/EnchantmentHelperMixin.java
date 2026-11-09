package com.tontoque28.progressivetimedifficulty.mixin;

import com.tontoque28.progressivetimedifficulty.DifficultyHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    @Inject(method = "getMobLooting", at = @At("RETURN"), cancellable = true)
    private static void ptd$looting(LivingEntity killer, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(DifficultyHandler.adjustLooting(killer, cir.getReturnValueI()));
    }
}
