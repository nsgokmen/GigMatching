import java.io.*;
import java.util.Locale;

/**
 * Main entry point for the platform.
 * Reads commands from input file and writes results to output file.
 */
public class Main {
    // The platform that handles all the business logic
    private static Platform platform = new Platform();

    // Start here - read input file, process commands, write output file
    public static void main(String[] args) {
        // Use US locale for consistent number formatting
        Locale.setDefault(Locale.US);
        
        // Need exactly 2 arguments: input file and output file
        if (args.length != 2) {
            System.err.println("Usage: java Main <input_file> <output_file>");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        // Read input file line by line, process each command, write results
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();  // Remove leading/trailing spaces
                if (line.isEmpty()) {
                    continue;  // Skip empty lines
                }

                // Process this command and write the result
                processCommand(line, writer);
            }

        } catch (IOException e) {
            System.err.println("Error reading/writing files: " + e.getMessage());
            e.printStackTrace();
        }
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

            // Figure out which command it is and handle it
            switch (operation) {
                case "register_customer":
                    // Register a new customer
                    if (parts.length != 2) {
                        result = "Some error occurred in register customer.";
                    } else {
                        result = platform.registerCustomer(parts[1]);
                    }
                    break;

                case "register_freelancer":
                    // Register a new freelancer with ID, service, price, and skills
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
                    break;

                case "request_job":
                    // Find top K freelancers for a service request
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
                    break;

                case "employ":
                case "employ_freelancer":
                    // Manually hire a specific freelancer
                    if (parts.length != 3) {
                        result = "Some error occurred in employ.";
                    } else {
                        result = platform.employ(parts[1], parts[2]);
                    }
                    break;

                case "complete_and_rate":
                    // Complete a job and rate the freelancer (0-5 stars)
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
                    break;

                case "cancel_by_freelancer":
                    // Freelancer cancels the job
                    if (parts.length != 2) {
                        result = "Some error occurred in cancel byfreelancer";
                    } else {
                        result = platform.cancelByFreelancer(parts[1]);
                    }
                    break;

                case "cancel_by_customer":
                    // Customer cancels the job
                    if (parts.length != 3) {
                        result = "Some error occurred in cancel bycustomer.";
                    } else {
                        result = platform.cancelByCustomer(parts[1], parts[2]);
                    }
                    break;

                case "blacklist":
                    // Add a freelancer to customer's blacklist
                    if (parts.length != 3) {
                        result = "Some error occurred in blacklist.";
                    } else {
                        result = platform.blacklist(parts[1], parts[2]);
                    }
                    break;

                case "unblacklist":
                    // Remove a freelancer from customer's blacklist
                    if (parts.length != 3) {
                        result = "Some error occurred in unblacklist.";
                    } else {
                        result = platform.unblacklist(parts[1], parts[2]);
                    }
                    break;

                case "change_service":
                    // Change a freelancer's service (applied at end of month)
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
                    break;

                case "simulate_month":
                    // Process end-of-month events (burnout, service changes, loyalty tiers)
                    result = platform.simulateMonth();
                    break;

                case "query_freelancer":
                    // Get information about a freelancer
                    if (parts.length != 2) {
                        result = "Some error occurred in query freelancer.";
                    } else {
                        result = platform.queryFreelancer(parts[1]);
                    }
                    break;

                case "query_customer":
                    // Get information about a customer
                    if (parts.length != 2) {
                        result = "Some error occurred in query customer.";
                    } else {
                        result = platform.queryCustomer(parts[1]);
                    }
                    break;

                case "update_skill":
                    // Update a freelancer's skills directly
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
