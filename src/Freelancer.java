/**
 * Represents a freelancer in the platform.
 * Stores their skills, rating, employment status, and other info.
 */
class Freelancer {
    // Basic info
    String id;  // Unique identifier
    String serviceType;  // What service they offer (e.g., "web_dev", "graphic_design")
    int price;  // How much they charge
    
    // Skills (0-100 each)
    int T, C, R, E, A;  // Technical, Communication, Reliability, Efficiency, Attitude
    
    // Rating and job history
    double rating;  // Current average rating (0-5)
    private int ratingCount;  // Number of ratings received
    int completedJobs;  // How many jobs they've completed
    int cancelledJobs;  // How many jobs they've cancelled
    
    // Status flags
    boolean available;  // Are they available for new jobs?
    boolean burnedOut;  // Are they burned out (did too many jobs this month)?
    boolean isEmployed;  // Are they currently working on a job?
    
    // Monthly tracking (reset each month)
    private int jobsThisMonth;  // How many jobs they did this month
    private int cancellationsThisMonth;  // How many cancellations this month
    
    // Service change queue (applied at end of month)
    private String queuedService;  // New service they want to switch to
    private int queuedPrice;  // New price they want to charge
    
    // Composite score (temporarily stored for heap operations)
    private int compositeScore;  // Calculated based on service requirements

    // Create a new freelancer with given info
    public Freelancer(String id, String serviceType, int price, int T, int C, int R, int E, int A) {
        this.id = id;
        this.serviceType = serviceType;
        this.price = price;
        this.T = T;
        this.C = C;
        this.R = R;
        this.E = E;
        this.A = A;
        this.rating = 5.0;  // Start with perfect rating
        this.ratingCount = 1;  // The initial 5.0 counts as one rating
        this.completedJobs = 0;
        this.cancelledJobs = 0;
        this.available = true;
        this.burnedOut = false;
        this.isEmployed = false;
        this.jobsThisMonth = 0;
        this.queuedService = null;
        this.queuedPrice = -1;
        this.cancellationsThisMonth = 0;
    }

    // Getter methods
    public String getId() { return id; }
    public String getServiceType() { return serviceType; }
    public int getPrice() { return price; }
    public int getT() { return T; }
    public int getC() { return C; }
    public int getR() { return R; }
    public int getE() { return E; }
    public int getA() { return A; }
    public double getRating() { return rating; }
    public int getCompletedJobs() { return completedJobs; }
    public int getCancelledJobs() { return cancelledJobs; }
    public boolean isAvailable() { return available; }
    public boolean isBurnedOut() { return burnedOut; }
    public int getJobsThisMonth() { return jobsThisMonth; }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    // Called when freelancer completes a job and gets rated
    // Updates rating and gives skill improvements if rating is good
    public void completeJob(int newRating, Service service) {
        available = true;  // They're available for new jobs now
        
        // Count total jobs BEFORE we increment (important for rating calculation)
        int n = completedJobs + cancelledJobs;
        completedJobs++;
        jobsThisMonth++;
        
        // Update rating using the formula: newavg = (oldavg * n + rating) / (n + 1)
        // But we also count the initial 5.0 rating, so total ratings = n + 1
        // For first job: n = 0, but we treat as n = 1 (the initial rating)
        // So: rating = (5.0 * 1 + newRating) / 2
        if (n == 0) {
            // First job: the initial 5.0 counts as one rating
            this.rating = (5.0 * 1 + newRating) / 2.0;
        } else {
            // Total ratings so far = n + 1 (including initial 5.0)
            // So for next rating, we use (n + 1) as the count
            int totalRatings = n + 1;
            this.rating = (this.rating * totalRatings + newRating) / (totalRatings + 1);
        }
        ratingCount = completedJobs + cancelledJobs;

        // If rating is 4 or 5, improve their skills
        if (newRating >= 4) {
            applySkillGains(service);
        }
    }

