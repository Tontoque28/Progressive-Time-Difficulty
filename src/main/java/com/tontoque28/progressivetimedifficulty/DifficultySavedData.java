package com.tontoque28.progressivetimedifficulty;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class DifficultySavedData extends SavedData {
    public static final Codec<DifficultySavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.LONG.optionalFieldOf("AdditionalTime", 0L).forGetter(DifficultySavedData::getAdditionalTime),
        Codec.BOOL.optionalFieldOf("HardcoreMode", false).forGetter(DifficultySavedData::isHardcoreMode),
        Codec.BOOL.optionalFieldOf("DevMode", false).forGetter(DifficultySavedData::isDevMode)
    ).apply(instance, DifficultySavedData::load));

    public static final SavedDataType<DifficultySavedData> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath(ProgressiveTimeDifficultyMod.MODID, "ptd_difficulty_data"),
        DifficultySavedData::new,
        CODEC,
        null
    );

    private long additionalTime = 0;
    private boolean hardcoreMode = false;
    private boolean devMode = false;

    public static DifficultySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    private static DifficultySavedData load(long additionalTime, boolean hardcoreMode, boolean devMode) {
        DifficultySavedData data = new DifficultySavedData();
        data.additionalTime = additionalTime;
        data.hardcoreMode = hardcoreMode;
        data.devMode = devMode;
        ProgressiveTimeDifficultyMod.DEV_MODE = devMode;
        return data;
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
