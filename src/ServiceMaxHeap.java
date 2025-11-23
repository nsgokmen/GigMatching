import java.util.ArrayList;

/**
 * Max heap for a service type that maintains freelancers sorted by composite score.
 * Composite score is calculated based on service requirements and freelancer data.
 * OPTIMIZED: Service skills cached, redundant checks removed.
 */
class ServiceMaxHeap {
    private MaxHeap heap;
    private Service service;
    private HashTable freelancerMap;
    
    // OPTIMIZATION #1: Cache service skill requirements (calculated once, not every time!)
    private final int sT, sC, sR, sE, sA;
    private final double sumSDivisor;
    
    // Create a max heap for a service
    public ServiceMaxHeap(Service service) {
        this.service = service;
        this.heap = new MaxHeap();
        this.freelancerMap = new HashTable(1000);
        
        // OPTIMIZATION: Cache service skills to avoid repeated getSkillProfile() calls
        // This is called thousands of times in calculateCompositeScore!
        int[] sSkills = service.getSkillProfile();
        this.sT = sSkills[0];
        this.sC = sSkills[1];
        this.sR = sSkills[2];
        this.sE = sSkills[3];
        this.sA = sSkills[4];
        int sumS = sT + sC + sR + sE + sA;
        this.sumSDivisor = 100.0 * sumS;
    }
    
    // Calculate composite score for a freelancer based on service requirements
    private int calculateCompositeScore(Freelancer freelancer) {
        // OPTIMIZATION: Use cached service skills directly (no method call!)
        int fT = freelancer.T, fC = freelancer.C, fR = freelancer.R, 
            fE = freelancer.E, fA = freelancer.A;
        
        // Calculate skill score: dot product of freelancer and service skills
        long dotProductLong = (long)fT * sT + (long)fC * sC + (long)fR * sR + 
                              (long)fE * sE + (long)fA * sA;
        double skillScore = dotProductLong / sumSDivisor;
        
        // Calculate rating score: normalize 0-5 to 0-1
        double rating = freelancer.rating;
        double ratingScore = rating / 5.0;
        
        // Calculate reliability score: based on completion rate
        int completed = freelancer.completedJobs;
        int cancelled = freelancer.cancelledJobs;
        int total = completed + cancelled;
        double reliabilityScore = (total == 0) ? 1.0 : (1.0 - ((double)cancelled / total));
        
        // Calculate burnout penalty: 45% reduction if burned out
        double burnoutPenalty = freelancer.burnedOut ? 0.45 : 0.0;
        
        // Calculate composite: 55% skills, 25% rating, 20% reliability, minus burnout
        double composite = 10000.0 * (0.55 * skillScore + 0.25 * ratingScore + 
                                      0.20 * reliabilityScore - burnoutPenalty);
        return (int) Math.floor(composite);
    }
    
    // Add a freelancer to the heap (O(log n))
    public void add(Freelancer freelancer) {
        if (freelancer == null || !freelancer.available || freelancer.isEmployed) {
            return;  // Don't add unavailable or employed freelancers
        }
        
        // Check if already exists - update instead of adding duplicate
        if (freelancerMap.containsKey(freelancer.id)) {
            updateFreelancer(freelancer);
            return;
        }
        
        int score = calculateCompositeScore(freelancer);
        freelancer.setCompositeScore(score);
        heap.insert(freelancer);
        freelancerMap.put(freelancer.id, freelancer);
    }
    
    // Update a freelancer's score (when their data changes)
    public void updateFreelancer(Freelancer freelancer) {
        if (freelancer == null) {
            return;
        }
        
        // Check if freelancer is already in heap using O(1) lookup
        Freelancer existingEntry = (Freelancer) freelancerMap.get(freelancer.id);
        
        if (existingEntry != null) {
            // Freelancer is in heap - update score if still available, otherwise remove
            if (freelancer.available && !freelancer.isEmployed) {
                // Recalculate score and update in place (O(log n))
                int newScore = calculateCompositeScore(freelancer);
                freelancer.setCompositeScore(newScore);
                heap.updateFreelancer(freelancer.id);
                // Update the entry in the map
                freelancerMap.put(freelancer.id, freelancer);
            } else {
                // Remove from heap (O(log n))
                removeById(freelancer.id);
            }
        } else {
            // Not in heap, add if available
            if (freelancer.available && !freelancer.isEmployed) {
                add(freelancer);
            }
        }
    }
    
    // Remove a freelancer from the heap (O(log n))
    public void remove(Freelancer freelancer) {
        if (freelancer != null) {
            removeById(freelancer.id);
        }
    }
    
    // Remove a freelancer by ID
    public void removeById(String freelancerId) {
        if (heap.removeById(freelancerId)) {
            freelancerMap.remove(freelancerId);
        }
    }
    
    // Get top K freelancers (available and not employed, not blacklisted)
    // Extract, filter, sort, then re-insert
    public ArrayList<Freelancer> getTopK(int k, HashTable blacklist) {
        ArrayList<Freelancer> result = new ArrayList<>(k);
        
        if (heap.isEmpty() || k <= 0) {
            return result;
        }
        
        ArrayList<Freelancer> extracted = new ArrayList<>(k * 2);
        ArrayList<Freelancer> valid = new ArrayList<>(k);
        
        // Step 1: Extract freelancers from heap
        while (!heap.isEmpty() && valid.size() < k) {
            Freelancer f = heap.extractMax();
            if (f == null) break;
            
            extracted.add(f);
            
            // OPTIMIZATION #2: Removed redundant freelancerMap.containsKey check
            // If it came from heap, it's already in our service
            if (f.available && !f.isEmployed) {
                if (blacklist == null || !blacklist.containsKey(f.id)) {
                    valid.add(f);
                }
            }
        }
        
        // Step 2: Sort valid freelancers by score (descending) then ID (ascending)
        // OPTIMIZATION #3: Skip sort if only 0-1 items
        if (valid.size() > 1) {
            sortFreelancersByScore(valid);
        }
        
        // Step 3: Re-insert all extracted freelancers back to heap
        for (int i = 0; i < extracted.size(); i++) {
            Freelancer f = extracted.get(i);
            if (freelancerMap.containsKey(f.id)) {  // Only if still in service
                heap.insert(f);
            }
        }
        
        // Step 4: Return valid freelancers
        return valid;
    }
    
    // Sort freelancers by composite score (descending), then by ID (ascending)
    private void sortFreelancersByScore(ArrayList<Freelancer> list) {
        int n = list.size();
        // Insertion sort - simple and efficient for small K
        for (int i = 1; i < n; i++) {
            Freelancer key = list.get(i);
            int keyScore = key.getCompositeScore();
            String keyId = key.getID();
            int j = i - 1;
            
            // Move elements that should come AFTER key to the right
            while (j >= 0) {
                Freelancer curr = list.get(j);
                int scoreDiff = curr.getCompositeScore() - keyScore;
                
                // Move to right if: lower score OR (same score AND larger ID)
                if (scoreDiff < 0 || (scoreDiff == 0 && curr.getID().compareTo(keyId) > 0)) {
                    list.set(j + 1, list.get(j));
                    j--;
                } else {
                    break;
                }
            }
            list.set(j + 1, key);
        }
    }
    
    // Get size of heap
    public int size() {
        return heap.size();
    }
    
    // Check if heap is empty
    public boolean isEmpty() {
        return heap.isEmpty();
    }
}
