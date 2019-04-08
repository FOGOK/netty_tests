public class PingPongLatch {
    private final Object lock = new Object();
    private volatile int pingPongLatches;


    public void incr(){
        synchronized (lock) {
            pingPongLatches++;
        }
    }

    public void decr(){
        synchronized (lock) {
            pingPongLatches--;
        }
    }

    public boolean isEmpty(){
        synchronized (lock) {
            return pingPongLatches == 0;
        }
    }
}
