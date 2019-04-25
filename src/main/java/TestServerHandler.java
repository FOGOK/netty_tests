import com.esotericsoftware.kryo.io.ByteBufferOutput;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.ByteBuffer;
import java.util.Random;

import static com.esotericsoftware.minlog.Log.info;

public class TestServerHandler extends ChannelInboundHandlerAdapter {

    private static final int BUFFER_SIZE = 32768;
    private final ByteBufferOutput output = new ByteBufferOutput(ByteBuffer.allocateDirect(BUFFER_SIZE));

    private ByteBufferOutput getClearedOutput() {
        output.clear();
        return output;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        info("Client connected. Start heavy send thread!");

        new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                ByteBufferOutput output = getClearedOutput();
                ByteBufSenderHelper.reservePlaceToHeader(output);

                int t = new Random().nextInt(100) + 10;
                for (int q = 0; q < ( i + 1); q++)
                    output.writeInt(q, true);

                ByteBufSenderHelper.setHeader(output);

                ByteBuf byteBuf = Unpooled.wrappedBuffer((ByteBuffer) output.getByteBuffer().flip());
                ctx.writeAndFlush(byteBuf).syncUninterruptibly();
                info("Sent! " + (i  + 1));
            }
        }).start();
    }
}
