/**
 * Helper class to store freelancer with score for topK selection.
 * Not currently used - MinHeap.FreelancerScore is used instead.
 */
class TopKCandidate {
    Freelancer freelancer;  // The freelancer object
    int score;              // Calculated composite score
    String id;              // Freelancer ID for comparison
    
    TopKCandidate(Freelancer f, int s, String id) {
        this.freelancer = f;
        this.score = s;
        this.id = id;
    }
}

/**
 * Main platform manager handling all operations.
 * Manages customers, freelancers, jobs, and matches.
 */
class Platform {
    // Stores all customers by their ID
    private HashTable customers;
    
    // Stores all freelancers by their ID
    private HashTable freelancers;
    
    // Keeps track of which freelancer is currently working for which customer
    private HashTable employments;
    
    // Manages service types and their variations (like "web_dev" vs "web development")
    private ServiceManager serviceManager;
    
    // List of all freelancers (useful when we need to iterate through everyone)
    private FreelancerList allFreelancers;
    
    // List of all customers (useful when we need to iterate through everyone)
    private CustomerList allCustomers;
    
    // Groups freelancers by service type, so we don't have to check all freelancers when searching
    private HashTable serviceIndex;

    // Set up all the data structures we need
    public Platform() {
        customers = new HashTable(100000);
        freelancers = new HashTable(100000);
        employments = new HashTable(100000);
        serviceManager = new ServiceManager();
        allFreelancers = new FreelancerList();
        allCustomers = new CustomerList();
        serviceIndex = new HashTable(100);  // Not many service types
    }

    // Register a new customer with given ID
    public String registerCustomer(String customerId) {
        // Check if ID already exists (as customer or freelancer)
        if (customers.containsKey(customerId) || freelancers.containsKey(customerId)) {
            return "Some error occurred in register customer.";
        }
        // Create new customer and store it
        Customer customer = new Customer(customerId);
        customers.put(customerId, customer);
        allCustomers.add(customer);  // Also add to list for when we need to go through all customers
        
        // Build the response message
        StringBuilder sb = new StringBuilder(30);
        sb.append("registered customer ").append(customerId);
        return sb.toString();
    }

    // Register a new freelancer with given ID, service, price, and skills
    public String registerFreelancer(String id, String serviceName, int price, int T, int C, int R, int E, int A) {
        // Check if ID already exists (as customer or freelancer)
        if (customers.containsKey(id) || freelancers.containsKey(id)) {
            return "Some error occurred in register freelancer.";
        }
        // Validate price and skills (must be 0-100)
        if (price <= 0 || T < 0 || T > 100 || C < 0 || C > 100 || R < 0 || R > 100 || E < 0 || E > 100 || A < 0 || A > 100) {
            return "Some error occurred in register freelancer.";
        }
        // Validate service name
        if (!serviceManager.isValidService(serviceName)) {
            return "Some error occurred in register freelancer.";
        }
        // Create new freelancer and store it
        Freelancer freelancer = new Freelancer(id, serviceName, price, T, C, R, E, A);
        freelancers.put(id, freelancer);
        allFreelancers.add(freelancer);  // Also add to list for when we need to go through all freelancers
        
        // Group this freelancer by service type in max heap sorted by composite score
        Service service = serviceManager.getService(serviceName);
        if (service != null) {
            // Get the standardized service name (handles things like "web_dev" vs "web development")
            String normalizedServiceName = service.getName();
            // Get the max heap of freelancers for this service, or create one if it doesn't exist
            ServiceMaxHeap serviceHeap = (ServiceMaxHeap) serviceIndex.get(normalizedServiceName);
            if (serviceHeap == null) {
                serviceHeap = new ServiceMaxHeap(service);
                serviceIndex.put(normalizedServiceName, serviceHeap);
            }
            serviceHeap.add(freelancer);
        }
        
        return "registered freelancer " + id;
    }

