package tpm;

import java.util.concurrent.atomic.AtomicReference;

public class CLHLock implements SimpleLock {

    private static class QNode {
        volatile boolean locked = false;
    }

    private final AtomicReference<QNode> tail =
            new AtomicReference<>(new QNode());

    private final ThreadLocal<QNode> myNode =
            ThreadLocal.withInitial(QNode::new);

    private final ThreadLocal<QNode> myPred =
            new ThreadLocal<>();

    @Override
    public void lock() {
        QNode node = myNode.get();
        node.locked = true;

        QNode pred = tail.getAndSet(node);
        myPred.set(pred);

        while (pred.locked) {
            Thread.onSpinWait();
        }
    }

    @Override
    public void unlock() {
        QNode node = myNode.get();
        node.locked = false;

        myNode.set(myPred.get());
        myPred.remove();
    }
}
