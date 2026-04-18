package net.skulknebula.snebula.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.skulknebula.snebula.SkulkNebulaMod;
import net.skulknebula.snebula.signal.SignalData;

public record SignalSyncPayload(String signalId, SignalData signal) implements CustomPayload {
    public static final CustomPayload.Id<SignalSyncPayload> ID =
            new CustomPayload.Id<>(Identifier.of(SkulkNebulaMod.MOD_ID, "signal_sync"));

    public static final PacketCodec<PacketByteBuf, SignalSyncPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeString(value.signalId);
                if (value.signal != null) {
                    buf.writeBoolean(true);
                    buf.writeString(value.signal.id());
                    buf.writeString(value.signal.content());
                    buf.writeInt(value.signal.difficulty());
                    buf.writeBoolean(value.signal.hasImage());
                    if (value.signal.hasImage()) {
                        buf.writeString(value.signal.imagePath());
                    }
                } else {
                    buf.writeBoolean(false);
                }
            },
            buf -> {
                String signalId = buf.readString();
                SignalData signal = null;
                if (buf.readBoolean()) {
                    signal = new SignalData(
                            buf.readString(),
                            buf.readString(),
                            buf.readInt(),
                            buf.readBoolean() ? buf.readString() : null
                    );
                }
                return new SignalSyncPayload(signalId, signal);
            }
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}