    // Find top K available freelancers for a service request
    // Returns sorted list with composite scores and auto-employs the best one
    public String requestJob(String customerId, String serviceType, int topK) {
        // Get customer from hash table
        Customer customer = (Customer) customers.get(customerId);
        // Validate customer exists and service is valid
        if (customer == null || !serviceManager.isValidService(serviceType)) {
            return "Some error occurred in request job.";
        }

        // Get service object to access normalized name and skill profile
        Service service = serviceManager.getService(serviceType);
        if (service == null) {
            return "Some error occurred in request job.";
        }
        // Get the standardized service name
        String actualServiceName = service.getName();
        
        // Get max heap for this service (sorted by composite score, highest first)
        ServiceMaxHeap serviceHeap = (ServiceMaxHeap) serviceIndex.get(actualServiceName);
        if (serviceHeap == null || serviceHeap.isEmpty()) {
            return "no freelancers available";
        }
        
        // Get customer's blacklist (if any)
        HashTable customerBlacklist = customer.getBlacklistCount() > 0 ? customer.getBlacklistTable() : null;
        
        // Get top K freelancers directly from max heap (O(K log N) - no linear search!)
        java.util.ArrayList<Freelancer> topFreelancers = serviceHeap.getTopK(topK, customerBlacklist);
        
        if (topFreelancers == null || topFreelancers.isEmpty()) {
            return "no freelancers available";
        }
        
        int count = topFreelancers.size();

        // Build the response message
        StringBuilder result = new StringBuilder(50 + 80 * count);
        result.append("available freelancers for ").append(serviceType).append(" (top ").append(topK).append("):\n");
        
        // Store these strings once, reuse them
        String compositeStr = " - composite: ";
        String priceStr = ", price: ";
        String ratingStr = ", rating: ";
        
        final double defaultRating = 5.0;  // New freelancers start with 5.0 rating
        // Add each freelancer's info to the result
        for (int i = 0; i < count; i++) {
            Freelancer f = topFreelancers.get(i);
            int score = f.getCompositeScore();
            String fid = f.id;
            
            int price = f.price;
            
            // Show 5.0 if freelancer hasn't done any jobs yet, otherwise show actual rating
            int completed = f.completedJobs;
            int cancelled = f.cancelledJobs;
            double displayRating = (completed + cancelled == 0) ? defaultRating : f.rating;
            String ratingFormatted = formatRating(displayRating);  // Format to one decimal place
            
            // Build the line for this freelancer
            result.append(fid).append(compositeStr).append(score)
                  .append(priceStr).append(price)
                  .append(ratingStr).append(ratingFormatted);
            if (i < count - 1) result.append("\n");
        }

        // Automatically hire the best freelancer (first in the list)
        if (count > 0) {
            Freelancer best = topFreelancers.get(0);
            // Mark as unavailable and employed
            best.available = false;
            best.isEmployed = true;
            // Update freelancer in max heap (no longer available)
            updateFreelancerInHeap(best, serviceType);
            // Create the employment record
            Employment emp = new Employment(customerId, best.id, serviceType);
            employments.put(best.id, emp);
            // Update customer's employment count
            customer.incrementEmploymentCount();
            
            // Add auto-employment message
            result.append("\nauto-employed best freelancer: ").append(best.id)
                  .append(" for customer ").append(customerId);
        }

        return result.toString();
    }

    // Manually employ a specific freelancer for a customer
    public String employ(String customerId, String freelancerId) {
        // Get customer and freelancer from hash tables
        Customer customer = (Customer) customers.get(customerId);
        Freelancer freelancer = (Freelancer) freelancers.get(freelancerId);
        
        // Validate: both exist, freelancer is available, not blacklisted, not already employed
        if (customer == null || freelancer == null || !freelancer.available ||
            customer.isBlacklisted(freelancerId) || freelancer.isEmployed) {
            return "Some error occurred in employ.";
        }

        // Get service type from freelancer
        String serviceType = freelancer.serviceType;
        // Mark freelancer as unavailable and employed
        freelancer.available = false;
        freelancer.isEmployed = true;
        // Update freelancer in max heap (no longer available)
        updateFreelancerInHeap(freelancer, serviceType);
        // Create employment record
        Employment emp = new Employment(customerId, freelancerId, serviceType);
        employments.put(freelancerId, emp);
        // Increment customer's employment count
        customer.incrementEmploymentCount();
        
        // Build response message
        StringBuilder sb = new StringBuilder(60);
        sb.append(customerId).append(" employed ").append(freelancerId)
          .append(" for ").append(serviceType);
        return sb.toString();
    }