    // Improve skills based on the service they completed
    // Primary skill gets +2, secondary skills get +1 each
    private void applySkillGains(Service service) {
        int[] profile = service.getSkillProfile();
        int primaryIdx = service.getPrimarySkill();  // Which skill is most important for this service
        int[] secondary = service.getSecondarySkills();  // Other important skills

        // Primary skill: increase by 2 (max 100)
        switch (primaryIdx) {
            case 0: T = Math.min(100, T + 2); break;
            case 1: C = Math.min(100, C + 2); break;
            case 2: R = Math.min(100, R + 2); break;
            case 3: E = Math.min(100, E + 2); break;
            case 4: A = Math.min(100, A + 2); break;
        }

        // Secondary skills: increase by 1 each (max 100)
        for (int idx : secondary) {
            switch (idx) {
                case 0: T = Math.min(100, T + 1); break;
                case 1: C = Math.min(100, C + 1); break;
                case 2: R = Math.min(100, R + 1); break;
                case 3: E = Math.min(100, E + 1); break;
                case 4: A = Math.min(100, A + 1); break;
            }
        }
    }

    // Called when freelancer cancels a job
    // Applies 0-star rating and degrades skills
    public void cancelJob() {
        available = true;  // They're available again
        
        // Count total jobs BEFORE we increment (important for rating calculation)
        int n = completedJobs + cancelledJobs;
        cancelledJobs++;
        cancellationsThisMonth++;
        
        // Apply 0-star rating using same formula
        // For first cancellation: n = 0, but we treat as n = 1 (the initial rating)
        // So: rating = (5.0 * 1 + 0) / 2 = 2.5
        if (n == 0) {
            rating = (5.0 * 1 + 0.0) / 2.0;
        } else {
            int totalRatings = n + 1;  // Including initial 5.0
            rating = (rating * totalRatings + 0.0) / (totalRatings + 1);
        }
        ratingCount = completedJobs + cancelledJobs;
        
        // Degrade all skills by 3 (min 0)
        T = Math.max(0, T - 3);
        C = Math.max(0, C - 3);
        R = Math.max(0, R - 3);
        E = Math.max(0, E - 3);
        A = Math.max(0, A - 3);
    }

    // Queue a service change (will be applied at end of month)
    public void queueServiceChange(String newService, int newPrice) {
        this.queuedService = newService;
        this.queuedPrice = newPrice;
    }

    // Check if they have a service change queued
    public boolean hasQueuedServiceChange() {
        return queuedService != null;
    }

    // Get the queued service name
    public String getQueuedService() {
        return queuedService;
    }

    // Get the queued price
    public int getQueuedPrice() {
        return queuedPrice;
    }

    // Apply the queued service change (called at end of month)
    public void applyServiceChange(String newService, int newPrice) {
        this.serviceType = newService;
        this.price = newPrice;
        this.queuedService = null;  // Clear the queue
        this.queuedPrice = -1;
    }

    // Reset monthly counters (called at end of month)
    public void resetMonth() {
        jobsThisMonth = 0;
        cancellationsThisMonth = 0;
    }

    // Update burnout status based on jobs this month
    // Burned out if did 5+ jobs, recovers if did 2 or fewer
    public void updateBurnout() {
        if (!burnedOut && jobsThisMonth >= 5) {
            burnedOut = true;  // Too many jobs, burned out
        } else if (burnedOut && jobsThisMonth <= 2) {
            burnedOut = false;  // Had a light month, recovered
        }
    }

    // Check if freelancer should be banned (too many cancellations this month)
    public boolean shouldBePlatformBanned() {
        return cancellationsThisMonth >= 5;
    }

    // Update skills directly (manual update)
    public void updateSkills(int T, int C, int R, int E, int A) {
        this.T = T;
        this.C = C;
        this.R = R;
        this.E = E;
        this.A = A;
    }

    // Get all skills as an array
    public int[] getSkillProfile() {
        return new int[]{T, C, R, E, A};
    }
    
    // Methods for MaxHeap compatibility
    public String getID() {
        return id;
    }
    
    public int getCompositeScore() {
        return compositeScore;
    }
    
    public void setCompositeScore(int score) {
        this.compositeScore = score;
    }
}
