package net.skulknebula.snebula.signal;

import net.skulknebula.snebula.SkulkNebulaMod;
import net.skulknebula.snebula.block.custom.ServerBlockEntity;
import java.util.*;

public class DecryptionManager {
    private static DecryptionManager instance;

    private SignalData currentSignal = null;
    private int decryptionProgress = 0;
    private int maxProgress = 100;
    private int imageQuality = 0; // 0-100 качество изображения

    private final List<SignalData> signalQueue = new ArrayList<>();
    private final List<DecryptedSignal> decryptedHistory = new ArrayList<>();

    private DecryptionManager() {}

    public static DecryptionManager getInstance() {
        if (instance == null) instance = new DecryptionManager();
        return instance;
    }

    public void addSignal(String signalId) {
        SignalData signal = SignalLoader.loadSignal(signalId);
        if (signal != null) {
            signalQueue.add(signal);
            if (currentSignal == null) startNextSignal();
        }
    }

    private void startNextSignal() {
        if (!signalQueue.isEmpty()) {
            currentSignal = signalQueue.remove(0);
            decryptionProgress = 0;
            imageQuality = 0;
            maxProgress = calculateDecryptionTime();
        }
    }

    private int calculateDecryptionTime() {
        List<ServerBlockEntity> servers = ServerBlockEntity.getAllServers();
        int totalPower = 0;
        for (ServerBlockEntity server : servers) {
            if (!server.isBroken()) {
                totalPower += getTierPower(server.getUpgradeTier());
            }
        }
        if (totalPower == 0) totalPower = 50;
        int serverCount = Math.max(1, servers.size());
        return Math.max(10, totalPower / serverCount);
    }

    private int getTierPower(int tier) {
        return switch (tier) {
            case 1 -> 60;
            case 2 -> 150;
            case 3 -> 220;
            default -> 80;
        };
    }

    public void tick() {
        if (currentSignal != null) {
            decryptionProgress++;
            imageQuality = (int)(getProgress() * 100);

            if (decryptionProgress >= maxProgress) {
                completeDecryption();
            }
        }
    }

    private void completeDecryption() {
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

    public String getMaskedText() {
        if (currentSignal == null) return "";
        return generateMaskedText(currentSignal.content(), getProgress());
    }

    private String generateMaskedText(String text, float progress) {
        int revealedChars = (int) (text.length() * progress);
        StringBuilder masked = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < text.length(); i++) {
            if (i < revealedChars) {
                masked.append(text.charAt(i));
            } else if (i < revealedChars + 5) {
                masked.append('§').append('k').append((char)(random.nextInt(26)+'A')).append('§').append('r');
            } else {
                masked.append('#');
            }
        }
        return masked.toString();
    }

    public boolean hasActiveSignal() { return currentSignal != null; }
    public String getCurrentSignalName() { return currentSignal != null ? currentSignal.id() : ""; }
    public int getQueueSize() { return signalQueue.size(); }
    public SignalData getCurrentSignal() { return currentSignal; }
    public List<DecryptedSignal> getDecryptedHistory() { return new ArrayList<>(decryptedHistory); }

    public record DecryptedSignal(SignalData signal, long timestamp) {}
}