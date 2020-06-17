package com.sstewartgallus.plato.runtime;

public abstract class Behaviour extends Throwable implements Runnable {
    public abstract Behaviour step() throws BehaviourThrowable;

    public final void run() {
        var current = this;
        for (; ; ) {
            try {
                current = current.step();
            } catch (HaltThrowable halt) {
                break;
            } catch (BehaviourThrowable t) {
                throw new IllegalStateException(t.getClass().toString());
            }
        }
    }
}