    // Complete a job and rate the freelancer (0-5 stars)
    public String completeAndRate(String freelancerId, int rating) {
        // Validate rating is in valid range
        if (rating < 0 || rating > 5) {
            return "Some error occurred in complete andrate.";
        }
        
        // Get employment record
        Employment emp = (Employment) employments.get(freelancerId);
        if (emp == null) {
            return "Some error occurred in complete andrate.";
        }

        // Get customer ID from employment
        String customerId = emp.getCustomerId();
        Freelancer freelancer = (Freelancer) freelancers.get(freelancerId);
        Customer customer = (Customer) customers.get(customerId);
        
        // Get service type from freelancer
        String serviceType = freelancer.serviceType;
        Service service = serviceManager.getService(serviceType);

        // Calculate payment using customer's current loyalty tier (before payment)
        int payment = customer.calculatePayment(freelancer.price);
        // Add payment to customer (this may update loyalty tier after payment)
        customer.addPayment(payment);
        // Update freelancer: apply rating, update skills if rating >= 4
        freelancer.completeJob(rating, service);
        // Mark freelancer as no longer employed
        freelancer.isEmployed = false;
        // Update freelancer in max heap (composite score changed)
        updateFreelancerInHeap(freelancer, serviceType);
        // Remove employment record (job completed)
        employments.remove(freelancerId);

        // Build response message
        StringBuilder sb = new StringBuilder(60);
        sb.append(freelancerId).append(" completed job for ").append(customerId)
          .append(" with rating ").append(rating);
        return sb.toString();
    }

    // Cancel a job by freelancer (freelancer cancels)
    public String cancelByFreelancer(String freelancerId) {
        // Get employment record
        Employment emp = (Employment) employments.get(freelancerId);
        if (emp == null) {
            return "Some error occurred in cancel byfreelancer";
        }

        // Get customer ID before removing employment
        String customerId = emp.getCustomerId();
        Freelancer freelancer = (Freelancer) freelancers.get(freelancerId);
        String serviceType = freelancer.serviceType;
        // Update freelancer: apply 0-star rating, degrade skills, update cancellation count
        freelancer.cancelJob();
        // Mark freelancer as no longer employed
        freelancer.isEmployed = false;
        // Update freelancer in max heap (composite score changed)
        updateFreelancerInHeap(freelancer, serviceType);
        // Remove employment record (job cancelled)
        employments.remove(freelancerId);

        // Build response message
        StringBuilder sb = new StringBuilder(80);
        sb.append("cancelled by freelancer: ").append(freelancerId)
          .append(" cancelled ").append(customerId);
        
        // Check if freelancer should be platform-banned (too many cancellations)
        if (freelancer.shouldBePlatformBanned()) {
            // Ban freelancer from platform (remove from all structures)
            platformBanFreelancer(freelancerId);
            sb.append("\nplatform banned freelancer: ").append(freelancerId);
        }

        return sb.toString();
    }

    // Cancel a job by customer (customer cancels)
    public String cancelByCustomer(String customerId, String freelancerId) {
        // Get employment record
        Employment emp = (Employment) employments.get(freelancerId);
        // Validate: employment exists and belongs to this customer
        if (emp == null || !emp.getCustomerId().equals(customerId)) {
            return "Some error occurred in cancel bycustomer.";
        }

        Freelancer freelancer = (Freelancer) freelancers.get(freelancerId);
        Customer customer = (Customer) customers.get(customerId);
        String serviceType = freelancer.serviceType;
        
        // Mark freelancer as available again
        freelancer.available = true;
        // Mark freelancer as no longer employed
        freelancer.isEmployed = false;
        // Add cancellation penalty to customer (affects loyalty tier calculation)
        customer.addCancellationPenalty();
        // Update freelancer in max heap (now available again)
        updateFreelancerInHeap(freelancer, serviceType);
        // Remove employment record (job cancelled)
        employments.remove(freelancerId);

        // Build response message
        StringBuilder sb = new StringBuilder(60);
        sb.append("cancelled by customer: ").append(customerId)
          .append(" cancelled ").append(freelancerId);
        return sb.toString();
    }

