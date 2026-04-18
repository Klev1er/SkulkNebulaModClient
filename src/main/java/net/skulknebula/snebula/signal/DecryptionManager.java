package net.skulknebula.snebula.signal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.skulknebula.snebula.SkulkNebulaMod;
import net.skulknebula.snebula.block.custom.ServerBlockEntity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DecryptionManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static DecryptionManager instance;
    private static Path saveFile;

    private SignalData currentSignal = null;
    private int decryptionProgress = 0;
    private int maxProgress = 100;
    private int imageQuality = 0;

    private final List<SignalData> signalQueue = new ArrayList<>();
    private final List<DecryptedSignal> decryptedHistory = new ArrayList<>();

    private DecryptionManager() {}

    public static DecryptionManager getInstance() {
        if (instance == null) {
            instance = new DecryptionManager();
        }
        return instance;
    }

    public static void init() {
        saveFile = FabricLoader.getInstance().getConfigDir().resolve("snebula/decryption_state.json");
        getInstance().loadState();
    }

    /**
     * Сохранить состояние в файл
     */
    public void saveState() {
        try {
            Files.createDirectories(saveFile.getParent());

            SaveData data = new SaveData(
                    currentSignal,
                    decryptionProgress,
                    maxProgress,
                    new ArrayList<>(signalQueue),
                    new ArrayList<>(decryptedHistory)
            );

            String json = GSON.toJson(data);
            Files.writeString(saveFile, json);
            SkulkNebulaMod.LOGGER.info("Decryption state saved");
        } catch (IOException e) {
            SkulkNebulaMod.LOGGER.warning("Failed to save decryption state: " + e);
        }
    }

    /**
     * Загрузить состояние из файла
     */
    private void loadState() {
        if (!Files.exists(saveFile)) {
            SkulkNebulaMod.LOGGER.info("No saved decryption state found");
            return;
        }

        try {
            String json = Files.readString(saveFile);
            SaveData data = GSON.fromJson(json, SaveData.class);

            if (data != null) {
                this.currentSignal = data.currentSignal;
                this.decryptionProgress = data.decryptionProgress;
                this.maxProgress = data.maxProgress;
                this.signalQueue.clear();
                if (data.signalQueue != null) {
                    this.signalQueue.addAll(data.signalQueue);
                }
                this.decryptedHistory.clear();
                if (data.decryptedHistory != null) {
                    this.decryptedHistory.addAll(data.decryptedHistory);
                }

                SkulkNebulaMod.LOGGER.info("Decryption state loaded. Progress: " + decryptionProgress + "/" + maxProgress);
            }
        } catch (IOException e) {
            SkulkNebulaMod.LOGGER.warning("Failed to load decryption state: " + e);
        }
    }

    public void addSignal(String signalId) {
        SkulkNebulaMod.LOGGER.info("Adding signal to queue: {" + signalId + "}");
        SignalData signal = SignalLoader.loadSignal(signalId);
        if (signal != null) {
            signalQueue.add(signal);
            SkulkNebulaMod.LOGGER.info("Signal added to queue. Queue size: {" + signalQueue.size() + "}");
            if (currentSignal == null) startNextSignal();
            saveState();
        } else {
            SkulkNebulaMod.LOGGER.warning("Failed to load signal: {" + signalId + "}");
        }
    }

    private void startNextSignal() {
        if (!signalQueue.isEmpty()) {
            currentSignal = signalQueue.remove(0);
            decryptionProgress = 0;
            imageQuality = 0;
            maxProgress = calculateDecryptionTime();
            SkulkNebulaMod.LOGGER.warning("Started decrypting: {" + currentSignal + "}, {" + maxProgress + "} days");
            saveState();
        }
    }

    private int calculateDecryptionTime() {
        List<ServerBlockEntity> servers = ServerBlockEntity.getAllServers();

        int workingServers = 0;
        float totalPower = 0;

        for (ServerBlockEntity server : servers) {
            if (!server.isBroken()) {
                workingServers++;
                int tier = server.getUpgradeTier();
                totalPower += getTierPower(tier);
            }
        }

        if (workingServers > 1) {
            totalPower *= (1 + (workingServers - 1) * 0.15f);
        }

        if (totalPower <= 0) {
            totalPower = 30;
        }

        int baseTime = 200;
        return Math.max(10, (int)(baseTime / (totalPower / 100)));
    }

    private int getTierPower(int tier) {
        return switch (tier) {
            case 1 -> 60;
            case 2 -> 150;
            case 3 -> 220;
            default -> 30;
        };
    }

    private float calculateDecryptionQuality() {
        List<ServerBlockEntity> servers = ServerBlockEntity.getAllServers();

        int workingServers = 0;
        int totalTier = 0;

        for (ServerBlockEntity server : servers) {
            if (!server.isBroken()) {
                workingServers++;
                totalTier += server.getUpgradeTier();
            }
        }

        float quality = 50f;
        quality += totalTier * 8f;
        quality += workingServers * 3f;

        return Math.max(20f, Math.min(100f, quality));
    }

    public void tick() {
        if (currentSignal != null) {
            decryptionProgress++;

            float baseQuality = calculateDecryptionQuality();
            float progress = getProgress();
            imageQuality = (int)Math.max(10, baseQuality * progress);

            if (decryptionProgress >= maxProgress) {
                completeDecryption();
            }
            saveState();
        }
    }

    private void completeDecryption() {
        SkulkNebulaMod.LOGGER.warning("Signal decrypted: {" + currentSignal.id() + "}");
        decryptedHistory.add(new DecryptedSignal(currentSignal, System.currentTimeMillis()));
        currentSignal = null;
        startNextSignal();
    }

    public float getProgress() {
        if (currentSignal == null) return 0f;
        return (float) decryptionProgress / maxProgress;
    }

    public int getImageQuality() {
        return imageQuality;
    }

    private float getCorruptionChance() {
        float quality = calculateDecryptionQuality();
        return Math.max(5f, 100f - quality) / 100f;
    }

    public String getMaskedText() {
        if (currentSignal == null) return "";
        return generateMaskedText(currentSignal.content(), getProgress());
    }

    private String generateMaskedText(String text, float progress) {
        int revealedChars = (int) (text.length() * progress);
        StringBuilder masked = new StringBuilder();
        Random random = new Random();
        float corruptionChance = getCorruptionChance();

        for (int i = 0; i < text.length(); i++) {
            if (i < revealedChars) {
                if (random.nextFloat() < corruptionChance * 0.3f) {
                    masked.append('■');
                } else {
                    masked.append(text.charAt(i));
                }
            } else if (i < revealedChars + 5) {
                masked.append('§').append('k');
                masked.append((char) (random.nextInt(26) + 'A'));
                masked.append('§').append('r');
            } else {
                if (random.nextFloat() < corruptionChance * 0.5f) {
                    masked.append('■');
                } else {
                    masked.append('#');
                }
            }
        }
        return masked.toString();
    }

    public boolean hasActiveSignal() {
        return currentSignal != null;
    }

    public String getCurrentSignalName() {
        return currentSignal != null ? currentSignal.id() : "";
    }

    public int getQueueSize() {
        return signalQueue.size();
    }

    public SignalData getCurrentSignal() {
        return currentSignal;
    }

    public List<DecryptedSignal> getDecryptedHistory() {
        return new ArrayList<>(decryptedHistory);
    }

    public record DecryptedSignal(SignalData signal, long timestamp) {}

    private static class SaveData {
        SignalData currentSignal;
        int decryptionProgress;
        int maxProgress;
        List<SignalData> signalQueue;
        List<DecryptedSignal> decryptedHistory;

        SaveData(SignalData currentSignal, int decryptionProgress, int maxProgress,
                 List<SignalData> signalQueue, List<DecryptedSignal> decryptedHistory) {
            this.currentSignal = currentSignal;
            this.decryptionProgress = decryptionProgress;
            this.maxProgress = maxProgress;
            this.signalQueue = signalQueue;
            this.decryptedHistory = decryptedHistory;
        }
    }
}