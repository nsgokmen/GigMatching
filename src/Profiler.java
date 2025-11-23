/**
 * Simple profiler to track method call counts and execution times.
 * Can be used to identify performance bottlenecks.
 */
import java.util.*;

public class Profiler {
    // Track call counts for each method
    private static Map<String, Integer> callCounts = new HashMap<>();
    
    // Track total execution time for each method (in nanoseconds)
    private static Map<String, Long> totalTimes = new HashMap<>();
    
    // Track individual call times for statistics
    private static Map<String, List<Long>> callTimes = new HashMap<>();
    
    // Track nested calls
    private static ThreadLocal<Stack<String>> callStack = ThreadLocal.withInitial(Stack::new);
    private static ThreadLocal<Stack<Long>> startTimes = ThreadLocal.withInitial(Stack::new);
    
    // Enable/disable profiling
    private static boolean enabled = true;
    
    /**
     * Start profiling a method call.
     */
    public static void start(String methodName) {
        if (!enabled) return;
        
        callStack.get().push(methodName);
        startTimes.get().push(System.nanoTime());
    }
    
    /**
     * End profiling a method call.
     */
    public static void end(String methodName) {
        if (!enabled) return;
        
        if (callStack.get().isEmpty() || startTimes.get().isEmpty()) {
            return;
        }
        
        long endTime = System.nanoTime();
        long startTime = startTimes.get().pop();
        String currentMethod = callStack.get().pop();
        
        if (!currentMethod.equals(methodName)) {
            // Mismatch - restore stack
            callStack.get().push(currentMethod);
            startTimes.get().push(startTime);
            return;
        }
        
        long duration = endTime - startTime;
        
        // Update statistics
        callCounts.put(methodName, callCounts.getOrDefault(methodName, 0) + 1);
        totalTimes.put(methodName, totalTimes.getOrDefault(methodName, 0L) + duration);
        
        callTimes.putIfAbsent(methodName, new ArrayList<>());
        callTimes.get(methodName).add(duration);
    }
    
    /**
     * Record a method call with duration.
     */
    public static void record(String methodName, long durationNanos) {
        if (!enabled) return;
        
        callCounts.put(methodName, callCounts.getOrDefault(methodName, 0) + 1);
        totalTimes.put(methodName, totalTimes.getOrDefault(methodName, 0L) + durationNanos);
        
        callTimes.putIfAbsent(methodName, new ArrayList<>());
        callTimes.get(methodName).add(durationNanos);
    }
    
    /**
     * Enable profiling.
     */
    public static void enable() {
        enabled = true;
    }
    
    /**
     * Disable profiling.
     */
    public static void disable() {
        enabled = false;
    }
    
    /**
     * Reset all profiling data.
     */
    public static void reset() {
        callCounts.clear();
        totalTimes.clear();
        callTimes.clear();
    }
    
    /**
     * Generate a profiling report.
     */
    public static String generateReport() {
        if (callCounts.isEmpty()) {
            return "No profiling data collected.\n";
        }
        
        StringBuilder report = new StringBuilder();
        report.append("=".repeat(80)).append("\n");
        report.append("PROFILER REPORT\n");
        report.append("=".repeat(80)).append("\n\n");
        
        // Sort methods by total time (descending)
        List<Map.Entry<String, Long>> sortedByTime = new ArrayList<>(totalTimes.entrySet());
        sortedByTime.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        
        // Total execution time
        long totalProgramTime = sortedByTime.get(0).getValue();
        for (Map.Entry<String, Long> entry : totalTimes.entrySet()) {
            if (entry.getValue() > totalProgramTime) {
                totalProgramTime = entry.getValue();
            }
        }
        
        report.append(String.format("Total Profiled Methods: %d\n\n", callCounts.size()));
        
        // Summary table
        report.append("METHOD PERFORMANCE SUMMARY (sorted by total time)\n");
        report.append("-".repeat(80)).append("\n");
        report.append(String.format("%-50s %12s %12s %12s %12s %12s\n", 
            "Method", "Calls", "Total (ms)", "Avg (ms)", "Min (ms)", "Max (ms)"));
        report.append("-".repeat(80)).append("\n");
        
        for (Map.Entry<String, Long> entry : sortedByTime) {
            String method = entry.getKey();
            int calls = callCounts.get(method);
            long totalNanos = entry.getValue();
            double totalMs = totalNanos / 1_000_000.0;
            double avgMs = totalMs / calls;
            
            List<Long> times = callTimes.get(method);
            long minNanos = times.stream().mapToLong(Long::longValue).min().orElse(0);
            long maxNanos = times.stream().mapToLong(Long::longValue).max().orElse(0);
            double minMs = minNanos / 1_000_000.0;
            double maxMs = maxNanos / 1_000_000.0;
            
            report.append(String.format("%-50s %12d %12.3f %12.3f %12.3f %12.3f\n",
                method, calls, totalMs, avgMs, minMs, maxMs));
        }
        
        report.append("\n");
        
        // Most called methods
        List<Map.Entry<String, Integer>> sortedByCalls = new ArrayList<>(callCounts.entrySet());
        sortedByCalls.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        
        report.append("MOST CALLED METHODS (top 10)\n");
        report.append("-".repeat(80)).append("\n");
        for (int i = 0; i < Math.min(10, sortedByCalls.size()); i++) {
            Map.Entry<String, Integer> entry = sortedByCalls.get(i);
            report.append(String.format("%-50s %12d calls\n", entry.getKey(), entry.getValue()));
        }
        
        report.append("\n");
        
        // Slowest average methods
        List<Map.Entry<String, Double>> sortedByAvg = new ArrayList<>();
        for (Map.Entry<String, Long> entry : totalTimes.entrySet()) {
            String method = entry.getKey();
            int calls = callCounts.get(method);
            double avgMs = (entry.getValue() / 1_000_000.0) / calls;
            sortedByAvg.add(new AbstractMap.SimpleEntry<>(method, avgMs));
        }
        sortedByAvg.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        report.append("SLOWEST AVERAGE METHODS (top 10)\n");
        report.append("-".repeat(80)).append("\n");
        for (int i = 0; i < Math.min(10, sortedByAvg.size()); i++) {
            Map.Entry<String, Double> entry = sortedByAvg.get(i);
            report.append(String.format("%-50s %12.3f ms/call\n", entry.getKey(), entry.getValue()));
        }
        
        report.append("\n");
        report.append("=".repeat(80)).append("\n");
        
        return report.toString();
    }
    
    /**
     * Get call count for a method.
     */
    public static int getCallCount(String methodName) {
        return callCounts.getOrDefault(methodName, 0);
    }
    
    /**
     * Get total time for a method (in milliseconds).
     */
    public static double getTotalTime(String methodName) {
        return totalTimes.getOrDefault(methodName, 0L) / 1_000_000.0;
    }
    
    /**
     * Get average time for a method (in milliseconds).
     */
    public static double getAverageTime(String methodName) {
        int calls = callCounts.getOrDefault(methodName, 0);
        if (calls == 0) return 0.0;
        return (totalTimes.getOrDefault(methodName, 0L) / 1_000_000.0) / calls;
    }
}