    // Add a freelancer to customer's blacklist
    public String blacklist(String customerId, String freelancerId) {
        // Get customer from hash table
        Customer customer = (Customer) customers.get(customerId);
        // Validate: customer and freelancer exist
        if (customer == null || freelancers.get(freelancerId) == null) {
            return "Some error occurred in blacklist.";
        }
        // Check if already blacklisted
        if (customer.isBlacklisted(freelancerId)) {
            return "Some error occurred in blacklist.";
        }
        // Add freelancer to customer's blacklist
        customer.blacklistFreelancer(freelancerId);
        // Build response message
        StringBuilder sb = new StringBuilder(40);
        sb.append(customerId).append(" blacklisted ").append(freelancerId);
        return sb.toString();
    }

    // Remove a freelancer from customer's blacklist
    public String unblacklist(String customerId, String freelancerId) {
        // Get customer from hash table
        Customer customer = (Customer) customers.get(customerId);
        // Validate: customer and freelancer exist
        if (customer == null || freelancers.get(freelancerId) == null) {
            return "Some error occurred in unblacklist.";
        }
        // Check if actually blacklisted
        if (!customer.isBlacklisted(freelancerId)) {
            return "Some error occurred in unblacklist.";
        }
        // Remove freelancer from customer's blacklist
        customer.unblacklistFreelancer(freelancerId);
        // Build response message
        StringBuilder sb = new StringBuilder(40);
        sb.append(customerId).append(" unblacklisted ").append(freelancerId);
        return sb.toString();
    }

    // Queue a service change for a freelancer (applied at end of month)
    public String changeService(String freelancerId, String newService, int newPrice) {
        // Validate: price is positive and service is valid
        if (newPrice <= 0 || !serviceManager.isValidService(newService)) {
            return "Some error occurred in change service.";
        }
        
        // Get freelancer from hash table
        Freelancer freelancer = (Freelancer) freelancers.get(freelancerId);
        if (freelancer == null) {
            return "Some error occurred in change service.";
        }

        // Get current service type
        String oldService = freelancer.serviceType;
        // Queue service change (applied during simulateMonth)
        freelancer.queueServiceChange(newService, newPrice);
        // Build response message
        StringBuilder sb = new StringBuilder(80);
        sb.append("service change for ").append(freelancerId).append(" queued from ")
          .append(oldService).append(" to ").append(newService);
        return sb.toString();
    }

