import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("NonAtomicOperationOnVolatileField")
public class PingPongLatch {

    private volatile int pingPongLatches;

    private ReentrantLock isEmptyLock = new ReentrantLock();
    private Condition isEmptyConditon = isEmptyLock.newCondition();


    public void waitAndIncr(){
        isEmptyLock.lock();
        pingPongLatches++;
        checkLatch();
        if (pingPongLatches != 0)
            isEmptyConditon.awaitUninterruptibly();
        isEmptyLock.unlock();
    }

    public void waitAndDecr(){
        isEmptyLock.lock();
        pingPongLatches--;
        checkLatch();
        if (pingPongLatches != 0)
            isEmptyConditon.awaitUninterruptibly();
        isEmptyLock.unlock();
    }

    public void incr(){
        isEmptyLock.lock();
        pingPongLatches++;
        checkLatch();
        isEmptyLock.unlock();
    }

    public void decr(){
        isEmptyLock.lock();
        pingPongLatches--;
        checkLatch();
        isEmptyLock.unlock();
    }

    private void checkLatch(){
        if (pingPongLatches == 0)
            isEmptyConditon.signalAll();
    }

    public void waitForPingPong(){
        isEmptyLock.lock();
        if (pingPongLatches != 0)
            isEmptyConditon.awaitUninterruptibly();

        isEmptyLock.unlock();
    }
}
