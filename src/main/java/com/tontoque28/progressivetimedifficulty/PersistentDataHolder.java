package com.tontoque28.progressivetimedifficulty;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public interface PersistentDataHolder {

    CompoundTag ptd$getPersistentData();

    static CompoundTag of(Entity entity) {
        return ((PersistentDataHolder) entity).ptd$getPersistentData();
    }
}
