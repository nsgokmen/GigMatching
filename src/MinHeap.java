/**
 * Min-heap to keep track of the top K freelancers.
 * It keeps the worst ones at the top, so we can easily remove them.
 */
class MinHeap {
    private FreelancerScore[] heap;
    private int size;
    private int capacity;
    
    // Stores a freelancer along with their score
    static class FreelancerScore {
        Freelancer freelancer;  // The freelancer object
        int score;              // Their composite score
        String id;              // Their ID (used for tie-breaking)
        
        FreelancerScore(Freelancer f, int s, String id) {
            this.freelancer = f;
            this.score = s;
            this.id = id;
        }
    }
    
    // Create a min-heap that can hold up to 'capacity' freelancers
    public MinHeap(int capacity) {
        this.capacity = capacity;
        this.heap = new FreelancerScore[capacity + 1];  // +1 because we start at index 1
        this.size = 0;
    }
    
    // Add a freelancer to the heap
    // If heap is full and new one is better, replace the worst one
    public void add(FreelancerScore item) {
        if (size < capacity) {
            // Heap still has space, just add it
            heap[++size] = item;
            bubbleUp(size);  // Move it up if needed
        } else {
            // Heap is full, check if this one is better than the worst one
            FreelancerScore min = heap[1];  // Worst one is always at the top
            if (item.score > min.score) {
                // This one is better, replace the worst one
                heap[1] = item;
                bubbleDown(1);  // Move it down to correct position
            } else if (item.score == min.score) {
                // Same score, check ID (prefer smaller ID)
                if (item.id.compareTo(min.id) < 0) {
                    heap[1] = item;
                    bubbleDown(1);
                }
            }
        }
    }
    
    // Move an element up in the heap (towards the root)
    // The root has the worst (minimum) score
    private void bubbleUp(int index) {
        FreelancerScore[] h = heap;
        while (index > 1) {
            int parent = index / 2;  // Parent is at index / 2
            FreelancerScore current = h[index];
            FreelancerScore parentItem = h[parent];
            
            // Compare scores (smaller is worse, should be closer to root)
            // If scores are equal, larger ID is worse
            int scoreDiff = current.score - parentItem.score;
            if (scoreDiff > 0 || (scoreDiff == 0 && current.id.compareTo(parentItem.id) > 0)) {
                // Current is better than parent, we're done
                break;
            }
            
            // Swap with parent
            FreelancerScore temp = h[index];
            h[index] = h[parent];
            h[parent] = temp;
            index = parent;
        }
    }
    
    // Move an element down in the heap (towards the leaves)
    // Fix the heap property after replacing the root
    private void bubbleDown(int index) {
        FreelancerScore[] h = heap;
        int s = size;
        while (index * 2 <= s) {  // While we have at least one child
            int left = index * 2;
            int right = left + 1;
            int smallest = index;
            FreelancerScore smallestItem = h[smallest];
            
            // Check left child
            FreelancerScore leftItem = h[left];
            int leftDiff = leftItem.score - smallestItem.score;
            // If left child is worse (smaller score or same score with larger ID), it should be at root
            if (leftDiff < 0 || (leftDiff == 0 && leftItem.id.compareTo(smallestItem.id) > 0)) {
                smallest = left;
                smallestItem = leftItem;
            }
            
            // Check right child if it exists
            if (right <= s) {
                FreelancerScore rightItem = h[right];
                int rightDiff = rightItem.score - smallestItem.score;
                if (rightDiff < 0 || (rightDiff == 0 && rightItem.id.compareTo(smallestItem.id) > 0)) {
                    smallest = right;
                }
            }
            
            // If we found a worse child, swap with it
            if (smallest == index) break;
            
            FreelancerScore temp = h[index];
            h[index] = h[smallest];
            h[smallest] = temp;
            index = smallest;
        }
    }
    
    // Helper method to swap two elements (not used anymore, kept for reference)
    private void swap(int i, int j) {
        FreelancerScore temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }
    
    // Get all freelancers sorted from best to worst
    // Returns them in descending order (best first)
    public FreelancerScore[] getSorted() {
        // Copy the heap elements (skip index 0)
        FreelancerScore[] result = new FreelancerScore[size];
        System.arraycopy(heap, 1, result, 0, size);
        // Sort them from best to worst
        insertionSortDesc(result);
        return result;
    }
    
    // Sort the array from best to worst using insertion sort
    // Best means highest score, or same score with smaller ID
    private void insertionSortDesc(FreelancerScore[] arr) {
        int len = arr.length;
        FreelancerScore[] a = arr;
        for (int i = 1; i < len; i++) {
            FreelancerScore key = a[i];
            int keyScore = key.score;
            String keyId = key.id;
            int j = i - 1;
            
            // Move elements that are worse than key to the right
            while (j >= 0) {
                FreelancerScore arrj = a[j];
                int scoreDiff = arrj.score - keyScore;
                // Worse means smaller score, or same score with larger ID
                if (scoreDiff < 0 || (scoreDiff == 0 && arrj.id.compareTo(keyId) > 0)) {
                    a[j + 1] = arrj;
                    j--;
                } else {
                    break;
                }
            }
            a[j + 1] = key;
        }
    }
    
    // Check how many freelancers are in the heap
    public int size() {
        return size;
    }
    
    // Check if heap is full (has K freelancers already)
    public boolean isFull() {
        return size >= capacity;
    }
    
    // Get the worst score currently in the heap
    // Used to skip freelancers that are definitely not in top K
    public int getMinScore() {
        return size > 0 ? heap[1].score : Integer.MIN_VALUE;
    }
    
    // Get the ID of the worst freelancer currently in the heap
    // Used for tie-breaking when scores are equal
    public String getMinId() {
        return size > 0 ? heap[1].id : null;
    }
    
    // Peek at the worst (minimum) freelancer without removing
    public FreelancerScore peek() {
        return size > 0 ? heap[1] : null;
    }
    
    // Remove and return the worst (minimum) freelancer
    public FreelancerScore removeMin() {
        if (size == 0) {
            return null;
        }
        
        FreelancerScore min = heap[1];
        if (size == 1) {
            size = 0;
            heap[1] = null;
            return min;
        }
        
        heap[1] = heap[size];
        heap[size] = null;
        size--;
        bubbleDown(1);
        return min;
    }
}
