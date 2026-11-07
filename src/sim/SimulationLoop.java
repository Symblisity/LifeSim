package sim;

import java.util.concurrent.locks.LockSupport;

public final class SimulationLoop implements Runnable {

    private final World world;
    private final long tickNanos;
    private volatile int ticksPerStep;
    private volatile boolean running = true;

    public SimulationLoop(World world, int tickRateHz, int speedMultiplier) {
        this.world = world;
        this.tickNanos = 1_000_000_000L / tickRateHz;
        this.ticksPerStep = Math.max(1, speedMultiplier);
    }

    @Override
    public void run() {
        long next = System.nanoTime();
        while (running) {
            synchronized (world) {
                for(int i = 0; i < ticksPerStep; i++) {
                    world.tick();
                }
            }
            next += tickNanos;
            long sleep = next - System.nanoTime();
            if (sleep > 0) {
                LockSupport.parkNanos(sleep);
            } else {
                next = System.nanoTime(); // Fell behind, do not try to catch up
            }
        }
    }

    public void setSpeedMultiplier(int ticksPerStep) {
        this.ticksPerStep = Math.max(1, ticksPerStep);
    }

    public void stop() {
        running = false;
    }

}
