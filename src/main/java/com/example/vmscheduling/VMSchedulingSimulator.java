package com.example.vmscheduling;

import java.util.*;

public class VMSchedulingSimulator {

    public static void main(String[] args) {
        int numVMs = 3;
        int numTasks = 20;
        int maxTaskDuration = 10;

        List<Task> tasks = generateRandomTasks(numTasks, maxTaskDuration);

        System.out.println("===============================================");
        System.out.println("            VM Scheduling Simulation           ");
        System.out.println("===============================================");
        System.out.println("Number of VMs      : " + numVMs);
        System.out.println("Number of Tasks    : " + numTasks);
        System.out.println("Max Task Duration  : " + maxTaskDuration + " units");
        System.out.println("===============================================\n");

        System.out.println(">>>>>>>>>> Round Robin Scheduling <<<<<<<<<<");
        roundRobinScheduling(new ArrayList<>(tasks), numVMs);

        System.out.println("\n>>>>>>>> Shortest Job First Scheduling <<<<<<<<");
        shortestJobFirstScheduling(new ArrayList<>(tasks), numVMs);
    }

    static class Task {
        int id, duration, startTime, endTime;

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
            return tasks.stream().mapToDouble(t -> t.startTime).sum() / tasks.size();
        }

        double getUtilization(int totalSimulationTime) {
            if (totalSimulationTime == 0) return 0;
            double busyTime = tasks.stream().mapToDouble(t -> t.duration).sum();
            return (busyTime / totalSimulationTime) * 100;
        }
    }

    // New: DataCenter class to hold VMs
    static class DataCenter {
        List<VM> vms;

        DataCenter(int numVMs) {
            vms = new ArrayList<>();
            for (int i = 0; i < numVMs; i++) {
                vms.add(new VM(i + 1));
            }
        }

        List<VM> getVMs() {
            return vms;
        }

        VM getVMByIndex(int index) {
            return vms.get(index);
        }

        VM getLeastLoadedVM() {
            return vms.stream()
                    .min(Comparator.comparingInt(VM::getTotalWorkTime))
                    .orElse(vms.get(0));
        }

        int getTotalSimulationTime() {
            return vms.stream().mapToInt(VM::getTotalWorkTime).max().orElse(0);
        }

        void printPerformanceMetrics(long executionTimeMs) {
            int totalSimTime = getTotalSimulationTime();

            System.out.println("\n>>> Performance Metrics <<<");
            System.out.println("Execution Time         : " + executionTimeMs + " ms");
            System.out.println("Total Simulation Time  : " + totalSimTime + " units\n");

            System.out.println("VM\tTasks\tTotal Time\tAvg Wait\tUtilization");
            System.out.println("-----------------------------------------------------------");

            for (VM vm : vms) {
                System.out.printf("%d\t%d\t%d\t\t%.1f\t\t%.1f%%\n",
                        vm.id,
                        vm.tasks.size(),
                        vm.getTotalWorkTime(),
                        vm.getAverageWaitTime(),
                        vm.getUtilization(totalSimTime));
            }

            double avgWaitTime = vms.stream()
                    .mapToDouble(VM::getAverageWaitTime)
                    .average()
                    .orElse(0);

            double avgUtilization = vms.stream()
                    .mapToDouble(vm -> vm.getUtilization(totalSimTime))
                    .average()
                    .orElse(0);

            double[] workloads = vms.stream()
                    .mapToDouble(VM::getTotalWorkTime)
                    .toArray();
            double mean = Arrays.stream(workloads).average().orElse(0);
            double variance = Arrays.stream(workloads)
                    .map(w -> Math.pow(w - mean, 2))
                    .average()
                    .orElse(0);
            double stdDev = Math.sqrt(variance);

            System.out.println("\nOverall Average Wait Time   : " + avgWaitTime);
            System.out.println("Overall Average Utilization : " + avgUtilization + "%");
            System.out.println("Load Balancing (Std Dev)    : " + stdDev);
            System.out.println("==========================================================\n");
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
        long startTime = System.nanoTime();

        DataCenter dataCenter = new DataCenter(numVMs);
        int currentVM = 0;
        while (!tasks.isEmpty()) {
            Task task = tasks.remove(0);
            dataCenter.getVMByIndex(currentVM).addTask(task);
            currentVM = (currentVM + 1) % numVMs;
        }

        long endTime = System.nanoTime();
        dataCenter.printPerformanceMetrics((endTime - startTime) / 1_000_000);
    }

    private static void shortestJobFirstScheduling(List<Task> tasks, int numVMs) {
        long startTime = System.nanoTime();

        DataCenter dataCenter = new DataCenter(numVMs);
        tasks.sort(Comparator.comparingInt(t -> t.duration));

        for (Task task : tasks) {
            dataCenter.getLeastLoadedVM().addTask(task);
        }

        long endTime = System.nanoTime();
        dataCenter.printPerformanceMetrics((endTime - startTime) / 1_000_000);
    }
}
