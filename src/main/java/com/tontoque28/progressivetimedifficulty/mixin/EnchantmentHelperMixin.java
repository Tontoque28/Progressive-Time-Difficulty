package com.tontoque28.progressivetimedifficulty.mixin;

import com.tontoque28.progressivetimedifficulty.DifficultyHandler;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    @Inject(method = "getEnchantmentLevel", at = @At("RETURN"), cancellable = true)
    private static void ptd$looting(Holder<Enchantment> enchantment, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (enchantment.is(Enchantments.LOOTING)) {
            cir.setReturnValue(DifficultyHandler.adjustLooting(entity, cir.getReturnValueI()));
        }
    }
}
