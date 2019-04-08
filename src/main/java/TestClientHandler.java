import com.esotericsoftware.kryo.io.ByteBufferInput;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class TestClientHandler extends ChannelInboundHandlerAdapter {

    public static final int BUFFER_SIZE = 32768;
    private final ByteBufferInput input = new ByteBufferInput(ByteBuffer.allocate(BUFFER_SIZE));

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Connected to server!");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        try {
            System.out.println(String.format("Received %s bytes", buf.readableBytes()));
            input.setBuffer(buf.nioBuffer());
            ////

            int[] troq = new int[5];
            for (int i = 0; i < troq.length; i++) {
                troq[i] = input.readInt(true);
            }

            System.out.println("Content: " + Arrays.toString(troq));


        } catch (Throwable e){
            e.printStackTrace();
        } finally {
            input.release();
            if (buf != null) {
                ReferenceCountUtil.release(buf);
            }
        }
    }
}
