import junit.framework.TestCase;
import org.junit.Test;

import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import static com.esotericsoftware.minlog.Log.info;

public class LatchTest extends TestCase {

    private LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();

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

}