    // Simulate end of month: update burnout, apply service changes, update loyalty tiers
    public String simulateMonth() {
        int freelancerCount = allFreelancers.size();
        
        // First, collect all freelancers who want to change their service
        Freelancer[] serviceChangeFreelancers = new Freelancer[Math.min(16, freelancerCount)];
        int serviceChangeCount = 0;
        
        // Go through all freelancers and update their status
        Freelancer[] freelancerArray = allFreelancers.getArray();
        Freelancer[] serviceChangeArray = serviceChangeFreelancers;
        int serviceChangeCapacity = serviceChangeArray.length;
        for (int i = 0; i < freelancerCount; i++) {
            Freelancer f = freelancerArray[i];
            if (f == null) continue;
            
            // Update whether they're burned out or not
            f.updateBurnout();
            // Reset their monthly job counters
            f.resetMonth();
            
            // Update freelancer in max heap (burnout status may have changed)
            updateFreelancerInHeap(f, f.serviceType);
            
            // If they want to change service, remember them for later
            if (f.hasQueuedServiceChange()) {
                // Make array bigger if we need more space
                if (serviceChangeCount >= serviceChangeCapacity) {
                    serviceChangeCapacity = serviceChangeCapacity * 2;
                    Freelancer[] newArray = new Freelancer[serviceChangeCapacity];
                    System.arraycopy(serviceChangeArray, 0, newArray, 0, serviceChangeCount);
                    serviceChangeArray = newArray;
                    serviceChangeFreelancers = newArray;
                }
                serviceChangeArray[serviceChangeCount++] = f;
            }
        }
        
        // Now apply the service changes we collected
        Freelancer[] scArray = serviceChangeFreelancers;
        ServiceManager sm = serviceManager;
        HashTable si = serviceIndex;
        for (int i = 0; i < serviceChangeCount; i++) {
            Freelancer f = scArray[i];
            String oldServiceName = f.serviceType;  // Get current service
            String newServiceName = f.getQueuedService();  // Get queued service
            int newPrice = f.getQueuedPrice();  // Get queued price
            Service newService = sm.getService(newServiceName);
            if (newService != null) {
                // Get normalized service name
                String actualNewServiceName = newService.getName();
                // Apply service change to freelancer
                f.applyServiceChange(actualNewServiceName, newPrice);
                
                // Update service index: remove from old service, add to new service
                // Remove from old service max heap (O(log n))
                Service oldService = sm.getService(oldServiceName);
                if (oldService != null) {
                    String normalizedOldService = oldService.getName();
                    ServiceMaxHeap oldServiceHeap = (ServiceMaxHeap) si.get(normalizedOldService);
                    if (oldServiceHeap != null) {
                        oldServiceHeap.remove(f);
                    }
                }
                
                // Add to new service max heap (O(log n))
                ServiceMaxHeap newServiceHeap = (ServiceMaxHeap) si.get(actualNewServiceName);
                if (newServiceHeap == null) {
                    newServiceHeap = new ServiceMaxHeap(newService);
                    si.put(actualNewServiceName, newServiceHeap);
                }
                newServiceHeap.add(f);
            }
        }
        
        // Finally, update all customers' loyalty tiers based on how much they've spent
        int customerCount = allCustomers.size();
        Customer[] customerArray = allCustomers.getArray();
        for (int i = 0; i < customerCount; i++) {
            Customer c = customerArray[i];
            if (c != null) {
                c.updateLoyaltyTier();
            }
        }
        
        return "month complete";
    }

    // Query freelancer information (details, stats, status)
    public String queryFreelancer(String freelancerId) {
        // Get freelancer from hash table
        Freelancer freelancer = (Freelancer) freelancers.get(freelancerId);
        if (freelancer == null) {
            return "Some error occurred in query freelancer.";
        }

        // Build query response with all freelancer information
        StringBuilder sb = new StringBuilder(150);
        sb.append(freelancerId).append(": ").append(freelancer.serviceType)
          .append(", price: ").append(freelancer.price)
          .append(", rating: ").append(formatRating(freelancer.rating))
          .append(", completed: ").append(freelancer.completedJobs)
          .append(", cancelled: ").append(freelancer.cancelledJobs)
          .append(", skills: (").append(freelancer.T).append(",")
          .append(freelancer.C).append(",").append(freelancer.R)
          .append(",").append(freelancer.E).append(",").append(freelancer.A)
          .append("), available: ").append(freelancer.available ? "yes" : "no")
          .append(", burnout: ").append(freelancer.burnedOut ? "yes" : "no");
        return sb.toString();
    }

    // Query customer information (spending, tier, stats)
    public String queryCustomer(String customerId) {
        // Get customer from hash table
        Customer customer = (Customer) customers.get(customerId);
        if (customer == null) {
            return "Some error occurred in query customer.";
        }

        // Build query response with all customer information
        StringBuilder sb = new StringBuilder(120);
        sb.append(customerId).append(": total spent: $").append(customer.getTotalSpent())
          .append(", loyalty tier: ").append(customer.getLoyaltyTier())
          .append(", blacklisted freelancer count: ").append(customer.getBlacklistCount())
          .append(", total employment count: ").append(customer.getTotalEmploymentCount());
        return sb.toString();
    }

    // Update freelancer skills directly
    public String updateSkill(String freelancerId, int T, int C, int R, int E, int A) {
        // Validate all skills are in valid range (0-100)
        if (T < 0 || T > 100 || C < 0 || C > 100 || R < 0 || R > 100 || E < 0 || E > 100 || A < 0 || A > 100) {
            return "Some error occurred in update skill.";
        }
        
        // Get freelancer from hash table
        Freelancer freelancer = (Freelancer) freelancers.get(freelancerId);
        if (freelancer == null) {
            return "Some error occurred in update skill.";
        }

        // Get service type before updating
        String serviceType = freelancer.serviceType;
        // Update freelancer skills directly
        freelancer.updateSkills(T, C, R, E, A);
        // Update freelancer in max heap (composite score changed due to skill update)
        updateFreelancerInHeap(freelancer, serviceType);
        // Build response message
        StringBuilder sb = new StringBuilder(50);
        sb.append("updated skills of ").append(freelancerId).append(" for ").append(serviceType);
        return sb.toString();
    }

