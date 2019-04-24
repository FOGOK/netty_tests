import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;

import static com.esotericsoftware.minlog.Log.*;

public class ByteBufDelemiter {
    private final boolean isDebug = false;

    private boolean expectedHeader = true;
    private ByteBuf headerBuf;
    private ByteBuf contentBuf = ByteBufAllocator.DEFAULT.buffer();

    private final int headerSize;

    private ReadPacketCallback readPacketCallback;
    public ByteBufDelemiter(ReadPacketCallback readPacketCallback, int headerSize) {
        this.headerSize = headerSize;
        this.readPacketCallback = readPacketCallback;
        headerBuf = ByteBufAllocator.DEFAULT.buffer(headerSize);
    }


    private int contentSize;
    public void read(ByteBuf buf) {
        while (buf.isReadable()) {
            if (expectedHeader) {
                if (isDebug) debug("Received Header Part");
                headerBuf.writeBytes(buf, Math.min(buf.capacity(), headerSize - headerBuf.writerIndex()));
                if (headerBuf.readableBytes() >= headerSize) {

                    contentSize = headerBuf.readInt();
                    headerBuf.clear();
                    expectedHeader = false;

                    if (isDebug) debug("Received content size: " + contentSize);

                    contentBuf.clear();
                    contentBuf.capacity(contentSize);
                }
            } else {
                if (isDebug) debug("Received Part " + buf.readableBytes());
                writeAndCheckContent(buf);
            }
        }
    }

    private void writeAndCheckContent(ByteBuf buf){
        int remainingReceivingBuf = contentSize - headerSize;
        contentBuf.writeBytes(buf, Math.min(remainingReceivingBuf - contentBuf.writerIndex(), buf.readableBytes()));
        if (contentBuf.readableBytes() == remainingReceivingBuf) {
            try {
                readPacketCallback.readPacket(contentBuf);
            } catch (Throwable e) {
                error("wtf mazafaka: " + e);
            } finally {
                contentBuf.clear();
                expectedHeader = true;
            }
        }
    }
    
    private void reset(){
        contentBuf.clear();
        headerBuf.clear();
        expectedHeader = true;
    }
}

