package uk.ac.ebi.spot.ols.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An object that can do some initialization work in parallel at startup.  Implementing classes should extend {@link
 * #doInitialization} with the work they wish to do.  All methods that require initialization to complete can check the
 * current state with a call to {@link #isReady()} which returns true or false, or a call to {@link #waitUntilReady()},
 * which blocks whilst initialization completes.
 *
 * @author Tony Burdett
 * @date 25/01/12
 */
public abstract class Initializable {
    private Thread initThread;

    private boolean initStarted;
    private boolean ready;
    private Throwable initializationException;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    protected synchronized void setInitStarted() {
        this.initStarted = true;
        notifyAll();
    }

    protected synchronized void setInitStopped() {
        this.initStarted = false;
        notifyAll();
    }

    protected synchronized void setReady(boolean ready) {
        this.ready = ready;
        notifyAll();
    }

    protected synchronized void setInitializationException(Throwable t) {
        if (t != null) {
            getLog().error("Failed to initialize " + Initializable.this.getClass().getSimpleName() + ". " +
                                   "Initialization exception updated", t.getMessage());
        }
        this.initializationException = t;
        notifyAll();
    }

    protected synchronized boolean hasInitStarted() throws IllegalStateException {
        return initStarted;
    }

    public synchronized boolean isReady() throws IllegalStateException {
        if (initializationException != null) {
            throw new IllegalStateException(
                    "Initialization of " + getClass().getSimpleName() + " failed: " + initializationException.getMessage(), initializationException);
        }
        else {
            return ready;
        }
    }

    public synchronized void waitUntilReady() throws IllegalStateException, InterruptedException {
        while (!isReady()) {
            getLog().debug("Waiting until " + getClass().getSimpleName() + " is ready...");
            wait();
        }
        getLog().debug(getClass().getSimpleName() + " is now ready");
    }

    protected synchronized void initOrWait() throws IllegalStateException, InterruptedException {
        if (hasInitStarted()) {
            // init already started, just wait
            waitUntilReady();
        }
        else {
            // start (or possibly restart) init
            if (!isReady()) {
                init();
                waitUntilReady();
            }
        }
    }

    protected synchronized void interrupt() {
        // if initializing, then interrupt
        if (hasInitStarted()) {
            initThread.interrupt();
            setInitStopped();
        }
        else {
            if (!isReady()) {
                setInitializationException(new InterruptedException("Initialization was forcibly interrupted"));
            }
        }
    }

    public void init() {
        // if not already started an init thread or fully initialized, then init
        synchronized (this) {
            if (!hasInitStarted() && !isReady()) {
                // clear any existing initialization exceptions and flag that init has started
                setInitializationException(null);
                setInitStarted();

                // now create new thread to do initialization
                initThread = new Thread((new Runnable() {
                    public void run() {
                        // call doInitialization() provided by subclasses
                        try {
                            getLog().debug("Initializing " + Initializable.this.getClass().getSimpleName() + "...");
                            doInitialization();
                            setReady(true);
                            getLog().debug("..." + Initializable.this.getClass().getSimpleName() + " initialized ok");
                        }
                        catch (Exception e) {
                            getLog().debug("Caught exception whilst initializing, " +
                                                   "attempting to handle with clean termination", e);
                            setInitializationException(e);
                        }
                        setInitStopped();
                    }
                }));
                // kick off init
                initThread.start();
            }
        }
    }

    public void destroy() {
        synchronized (this) {
            try {
                doTermination();
            }
            catch (Exception e) {
                getLog().error("Failed to terminate " + Initializable.this.getClass().getSimpleName() + ": " +
                                       "this may result in stale threads and a possible memory leak", e);
                setInitializationException(e);
            }
            interrupt();
            initThread = null;
        }
    }

    protected abstract void doInitialization() throws Exception;

    protected abstract void doTermination() throws Exception;
}