    // Helper method to calculate composite score (not used anymore, kept for reference)
    // The actual calculation is done directly in requestJob method
    private int calculateCompositeScore(Freelancer freelancer, Service service) {
        // Calculate skill score: dot product of freelancer and service skill profiles
        int[] fSkills = freelancer.getSkillProfile();
        int[] sSkills = service.getSkillProfile();
        double dotProduct = fSkills[0] * sSkills[0] + fSkills[1] * sSkills[1] + 
                           fSkills[2] * sSkills[2] + fSkills[3] * sSkills[3] + 
                           fSkills[4] * sSkills[4];
        int sumS = sSkills[0] + sSkills[1] + sSkills[2] + sSkills[3] + sSkills[4];
        double skillScore = dotProduct / (100.0 * sumS);

        // Calculate rating score: normalized to 0-1 range
        double ratingScore = freelancer.getRating() / 5.0;

        // Calculate reliability score: based on completion ratio
        int total = freelancer.getCompletedJobs() + freelancer.getCancelledJobs();
        double reliabilityScore = (total == 0) ? 1.0 : 
            (1.0 - (double)freelancer.getCancelledJobs() / total);

        // Apply burnout penalty: 45% reduction if burned out
        double burnoutPenalty = freelancer.isBurnedOut() ? 0.45 : 0.0;

        // Calculate composite: weighted combination (55% skill, 25% rating, 20% reliability, minus burnout)
        double composite = 10000.0 * (0.55 * skillScore + 0.25 * ratingScore + 0.20 * reliabilityScore - burnoutPenalty);
        return (int) Math.floor(composite);
    }

    // Quick sort method (not used anymore - we use MinHeap instead)
    // Sorts by score (highest first), then by ID (smallest first if scores are equal)
    private void quickSortWithScores(Freelancer[] arr, int[] scores, String[] ids, int low, int high) {
        if (low < high) {
            // Partition array around pivot
            int pi = partitionWithScores(arr, scores, ids, low, high);
            // Recursively sort left and right partitions
            quickSortWithScores(arr, scores, ids, low, pi - 1);
            quickSortWithScores(arr, scores, ids, pi + 1, high);
        }
    }

    // Partition function for quick sort: swaps elements based on score and ID
    private int partitionWithScores(Freelancer[] arr, int[] scores, String[] ids, int low, int high) {
        // Use last element as pivot
        int pivotScore = scores[high];
        String pivotId = ids[high];
        int i = low - 1;  // Index of smaller element
        
        for (int j = low; j < high; j++) {
            // If element is greater than pivot, or equal with smaller ID, swap
            if (scores[j] > pivotScore || (scores[j] == pivotScore && ids[j].compareTo(pivotId) < 0)) {
                i++;
                // Swap all three arrays in parallel
                Freelancer temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                int tempScore = scores[i];
                scores[i] = scores[j];
                scores[j] = tempScore;
                String tempId = ids[i];
                ids[i] = ids[j];
                ids[j] = tempId;
            }
        }
        
        // Swap pivot to correct position
        Freelancer temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        int tempScore = scores[i + 1];
        scores[i + 1] = scores[high];
        scores[high] = tempScore;
        String tempId = ids[i + 1];
        ids[i + 1] = ids[high];
        ids[high] = tempId;
        return i + 1;
    }

    // Quickselect method to find top K elements without sorting everything (not used anymore)
    // We use MinHeap instead
    private void quickSelectTopK(Freelancer[] arr, int[] scores, String[] ids, int low, int high, int k) {
        if (low < high) {
            // Partition array around pivot
            int pi = partitionWithScores(arr, scores, ids, low, high);
            if (pi == k - 1) {
                return;  // Found top K elements
            } else if (pi > k - 1) {
                // Top K is in left partition
                quickSelectTopK(arr, scores, ids, low, pi - 1, k);
            } else {
                // Top K is in right partition
                quickSelectTopK(arr, scores, ids, pi + 1, high, k);
            }
        }
    }
    
