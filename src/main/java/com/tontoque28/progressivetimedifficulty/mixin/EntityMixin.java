package com.tontoque28.progressivetimedifficulty.mixin;

import com.tontoque28.progressivetimedifficulty.PersistentDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements PersistentDataHolder {

    @Unique
    private CompoundTag ptd$data;

    @Override
    public CompoundTag ptd$getPersistentData() {
        if (this.ptd$data == null) {
            this.ptd$data = new CompoundTag();
        }
        return this.ptd$data;
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void ptd$save(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (this.ptd$data != null && !this.ptd$data.isEmpty()) {
            cir.getReturnValue().put("PTDData", this.ptd$data.copy());
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void ptd$load(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("PTDData", 10)) {
            this.ptd$data = tag.getCompound("PTDData").copy();
        }
    }
}
