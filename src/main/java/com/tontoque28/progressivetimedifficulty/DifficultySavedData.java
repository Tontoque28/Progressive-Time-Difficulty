package com.tontoque28.progressivetimedifficulty;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public class DifficultySavedData extends WorldSavedData {
    private static final String DATA_NAME = "ptd_difficulty_data";
    private long additionalTime = 0;
    private boolean hardcoreMode = false;
    private boolean devMode = false;

    public DifficultySavedData(String name) {
        super(name);
    }

    public static DifficultySavedData get(World level) {
        MapStorage storage = level.getPerWorldStorage();
        DifficultySavedData data = (DifficultySavedData) storage.getOrLoadData(DifficultySavedData.class, DATA_NAME);
        if (data == null) {
            data = new DifficultySavedData(DATA_NAME);
            storage.setData(DATA_NAME, data);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        this.additionalTime = tag.getLong("AdditionalTime");

        if (tag.hasKey("HardcoreMode")) {
            this.hardcoreMode = tag.getBoolean("HardcoreMode");
        }

        if (tag.hasKey("DevMode")) {
            this.devMode = tag.getBoolean("DevMode");
            ProgressiveTimeDifficultyMod.DEV_MODE = this.devMode;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setLong("AdditionalTime", additionalTime);
        tag.setBoolean("HardcoreMode", hardcoreMode);
        tag.setBoolean("DevMode", devMode);
        return tag;
    }

    public long getAdditionalTime() {
        return additionalTime;
    }

    public void setAdditionalTime(long time) {
        this.additionalTime = time;
        this.markDirty();
    }

    public void addTime(long time) {
        this.additionalTime += time;
        this.markDirty();
    }

    public boolean isHardcoreMode() {
        return hardcoreMode;
    }

    public void setHardcoreMode(boolean active) {
        this.hardcoreMode = active;
        this.markDirty();
    }

    public boolean isDevMode() {
        return devMode;
    }

    public void setDevMode(boolean active) {
        this.devMode = active;
        ProgressiveTimeDifficultyMod.DEV_MODE = active;
        this.markDirty();
    }
}
