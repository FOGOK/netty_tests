import com.esotericsoftware.kryo.io.ByteBufferOutput;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.ByteBuffer;

public class TestServerHandler extends ChannelInboundHandlerAdapter {

    public static final int BUFFER_SIZE = 32768;
    private final ByteBufferOutput output = new ByteBufferOutput(ByteBuffer.allocateDirect(BUFFER_SIZE));

    public ByteBufferOutput getClearedOutput() {
        output.clear();
        return output;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected. Start heavy send thread!");
        new Thread(() -> {
            while (true){
                ByteBufferOutput output = getClearedOutput();
                output.writeInt(1, true);
                output.writeInt(2, true);
                output.writeInt(3, true);
                output.writeInt(4, true);
                output.writeInt(5, true);
                ctx.channel().writeAndFlush(Unpooled.wrappedBuffer((ByteBuffer) output.getByteBuffer().flip())).syncUninterruptibly();
            }
        }).run();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

    }
}
