import io.netty.buffer.ByteBuf;

public interface ReadPacketCallback {
    void readPacket(ByteBuf buf) throws Throwable;
}
