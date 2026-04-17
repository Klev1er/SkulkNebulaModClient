package net.skulknebula.snebula.signal;

public record SignalData(String id, String content, int difficulty, String imagePath) {
    public SignalData(String id, String content, int difficulty) {
        this(id, content, difficulty, null);
    }

    public boolean hasImage() {
        return imagePath != null && !imagePath.isEmpty();
    }
}