    // Format rating to show one decimal place (like 4.5 or 3.2)
    private String formatRating(double rating) {
        return String.format("%.1f", rating);
    }
    
    // Insertion sort method (not used anymore - we use MinHeap instead)
    // Sorts by score (highest first), then by ID (smallest first if scores are equal)
    private void insertionSortTopKParallel(Freelancer[] freelancers, int[] scores, String[] ids, int n) {
        for (int i = 1; i < n; i++) {
            // Get current element
            Freelancer keyF = freelancers[i];
            int keyScore = scores[i];
            String keyId = ids[i];
            int j = i - 1;
            
            // Shift elements greater than key to the right
            // Sort descending by score, then ascending by ID
            while (j >= 0) {
                int scoreDiff = scores[j] - keyScore;
                if (scoreDiff < 0 || (scoreDiff == 0 && ids[j].compareTo(keyId) > 0)) {
                    // Shift elements in all three arrays
                    freelancers[j + 1] = freelancers[j];
                    scores[j + 1] = scores[j];
                    ids[j + 1] = ids[j];
                    j--;
                } else {
                    break;
                }
            }
            // Insert key at correct position
            freelancers[j + 1] = keyF;
            scores[j + 1] = keyScore;
            ids[j + 1] = keyId;
        }
    }
    
    // Another insertion sort method (not used anymore - we use MinHeap instead)
    // Good for small lists but we don't use it
    private void insertionSortWithScores(Freelancer[] arr, int[] scores, String[] ids, int n) {
        for (int i = 1; i < n; i++) {
            // Get current element
            Freelancer keyF = arr[i];
            int keyScore = scores[i];
            String keyId = ids[i];
            int j = i - 1;
            
            // Shift elements while finding correct position
            while (j >= 0 && (scores[j] < keyScore || (scores[j] == keyScore && ids[j].compareTo(keyId) > 0))) {
                arr[j + 1] = arr[j];
                scores[j + 1] = scores[j];
                ids[j + 1] = ids[j];
                j--;
            }
            // Insert key at correct position
            arr[j + 1] = keyF;
            scores[j + 1] = keyScore;
            ids[j + 1] = keyId;
        }
    }

    // Ban a freelancer from the platform (remove them from everywhere)
    // This happens when they cancel too many jobs
    private void platformBanFreelancer(String freelancerId) {
        Freelancer freelancer = (Freelancer) freelancers.get(freelancerId);
        if (freelancer != null) {
            // Remove them from their service's max heap (O(log n))
            String serviceType = freelancer.serviceType;
            Service service = serviceManager.getService(serviceType);
            if (service != null) {
                String normalizedServiceName = service.getName();
                ServiceMaxHeap serviceHeap = (ServiceMaxHeap) serviceIndex.get(normalizedServiceName);
                if (serviceHeap != null) {
                    serviceHeap.remove(freelancer);
                }
            }
        }
        
        // Remove from main freelancer storage
        freelancers.remove(freelancerId);
        // Remove from the list too (O(n) linear search - could be optimized but less critical)
        for (int i = 0; i < allFreelancers.size(); i++) {
            Freelancer f = allFreelancers.get(i);
            if (f != null && f.id.equals(freelancerId)) {
                allFreelancers.remove(i);
                break;
            }
        }
    }
    
    // Helper method to update a freelancer in their service's max heap
    // Called when freelancer data changes (rating, skills, burnout, availability)
    // Updates the freelancer's position in the heap based on new composite score (O(log n))
    private void updateFreelancerInHeap(Freelancer freelancer, String serviceType) {
        if (freelancer == null || serviceType == null) {
            return;
        }
        
        Service service = serviceManager.getService(serviceType);
        if (service != null) {
            String normalizedServiceName = service.getName();
            ServiceMaxHeap serviceHeap = (ServiceMaxHeap) serviceIndex.get(normalizedServiceName);
            if (serviceHeap != null) {
                serviceHeap.updateFreelancer(freelancer);
            }
        }
    }
}

