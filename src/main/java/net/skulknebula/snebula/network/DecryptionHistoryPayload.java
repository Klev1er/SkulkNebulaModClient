package net.skulknebula.snebula.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.skulknebula.snebula.SkulkNebulaMod;
import net.skulknebula.snebula.signal.DecryptionManager;
import net.skulknebula.snebula.signal.SignalData;

import java.util.ArrayList;
import java.util.List;

public record DecryptionHistoryPayload(List<HistoryEntry> history) implements CustomPayload {
    public static final CustomPayload.Id<DecryptionHistoryPayload> ID =
            new CustomPayload.Id<>(Identifier.of(SkulkNebulaMod.MOD_ID, "decryption_history"));

    public static final PacketCodec<PacketByteBuf, DecryptionHistoryPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeInt(value.history.size());
                for (HistoryEntry entry : value.history) {
                    buf.writeString(entry.signal().id());
                    buf.writeString(entry.signal().content());
                    buf.writeInt(entry.signal().difficulty());
                    buf.writeBoolean(entry.signal().hasImage());
                    if (entry.signal().hasImage()) buf.writeString(entry.signal().imagePath());
                    buf.writeLong(entry.timestamp());
                }
            },
            buf -> {
                int size = buf.readInt();
                List<HistoryEntry> history = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    SignalData signal = new SignalData(
                            buf.readString(), buf.readString(), buf.readInt(),
                            buf.readBoolean() ? buf.readString() : null
                    );
                    history.add(new HistoryEntry(signal, buf.readLong()));
                }
                return new DecryptionHistoryPayload(history);
            }
    );

    public static DecryptionHistoryPayload fromDecryptedSignals(List<DecryptionManager.DecryptedSignal> signals) {
        List<HistoryEntry> entries = signals.stream()
                .map(ds -> new HistoryEntry(ds.signal(), ds.timestamp()))
                .toList();
        return new DecryptionHistoryPayload(entries);
    }

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }

    public record HistoryEntry(SignalData signal, long timestamp) {}
}