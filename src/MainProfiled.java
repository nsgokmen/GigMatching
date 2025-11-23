import java.io.*;
import java.util.Locale;

/**
 * Profiled version of Main that tracks execution times and generates a profiler report.
 */
public class MainProfiled {
    // The platform that handles all the business logic
    private static Platform platform = new Platform();

    // Start here - read input file, process commands, write output file
    public static void main(String[] args) {
        // Use US locale for consistent number formatting
        Locale.setDefault(Locale.US);
        
        // Need exactly 2 arguments: input file and output file
        if (args.length != 2) {
            System.err.println("Usage: java MainProfiled <input_file> <output_file>");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        // Enable profiling
        Profiler.enable();
        Profiler.reset();
        
        // Track overall execution time
        long totalStartTime = System.nanoTime();

        // Read input file line by line, process each command, write results
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();  // Remove leading/trailing spaces
                if (line.isEmpty()) {
                    continue;  // Skip empty lines
                }

                lineCount++;
                // Process this command and write the result
                processCommand(line, writer);
                
                // Progress indicator for large files
                if (lineCount % 50000 == 0) {
                    System.err.println("Processed " + lineCount + " lines...");
                }
            }
            
            System.err.println("Total lines processed: " + lineCount);

        } catch (IOException e) {
            System.err.println("Error reading/writing files: " + e.getMessage());
            e.printStackTrace();
        }
        
        long totalEndTime = System.nanoTime();
        double totalTimeMs = (totalEndTime - totalStartTime) / 1_000_000.0;
        
        // Generate profiler report
        String report = Profiler.generateReport();
        
        // Write profiler report to a separate file
        String reportFile = outputFile + ".profile.txt";
        try (BufferedWriter reportWriter = new BufferedWriter(new FileWriter(reportFile))) {
            reportWriter.write("Total Execution Time: " + String.format("%.3f", totalTimeMs) + " ms\n");
            reportWriter.write("(" + String.format("%.3f", totalTimeMs / 1000.0) + " seconds)\n\n");
            reportWriter.write(report);
            System.err.println("\nProfiler report written to: " + reportFile);
        } catch (IOException e) {
            System.err.println("Error writing profiler report: " + e.getMessage());
        }
        
