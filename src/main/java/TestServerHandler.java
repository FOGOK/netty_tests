import com.esotericsoftware.kryo.io.ByteBufferOutput;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.ByteBuffer;

import static com.esotericsoftware.minlog.Log.info;

public class TestServerHandler extends ChannelInboundHandlerAdapter {

    public static final int BUFFER_SIZE = 32768;
    private final ByteBufferOutput output = new ByteBufferOutput(ByteBuffer.allocateDirect(BUFFER_SIZE));

    public ByteBufferOutput getClearedOutput() {
        output.clear();
        return output;
    }

    private PingPongLatch pingPongLatch = new PingPongLatch();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        info("Client connected. Start heavy send thread!");
        new Thread(() -> {
            while (true){
                if (!pingPongLatch.isEmpty())
                    continue;

                ByteBufferOutput output = getClearedOutput();
                output.writeInt(1, true);
                output.writeInt(2, true);
                output.writeInt(3, true);
                output.writeInt(4, true);
                output.writeInt(5, true);

                ByteBuf byteBuf = Unpooled.wrappedBuffer((ByteBuffer) output.getByteBuffer().flip());
                ctx.channel().writeAndFlush(byteBuf);
                pingPongLatch.incr();
                info(String.format("Sent %s bytes", byteBuf.readableBytes()));
            }
        }).start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        pingPongLatch.decr();
        info("received");
    }
}
