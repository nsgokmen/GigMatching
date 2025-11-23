/**
 * Represents a customer in the platform.
 * Tracks spending, loyalty tier, and blacklisted freelancers.
 */
class Customer {
    private String id;  // Unique identifier
    private int totalSpent;  // How much money they've spent total
    private int loyaltyPenalty;  // Penalties from cancellations (reduces effective spending)
    private String loyaltyTier;  // BRONZE, SILVER, GOLD, or PLATINUM
    private HashTable blacklist;  // Freelancers they don't want to work with
    private int totalEmploymentCount;  // How many times they've hired someone

    // Create a new customer
    public Customer(String id) {
        this.id = id;
        this.totalSpent = 0;
        this.loyaltyPenalty = 0;
        this.loyaltyTier = "BRONZE";  // Everyone starts as bronze
        this.blacklist = new HashTable(1000);
        this.totalEmploymentCount = 0;
    }

    // Getter methods
    public String getId() { return id; }
    public int getTotalSpent() { return totalSpent; }
    
    // Get loyalty tier (only updated during simulate_month, not on every payment)
    public String getLoyaltyTier() {
        return loyaltyTier;
    }
    
    public int getBlacklistCount() { return blacklist.size(); }
    public int getTotalEmploymentCount() { return totalEmploymentCount; }

    // Add a payment (increases totalSpent)
    // Tier is NOT updated here - it only updates during simulate_month
    // This way we calculate payment using current tier, then update tier later
    public void addPayment(int amount) {
        totalSpent += amount;
        // Don't update tier here - tier updates only happen during simulate_month
    }

    // Add a cancellation penalty (reduces effective spending)
    // Tier is NOT updated here - it only updates during simulate_month
    public void addCancellationPenalty() {
        loyaltyPenalty += 250;  // Each cancellation costs $250 in effective spending
        // Don't update tier here - tier updates only happen during simulate_month
    }

    // Get effective total spending (actual spending minus penalties)
    // This is what determines loyalty tier
    public int getEffectiveTotalSpent() {
        return totalSpent - loyaltyPenalty;
    }

    // Update loyalty tier based on effective total spending
    // Called during simulate_month
    // Thresholds:
    //   BRONZE: < $500
    //   SILVER: $500 - $1,999
    //   GOLD: $2,000 - $4,999
    //   PLATINUM: $5,000+
    public void updateLoyaltyTier() {
        int effective = getEffectiveTotalSpent();
        if (effective >= 5000) {
            loyaltyTier = "PLATINUM";
        } else if (effective >= 2000) {
            loyaltyTier = "GOLD";
        } else if (effective >= 500) {
            loyaltyTier = "SILVER";
        } else {
            loyaltyTier = "BRONZE";
        }
    }
    
    // Get the discount percentage based on loyalty tier
    // PLATINUM: 15% off, GOLD: 10% off, SILVER: 5% off, BRONZE: 0% off
    public double getSubsidy() {
        if (loyaltyTier.equals("PLATINUM")) return 0.15;
        if (loyaltyTier.equals("GOLD")) return 0.10;
        if (loyaltyTier.equals("SILVER")) return 0.05;
        return 0.0;
    }

    // Calculate how much customer pays for a freelancer
    // Uses current tier to determine discount
    // Tier is based on effective total (after penalties)
    public int calculatePayment(int freelancerPrice) {
        double subsidy = getSubsidy();  // Get discount percentage
        return (int) Math.floor(freelancerPrice * (1.0 - subsidy));
    }

    // Add a freelancer to blacklist
    public void blacklistFreelancer(String freelancerId) {
        blacklist.put(freelancerId, true);
    }

    // Remove a freelancer from blacklist
    public void unblacklistFreelancer(String freelancerId) {
        blacklist.remove(freelancerId);
    }

    // Check if a freelancer is blacklisted
    public boolean isBlacklisted(String freelancerId) {
        return blacklist.containsKey(freelancerId);
    }
    
    // Get the blacklist hash table directly
    // Used when we need to check blacklist quickly during job requests
    HashTable getBlacklistTable() {
        return blacklist;
    }

    // Increase employment count (called when they hire someone)
    public void incrementEmploymentCount() {
        totalEmploymentCount++;
    }
}
