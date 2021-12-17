package net.protolauncher.ui.task;

import javafx.application.Platform;
import javafx.concurrent.Task;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * An extension of the JavaFX {@link Task<V>} which adds
 * better event handling for more useful UI updates, and
 * fixes issues with rapid calls of progress and message
 * updates. Additionally, it synchronizes when updates are
 * pushed for all handlers and allows for up to three
 * progress handlers.
 *
 * Special thanks to  <a href="https://stackoverflow.com/a/40201248/6472449">fabian from StackOverflow</a>.
 */
public abstract class LauncherTask<V> extends Task<V> {

    // Event Consumers
    private Consumer<String> messageHandler;
    private Consumer<Progress> progressHandler;
    private Consumer<Progress> progressHandler2;
    private Consumer<Progress> progressHandler3;
    private Consumer<String> titleHandler;
    private Consumer<Optional<V>> valueHandler;

    // Queues
    private final Queue<String> messageQueue = new ArrayDeque<>();
    private final Queue<Progress> progressQueue = new ArrayDeque<>();
    private final Queue<Progress> progressQueue2 = new ArrayDeque<>();
    private final Queue<Progress> progressQueue3 = new ArrayDeque<>();
    private final Queue<String> titleQueue = new ArrayDeque<>();
    private final Queue<Optional<V>> valueQueue = new ArrayDeque<>();

    // Update Variables
    private final AtomicBoolean updateRequested = new AtomicBoolean(false);
    private final AtomicBoolean updating = new AtomicBoolean(false);

    // Setters
    public void setMessageHandler(Consumer<String> messageHandler) {
        this.messageHandler = messageHandler;
    }
    public void setProgressHandler(Consumer<Progress> progressHandler) {
        this.progressHandler = progressHandler;
    }
    public void setProgressHandler2(Consumer<Progress> progressHandler2) {
        this.progressHandler2 = progressHandler2;
    }
    public void setProgressHandler3(Consumer<Progress> progressHandler3) {
        this.progressHandler3 = progressHandler3;
    }
    public void setTitleHandler(Consumer<String> titleHandler) {
        this.titleHandler = titleHandler;
    }
    public void setValueHandler(Consumer<Optional<V>> valueHandler) {
        this.valueHandler = valueHandler;
    }

    /**
     * Checks if we are currently updating, and if so, then requests for a new update.
     * Otherwise, runs the update function inside {@link Platform#runLater(Runnable)}.
     */
    private void update() {
        if (!updating.get()) {
            updating.set(true);
            Platform.runLater(() -> {
                // Message
                synchronized (messageQueue) {
                    this.sendQueueToConsumer(messageQueue, messageHandler);
                }

                // Progress 1
                synchronized (progressQueue) {
                    this.sendQueueToConsumer(progressQueue, progressHandler);
                }

                // Progress 2
                synchronized (progressQueue2) {
                    this.sendQueueToConsumer(progressQueue2, progressHandler2);
                }

                // Progress 3
                synchronized (progressQueue3) {
                    this.sendQueueToConsumer(progressQueue3, progressHandler3);
                }

                // Title
                synchronized (titleQueue) {
                    this.sendQueueToConsumer(titleQueue, titleHandler);
                }

                // Value
                synchronized (valueQueue) {
                    this.sendQueueToConsumer(valueQueue, valueHandler);
                }

                updating.set(false);
                if (updateRequested.get()) {
                    updateRequested.set(false);
                    new Thread(this::update).start();
                }
            });
        } else {
            updateRequested.set(true);
        }
    }

    /**
     * Empties the given queue by passing each item into the given consumer.
     *
     * @param queue The queue to empty.
     * @param consumer The consumer to pass the queue into.
     * @param <T> The type of item we are passing.
     */
    public <T> void sendQueueToConsumer(Queue<T> queue, @Nullable Consumer<T> consumer) {
        if (queue.size() > 0 && consumer != null) {
            T item = queue.poll();
            while (item != null) {
                consumer.accept(item);
                item = queue.poll();
            }
        }
    }

    // Update Methods
    @Override
    protected void updateMessage(String message) {
        synchronized (messageQueue) {
            messageQueue.add(message);
            this.update();
        }
        super.updateMessage(message);
    }

    @Override
    protected void updateProgress(long workDone, long max) {
        this.updateProgress((double) workDone, (double) max);
    }

    @Override
    protected void updateProgress(double workDone, double max) {
        synchronized (progressQueue) {
            progressQueue.add(new Progress(workDone, max));
            this.update();
        }
        super.updateProgress(workDone, max);
    }

    protected void updateProgress2(double workDone, double max) {
        synchronized (progressQueue2) {
            progressQueue2.add(new Progress(workDone, max));
            this.update();
        }
    }

    protected void updateProgress3(double workDone, double max) {
        synchronized (progressQueue3) {
            progressQueue2.add(new Progress(workDone, max));
            this.update();
        }
    }

    @Override
    protected void updateTitle(String title) {
        synchronized (titleQueue) {
            titleQueue.add(title);
            this.update();
        }
        super.updateTitle(title);
    }

    @Override
    protected void updateValue(V value) {
        synchronized (valueQueue) {
            valueQueue.add(Optional.ofNullable(value));
            this.update();
        }
        super.updateValue(value);
    }

    /**
     * Represents 'progress', or workDone and max amount of work.
     * See {@link Task#updateProgress(double, double)}
     */
    public static class Progress {

        // Variables
        private double workDone;
        private double max;

        // Constructor
        public Progress(double workDone, double max) {
            this.workDone = workDone;
            this.max = max;
        }

        // Getters
        public double getWorkDone() {
            return workDone;
        }
        public double getMax() {
            return max;
        }

        // Setters
        public void setWorkDone(double workDone) {
            this.workDone = workDone;
        }
        public void setMax(double max) {
            this.max = max;
        }

    }

}
