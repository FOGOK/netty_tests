import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.minlog.Log;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import junit.framework.TestCase;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import static com.esotericsoftware.minlog.Log.*;

public class ExperimentsTest extends TestCase {

    private LinkedBlockingQueue<String> messages;

    @Override
    protected void setUp() throws Exception {
        Log.set(LEVEL_TRACE);
        Log.setLogger(new MyLogger());
        messages = new LinkedBlockingQueue<>();
    }

    public void testSimpleTwoThreads() throws InterruptedException {

        final String pingMessage = "ping";
        final String pongMessage = "pong";
        final int countTest = 5000;
        StringBuffer bf = new StringBuffer((pingMessage.length() + pongMessage.length()) * (countTest / 2));

        PingPongLatch latch = new PingPongLatch();
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < countTest / 2; i++) {
                String message = null;
                try {
                    message = messages.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                bf.append(message);

                latch.waitAndDecr();
            }

        }, "Thread1");

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < countTest / 2; i++) {

                bf.append(pingMessage);
                messages.add(pongMessage);

                latch.waitAndIncr();

            }
        }, "Thread2");

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        assertEquals(bf.toString().split(pongMessage).length, countTest / 2);
    }


    private static class Response{
        Object object;
    }

    public void testPacketsExperiments() {

//        info("Starting packets delimiter test. \"wtf mazafaka: java.lang.Exception: sosny huyca\" its normal, don`t panic");


        int headerSize = 4;

        final Response response = new Response();
        ByteBufDelemiter byteBufDelemiter = new ByteBufDelemiter((buf) -> {

//            if (new Random().nextInt(10) == 5) {
//                response.object = null;
//                throw new Exception("sosny huyca");
//            }

            //readPacket
            info("Received bytes: " + buf.readableBytes());

            ByteBufferInput input = new ByteBufferInput(ByteBuffer.allocate(buf.readableBytes()));
            input.setBuffer(buf.nioBuffer());
            int[] troq = new int[200];
            int i = 0;
            while (input.canReadInt()) {
                troq[i] = input.readInt(true);
                i++;
            }

            int[] result = Arrays.copyOf(troq, i);
            info("Received content(" + i + "): " + Arrays.toString(result));
            info("/////////////////////////////");

            response.object = result;
        }, headerSize);


        for (int q = 0; q < 50000; q++) {
            info("/////////////////////////////");
            info("start test #" + q);
            info("/////////////////////////////");


            //write
            int BUFFER_SIZE = 10000;
            ByteBufferOutput output = new ByteBufferOutput(ByteBuffer.allocateDirect(BUFFER_SIZE));

            int countN = new Random().nextInt(100)  + 10;
//            int countN = 10;
            int[] n = new int[countN];
            n[0] = -1;


            for (int i = 0; i < countN; i++) {
                if (i == 0) {
                    output.writeInt(-1);
                } else {
                    int nVal = new Random().nextInt(Integer.MAX_VALUE);
//                    int nVal = i;
                    n[i] = nVal;
                    output.writeInt(n[i], true);
                }
            }

            ByteBufSenderHelper.setHeader(output);

            info("Sent bytes: " + (output.position() - 4));

            int[] contentWithoutHeader = Arrays.copyOfRange(n, 1, n.length );

            info("Sent content(" + (countN - 1) + "): " + Arrays.toString(contentWithoutHeader));
            info("/////////////////////////////");


            //readPacket
            ByteBuffer byteBuffer = (ByteBuffer) output.getByteBuffer().flip();
            imitateNetworkingByteSlicing(byteBufDelemiter, byteBuffer, new Random().nextInt(byteBuffer.limit() - 1) + 1);// new Random().nextInt(byteBuffer.limit() - 1) + 1


            //compare results
            try {
                if (response.object != null)
                    assertTrue(Arrays.deepEquals(new Object[]{response.object}, new Object[]{contentWithoutHeader}));
            } catch (Throwable e) {
                error("Ne sootvetstvie mazafaka: " + Arrays.toString((int[])response.object) + " != "  + Arrays.toString(contentWithoutHeader));
                assertTrue(false);
            }
        }
    }

//    public void testDaun(){
//        int t = 7;
//        int partsCount = 4;
//
//        int partsSize = t / partsCount;
//        int lastPartSize = partsSize + t % partsCount;
//        System.out.println(partsSize);
//        System.out.println(lastPartSize);
//    }

    private void imitateNetworkingByteSlicing(ByteBufDelemiter byteBufDelemiter, ByteBuffer byteBuffer, int partsCount){
        info("starting imitation byte slicing with " + partsCount + " parts");
        int length = byteBuffer.limit();

        int partsSize = length / partsCount;
        int lastPartSize = partsSize + length % partsCount;

        info("partsSize " + partsSize);
        info("lastPartSize " + lastPartSize);

        for (int i = 0; i < partsCount; i++) {
            int bufferSize = i != partsCount - 1 ? partsSize : lastPartSize;
            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(bufferSize);
            for (int j = 0; j < bufferSize; j++) {
                buffer.writeByte(byteBuffer.get());
            }
            byteBufDelemiter.read(buffer);
        }
    }
}
