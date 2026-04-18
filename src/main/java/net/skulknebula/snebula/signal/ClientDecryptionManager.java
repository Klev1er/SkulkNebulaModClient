package net.skulknebula.snebula.signal;


import net.skulknebula.snebula.SkulkNebulaMod;
import net.skulknebula.snebula.network.DecryptionHistoryPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClientDecryptionManager {
    private static ClientDecryptionManager instance;

    private SignalData currentSignal;
    private int progress;
    private int maxProgress = 100;
    private int imageQuality;
    private final List<DecryptionManager.DecryptedSignal> history = new ArrayList<>();

    public static ClientDecryptionManager getInstance() {
        if (instance == null) instance = new ClientDecryptionManager();
        return instance;
    }

    public void syncFromServer(String signalId, SignalData signal) {
        SkulkNebulaMod.LOGGER.info("Client received signal: {" + signalId + "}");
        this.currentSignal = signal;
        this.progress = 0;
        this.maxProgress = 100;
        this.imageQuality = 0;
    }

    public void syncProgress(String signalId, int progress, int maxProgress, int imageQuality) {
        this.progress = progress;
        this.maxProgress = maxProgress;
        this.imageQuality = imageQuality;
    }

    public void syncHistory(List<DecryptionHistoryPayload.HistoryEntry> entries) {
        history.clear();
        for (var entry : entries) {
            history.add(new DecryptionManager.DecryptedSignal(entry.signal(), entry.timestamp()));
        }
    }

    public boolean hasActiveSignal() {
        return currentSignal != null;
    }

    public String getCurrentSignalName() {
        return currentSignal != null ? currentSignal.id() : "";
    }

    public SignalData getCurrentSignal() {
        return currentSignal;
    }

    public float getProgress() {
        if (maxProgress <= 0) return 0f;
        return (float) progress / maxProgress;
    }

    public int getImageQuality() {
        return imageQuality;
    }

    public List<DecryptionManager.DecryptedSignal> getDecryptedHistory() {
        return new ArrayList<>(history);
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
                masked.append('§').append('k');
                masked.append((char) (random.nextInt(26) + 'A'));
                masked.append('§').append('r');
            } else {
                masked.append('#');
            }
        }
        return masked.toString();
    }

    public int getQueueSize() {
        return 0; // Очередь не синхронизируется на клиент
    }
}