package com.tontoque28.progressivetimedifficulty.mixin;

import com.tontoque28.progressivetimedifficulty.PersistentDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    private void ptd$save(ValueOutput output, CallbackInfo ci) {
        if (this.ptd$data != null && !this.ptd$data.isEmpty()) {
            output.store("PTDData", CompoundTag.CODEC, this.ptd$data.copy());
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void ptd$load(ValueInput input, CallbackInfo ci) {
        input.read("PTDData", CompoundTag.CODEC).ifPresent(tag -> this.ptd$data = tag.copy());
    }
}
