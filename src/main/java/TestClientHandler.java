import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.esotericsoftware.minlog.Log.info;

public class TestClientHandler extends SimpleChannelInboundHandler<ByteBuf> implements ReadPacketCallback {

    public static final int BUFFER_SIZE = 32768;
    private final ByteBufferInput input = new ByteBufferInput(ByteBuffer.allocate(BUFFER_SIZE));

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        info("Connected to server!");
    }

    private ByteBufDelemiter byteBufDelemiter = new ByteBufDelemiter(this, 4);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        info("Start read");
        byteBufDelemiter.read(buf);
    }

    int readPackets;
    @Override
    public void readPacket(ByteBuf buf) throws Throwable {
        try {
            info(String.format("Received %s bytes", buf.readableBytes()));
            input.setBuffer(buf.nioBuffer());
            ////

            int[] troq = new int[11000];
            int i = 0;
            while (input.canReadInt()) {
                troq[i] = input.readInt(true);
                i++;
            }

            info("Content: " + Arrays.toString(Arrays.copyOf(troq, i)));

        } catch (Throwable e){
            e.printStackTrace();
        } finally {
            input.release();
        }
        readPackets++;
        info("readPackets: " + readPackets);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
    }
}
