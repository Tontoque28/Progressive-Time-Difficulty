package com.tontoque28.progressivetimedifficulty;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class DifficultySavedData extends SavedData {
    private static final String DATA_NAME = "ptd_difficulty_data";
    private long additionalTime = 0;
    private boolean hardcoreMode = false;
    private boolean devMode = false;

    public DifficultySavedData() {
        super(DATA_NAME);
    }

    public static DifficultySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(DifficultySavedData::new, DATA_NAME);
    }

    @Override
    public void load(CompoundTag tag) {
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
    public CompoundTag save(CompoundTag tag) {
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
