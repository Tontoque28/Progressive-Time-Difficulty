package com.tontoque28.progressivetimedifficulty;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public class DifficultySavedData extends WorldSavedData {
    private static final String DATA_NAME = "ptd_difficulty_data";
    private long additionalTime = 0;
    private boolean hardcoreMode = false;
    private boolean devMode = false;

    public DifficultySavedData() {
        super(DATA_NAME);
    }

    public static DifficultySavedData get(ServerWorld level) {
        return level.getDataStorage().computeIfAbsent(DifficultySavedData::new, DATA_NAME);
    }

    @Override
    public void load(CompoundNBT tag) {
        this.additionalTime = tag.getLong("AdditionalTime");

        if (tag.contains("HardcoreMode")) {
            this.hardcoreMode = tag.getBoolean("HardcoreMode");
        }

        if (tag.contains("DevMode")) {
            this.devMode = tag.getBoolean("DevMode");
            ProgressiveTimeDifficultyMod.DEV_MODE = this.devMode;
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        tag.putLong("AdditionalTime", additionalTime);
        tag.putBoolean("HardcoreMode", hardcoreMode);
        tag.putBoolean("DevMode", devMode);
        return tag;
    }

    public long getAdditionalTime() {
        return additionalTime;
    }

    public void setAdditionalTime(long time) {
        this.additionalTime = time;
        this.setDirty();
    }

    public void addTime(long time) {
        this.additionalTime += time;
        this.setDirty();
    }

    public boolean isHardcoreMode() {
        return hardcoreMode;
    }

    public void setHardcoreMode(boolean active) {
        this.hardcoreMode = active;
        this.setDirty();
    }

    public boolean isDevMode() {
        return devMode;
    }

    public void setDevMode(boolean active) {
        this.devMode = active;
        ProgressiveTimeDifficultyMod.DEV_MODE = active;
        this.setDirty();
    }
}
