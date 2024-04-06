
import java.util.concurrent.*;
import java.util.*;

class TemperatureSensor implements Runnable {
    private ConcurrentSkipListMap<Long, Integer> sharedMemory;
    private final int sensorId;

    public TemperatureSensor(ConcurrentSkipListMap<Long, Integer> sharedMemory, int sensorId) {
        this.sharedMemory = sharedMemory;
        this.sensorId = sensorId;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            int temperature = ThreadLocalRandom.current().nextInt(-100, 71); // Random temperature between -100F and 70F
            long timestamp = System.currentTimeMillis();
            sharedMemory.put(timestamp, temperature);

            try {
                TimeUnit.MINUTES.sleep(1); // Simulate a 1-minute interval between readings
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
                System.out.println("Sensor " + sensorId + " interrupted.");
            }
        }
    }
}

class TemperatureReportGenerator implements Runnable {
    private ConcurrentSkipListMap<Long, Integer> sharedMemory;

    public TemperatureReportGenerator(ConcurrentSkipListMap<Long, Integer> sharedMemory) {
        this.sharedMemory = sharedMemory;
    }

    @Override
    public void run() {
        // Hourly report generation logic
        // This includes extracting the top 5 highest and lowest temperatures and calculating the 10-minute interval with the largest temperature difference.
        System.out.println("Generating hourly report...");
        // Implementation details omitted for brevity
    }
}

public class MarsRoverTemperatureModule {
    public static void main(String[] args) {
        ConcurrentSkipListMap<Long, Integer> sharedMemory = new ConcurrentSkipListMap<>();
        ScheduledExecutorService sensorExecutor = Executors.newScheduledThreadPool(8);
        ScheduledExecutorService reportExecutor = Executors.newSingleThreadScheduledExecutor();

        // Initialize and start sensor threads
        for (int i = 0; i < 8; i++) {
            sensorExecutor.scheduleAtFixedRate(new TemperatureSensor(sharedMemory, i), 0, 1, TimeUnit.MINUTES);
        }

        // Schedule the report generator every hour
        reportExecutor.scheduleAtFixedRate(new TemperatureReportGenerator(sharedMemory), 1, 1, TimeUnit.HOURS);

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            sensorExecutor.shutdownNow();
            reportExecutor.shutdownNow();
            System.out.println("Shutdown complete.");
        }));
    }
}
