package com.example.vmscheduling;

import org.apache.commons.lang3.time.StopWatch;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class VMSchedulingSimulator {

    public static void main(String[] args) {
        // Simulation parameters
        int numVMs = 3; // Changed from 5 to 3
        int numTasks = 20;
        int maxTaskDuration = 10; // in time units

        // Generate random tasks
        List<Task> tasks = generateRandomTasks(numTasks, maxTaskDuration);

        System.out.println("===============================================");
        System.out.println("            VM Scheduling Simulation           ");
        System.out.println("===============================================");
        System.out.println("Number of VMs      : " + numVMs);
        System.out.println("Number of Tasks    : " + numTasks);
        System.out.println("Max Task Duration  : " + maxTaskDuration + " units");
        System.out.println("===============================================\n");

        // Run Round Robin scheduling
        System.out.println(">>>>>>>>>> Round Robin Scheduling <<<<<<<<<<");
        roundRobinScheduling(new ArrayList<>(tasks), numVMs);

        // Run Shortest Job First scheduling
        System.out.println("\n>>>>>>>> Shortest Job First Scheduling <<<<<<<<");
        shortestJobFirstScheduling(new ArrayList<>(tasks), numVMs);
    }

    static class Task {
        int id;
        int duration;
        int startTime;
        int endTime;

        Task(int id, int duration) {
            this.id = id;
            this.duration = duration;
        }

        @Override
        public String toString() {
            return "Task-" + id + " (" + duration + " units)";
        }
    }

    static class VM {
        int id;
        List<Task> tasks = new ArrayList<>();
        int currentTime = 0;

        VM(int id) {
            this.id = id;
        }

        void addTask(Task task) {
            task.startTime = currentTime;
            task.endTime = currentTime + task.duration;
            currentTime = task.endTime;
            tasks.add(task);
        }

        int getTotalWorkTime() {
            return currentTime;
        }

        double getAverageWaitTime() {
            if (tasks.isEmpty()) return 0;
            double totalWait = 0;
            for (Task t : tasks) {
                totalWait += t.startTime;
            }
            return totalWait / tasks.size();
        }

        double getUtilization(int totalSimulationTime) {
            if (totalSimulationTime == 0) return 0;
            double busyTime = 0;
            for (Task t : tasks) {
                busyTime += t.duration;
            }
            return (busyTime / totalSimulationTime) * 100;
        }
    }

    private static List<Task> generateRandomTasks(int count, int maxDuration) {
        List<Task> tasks = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            tasks.add(new Task(i + 1, random.nextInt(maxDuration) + 1));
        }
        return tasks;
    }

    private static void roundRobinScheduling(List<Task> tasks, int numVMs) {
        StopWatch stopWatch = StopWatch.createStarted();
        List<VM> vms = new ArrayList<>();
        for (int i = 0; i < numVMs; i++) {
            vms.add(new VM(i + 1));
        }

        int currentVM = 0;
        while (!tasks.isEmpty()) {
            Task task = tasks.remove(0);
            vms.get(currentVM).addTask(task);
            currentVM = (currentVM + 1) % numVMs;
        }

        stopWatch.stop();
        printPerformanceMetrics(vms, stopWatch.getTime(TimeUnit.MILLISECONDS));
    }

    private static void shortestJobFirstScheduling(List<Task> tasks, int numVMs) {
        StopWatch stopWatch = StopWatch.createStarted();
        List<VM> vms = new ArrayList<>();
        for (int i = 0; i < numVMs; i++) {
            vms.add(new VM(i + 1));
        }

        tasks.sort(Comparator.comparingInt(t -> t.duration));

        for (Task task : tasks) {
            VM leastLoadedVM = vms.stream()
                .min(Comparator.comparingInt(VM::getTotalWorkTime))
                .orElse(vms.get(0));
            leastLoadedVM.addTask(task);
        }

        stopWatch.stop();
        printPerformanceMetrics(vms, stopWatch.getTime(TimeUnit.MILLISECONDS));
    }

    private static void printPerformanceMetrics(List<VM> vms, long executionTimeMs) {
        int totalSimulationTime = vms.stream()
            .mapToInt(VM::getTotalWorkTime)
            .max()
            .orElse(0);

        System.out.println("\n>>> Performance Metrics <<<");
        System.out.println("Execution Time         : " + executionTimeMs + " ms");
        System.out.println("Total Simulation Time  : " + totalSimulationTime + " units\n");

        System.out.println("VM\tTasks\tTotal Time\tAvg Wait\tUtilization");
        System.out.println("-----------------------------------------------------------");

        for (VM vm : vms) {
            System.out.printf("%d\t%d\t%d\t\t%.1f\t\t%.1f%%\n",
                vm.id,
                vm.tasks.size(),
                vm.getTotalWorkTime(),
                vm.getAverageWaitTime(),
                vm.getUtilization(totalSimulationTime));
        }

        double avgWaitTime = vms.stream()
            .mapToDouble(VM::getAverageWaitTime)
            .average()
            .orElse(0);

        double avgUtilization = vms.stream()
            .mapToDouble(vm -> vm.getUtilization(totalSimulationTime))
            .average()
            .orElse(0);

        System.out.println("\nOverall Average Wait Time   : " + avgWaitTime);
        System.out.println("Overall Average Utilization : " + avgUtilization + "%");

        double[] workloads = vms.stream()
            .mapToDouble(VM::getTotalWorkTime)
            .toArray();
        double mean = Arrays.stream(workloads).average().orElse(0);
        double variance = Arrays.stream(workloads)
            .map(w -> Math.pow(w - mean, 2))
            .average()
            .orElse(0);
        double stdDev = Math.sqrt(variance);

        System.out.println("Load Balancing (Std Dev)    : " + stdDev);
        System.out.println("==========================================================\n");
    }
}
