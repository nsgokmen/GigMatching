/**
 * Represents a job relationship between a customer and a freelancer.
 * Created when a customer hires a freelancer, removed when job is completed or cancelled.
 */
class Employment {
    private String customerId;  // Who hired them
    private String freelancerId;  // Who got hired
    private String serviceType;  // What service is being provided

    // Create a new employment relationship
    public Employment(String customerId, String freelancerId, String serviceType) {
        this.customerId = customerId;
        this.freelancerId = freelancerId;
        this.serviceType = serviceType;
    }

    // Getter methods
    public String getCustomerId() { return customerId; }
    public String getFreelancerId() { return freelancerId; }
    public String getServiceType() { return serviceType; }
}