        // Also print summary to stderr
        System.err.println("\n=== EXECUTION SUMMARY ===");
        System.err.println("Total execution time: " + String.format("%.3f", totalTimeMs) + " ms");
        System.err.println("Profiler report saved to: " + reportFile);
    }

    // Figure out what command this is and call the right method on the platform
    private static void processCommand(String command, BufferedWriter writer)
            throws IOException {

        // Split command by spaces
        String[] parts = command.split("\\s+");
        if (parts.length == 0) {
            return;
        }
        String operation = parts[0];  // First word is the command name

        try {
            String result = "";
            long startTime = System.nanoTime();

            // Figure out which command it is and handle it
            switch (operation) {
                case "register_customer":
                    Profiler.start("Platform.registerCustomer");
                    if (parts.length != 2) {
                        result = "Some error occurred in register customer.";
                    } else {
                        result = platform.registerCustomer(parts[1]);
                    }
                    Profiler.end("Platform.registerCustomer");
                    break;

                case "register_freelancer":
                    Profiler.start("Platform.registerFreelancer");
                    if (parts.length != 9) {
                        result = "Some error occurred in register freelancer.";
                    } else {
                        try {
                            String id = parts[1];
                            String service = parts[2];
                            int price = Integer.parseInt(parts[3]);
                            int T = Integer.parseInt(parts[4]);
                            int C = Integer.parseInt(parts[5]);
                            int R = Integer.parseInt(parts[6]);
                            int E = Integer.parseInt(parts[7]);
                            int A = Integer.parseInt(parts[8]);
                            result = platform.registerFreelancer(id, service, price, T, C, R, E, A);
                        } catch (NumberFormatException e) {
                            result = "Some error occurred in register freelancer.";
                        }
                    }
                    Profiler.end("Platform.registerFreelancer");
                    break;

                case "request_job":
                    Profiler.start("Platform.requestJob");
                    if (parts.length != 4) {
                        result = "Some error occurred in request job.";
                    } else {
                        try {
                            String customerId = parts[1];
                            String service = parts[2];
                            int topK = Integer.parseInt(parts[3]);
                            result = platform.requestJob(customerId, service, topK);
                        } catch (NumberFormatException e) {
                            result = "Some error occurred in request job.";
                        }
                    }
                    Profiler.end("Platform.requestJob");
                    break;

                case "employ":
                case "employ_freelancer":
                    Profiler.start("Platform.employ");
                    if (parts.length != 3) {
                        result = "Some error occurred in employ.";
                    } else {
                        result = platform.employ(parts[1], parts[2]);
                    }
                    Profiler.end("Platform.employ");
                    break;

                case "complete_and_rate":
                    Profiler.start("Platform.completeAndRate");
                    if (parts.length != 3) {
                        result = "Some error occurred in complete andrate.";
                    } else {
                        try {
                            String freelancerId = parts[1];
                            int rating = Integer.parseInt(parts[2]);
                            result = platform.completeAndRate(freelancerId, rating);
                        } catch (NumberFormatException e) {
                            result = "Some error occurred in complete andrate.";
                        }
                    }
                    Profiler.end("Platform.completeAndRate");
                    break;

                case "cancel_by_freelancer":
                    Profiler.start("Platform.cancelByFreelancer");
                    if (parts.length != 2) {
                        result = "Some error occurred in cancel byfreelancer";
                    } else {
                        result = platform.cancelByFreelancer(parts[1]);
                    }
                    Profiler.end("Platform.cancelByFreelancer");
                    break;

                case "cancel_by_customer":
                    Profiler.start("Platform.cancelByCustomer");
                    if (parts.length != 3) {
                        result = "Some error occurred in cancel bycustomer.";
                    } else {
                        result = platform.cancelByCustomer(parts[1], parts[2]);
                    }
                    Profiler.end("Platform.cancelByCustomer");
                    break;

                case "blacklist":
                    Profiler.start("Platform.blacklist");
                    if (parts.length != 3) {
                        result = "Some error occurred in blacklist.";
                    } else {
                        result = platform.blacklist(parts[1], parts[2]);
                    }
                    Profiler.end("Platform.blacklist");
                    break;

                case "unblacklist":
                    Profiler.start("Platform.unblacklist");
                    if (parts.length != 3) {
                        result = "Some error occurred in unblacklist.";
                    } else {
                        result = platform.unblacklist(parts[1], parts[2]);
                    }
                    Profiler.end("Platform.unblacklist");
                    break;

                case "change_service":
                    Profiler.start("Platform.changeService");
                    if (parts.length != 4) {
                        result = "Some error occurred in change service.";
                    } else {
                        try {
                            String freelancerId = parts[1];
                            String newService = parts[2];
                            int newPrice = Integer.parseInt(parts[3]);
                            result = platform.changeService(freelancerId, newService, newPrice);
                        } catch (NumberFormatException e) {
                            result = "Some error occurred in change service.";
                        }
                    }
                    Profiler.end("Platform.changeService");
                    break;

                case "simulate_month":
                    Profiler.start("Platform.simulateMonth");
                    result = platform.simulateMonth();
                    Profiler.end("Platform.simulateMonth");
                    break;

                case "query_freelancer":
                    Profiler.start("Platform.queryFreelancer");
                    if (parts.length != 2) {
                        result = "Some error occurred in query freelancer.";
                    } else {
                        result = platform.queryFreelancer(parts[1]);
                    }
                    Profiler.end("Platform.queryFreelancer");
                    break;

                case "query_customer":
                    Profiler.start("Platform.queryCustomer");
                    if (parts.length != 2) {
                        result = "Some error occurred in query customer.";
                    } else {
                        result = platform.queryCustomer(parts[1]);
                    }
                    Profiler.end("Platform.queryCustomer");
                    break;

                case "update_skill":
                    Profiler.start("Platform.updateSkill");
                    if (parts.length != 7) {
                        result = "Some error occurred in update skill.";
                    } else {
                        try {
                            String freelancerId = parts[1];
                            int T = Integer.parseInt(parts[2]);
                            int C = Integer.parseInt(parts[3]);
                            int R = Integer.parseInt(parts[4]);
                            int E = Integer.parseInt(parts[5]);
                            int A = Integer.parseInt(parts[6]);
                            result = platform.updateSkill(freelancerId, T, C, R, E, A);
                        } catch (NumberFormatException e) {
                            result = "Some error occurred in update skill.";
                        }
                    }
                    Profiler.end("Platform.updateSkill");
                    break;

                default:
                    // Unknown command
                    result = "Unknown command: " + operation;
            }

            // Write the result to output file
            writer.write(result);
            writer.newLine();

        } catch (Exception e) {
            // If something goes wrong, write an error message
            writer.write("Error processing command: " + command);
            writer.newLine();
        }
    }
}
