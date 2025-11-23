import java.util.ArrayList;

/**
 * Max-heap to store freelancers sorted by composite score.
 * Highest scores are at the root, allowing O(1) access to best freelancers.
 * Uses ArrayList and HashTable for O(1) lookup and O(log n) updates.
 */
public class MaxHeap {

    private ArrayList<Freelancer> heapList;
    private HashTable indexMap;

    public MaxHeap() {
        this.heapList = new ArrayList<>(1000);  // OPTIMIZATION: Initial capacity
        this.indexMap = new HashTable(1000);
    }

    public boolean isEmpty() {
        return heapList.isEmpty();
    }

    public void clear() {
        heapList.clear();
        indexMap = new HashTable(1000);
    }

    public Freelancer peekMax() {
        if (heapList.isEmpty()) return null;
        return heapList.get(0);
    }

    // -------------------------------------------------------------------
    //  INSERT
    // -------------------------------------------------------------------
    public void insert(Freelancer f) {
        heapList.add(f);
        int index = heapList.size() - 1;
        indexMap.put(f.getID(), Integer.valueOf(index));
        siftUp(index);
    }

    // -------------------------------------------------------------------
    //  EXTRACT MAX
    // -------------------------------------------------------------------
    public Freelancer extractMax() {
        if (heapList.isEmpty()) return null;

        Freelancer max = heapList.get(0);
        Freelancer last = heapList.remove(heapList.size() - 1);
        indexMap.remove(max.getID());

        if (!heapList.isEmpty()) {
            heapList.set(0, last);
            indexMap.put(last.getID(), Integer.valueOf(0));
            siftDown(0);
        }

        return max;
    }

    // -------------------------------------------------------------------
    //  UPDATE SPECIFIC FREELANCER
    // -------------------------------------------------------------------
    public void updateFreelancer(String freelancerID) {
        Integer indexObj = (Integer) indexMap.get(freelancerID);
        if (indexObj == null) {
            return; // freelancer not in this heap
        }
        int index = indexObj.intValue();

        // May have changed position, so restore heap property
        siftUp(index);
        siftDown(index);
    }

    // -------------------------------------------------------------------
    //  REMOVE BY ID
    // -------------------------------------------------------------------
    public boolean removeById(String freelancerID) {
        Integer indexObj = (Integer) indexMap.get(freelancerID);
        if (indexObj == null) {
            return false; // freelancer not in this heap
        }
        int index = indexObj.intValue();

        // Remove from index map
        indexMap.remove(freelancerID);

        // If it's the last element, just remove it
        if (index == heapList.size() - 1) {
            heapList.remove(index);
            return true;
        }

        // Replace with last element and restore heap property
        Freelancer last = heapList.remove(heapList.size() - 1);
        if (index < heapList.size()) {
            heapList.set(index, last);
            indexMap.put(last.getID(), Integer.valueOf(index));
            // Restore heap property
            siftUp(index);
            siftDown(index);
        }

        return true;
    }

    // -------------------------------------------------------------------
    //  CHECK IF CONTAINS
    // -------------------------------------------------------------------
    public boolean contains(String freelancerID) {
        return indexMap.containsKey(freelancerID);
    }

    // -------------------------------------------------------------------
    //  SIFT UP
    // -------------------------------------------------------------------
    private void siftUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            if (isBetterThan(index, parentIndex)) {
                swap(index, parentIndex);
                index = parentIndex;
            } else {
                break;
            }
        }
    }

    // -------------------------------------------------------------------
    //  SIFT DOWN - OPTIMIZED with cached references
    // -------------------------------------------------------------------
    private void siftDown(int index) {
        int size = heapList.size();  // OPTIMIZATION: Cache size
        while (true) {
            int leftChild = 2 * index + 1;
            if (leftChild >= size) break;
            
            int rightChild = leftChild + 1;
            int bestChild = leftChild;

            if (rightChild < size && isBetterThan(rightChild, leftChild)) {
                bestChild = rightChild;
            }

            if (isBetterThan(bestChild, index)) {
                swap(index, bestChild);
                index = bestChild;
            } else {
                break;
            }
        }
    }

    // -------------------------------------------------------------------
    //  SWAP + indexMap UPDATE
    // -------------------------------------------------------------------
    private void swap(int i, int j) {
        Freelancer fi = heapList.get(i);
        Freelancer fj = heapList.get(j);

        heapList.set(i, fj);
        heapList.set(j, fi);

        indexMap.put(fi.getID(), Integer.valueOf(j));
        indexMap.put(fj.getID(), Integer.valueOf(i));
    }

    // -------------------------------------------------------------------
    //  COMPARISON
    // -------------------------------------------------------------------
    private boolean isBetterThan(int i, int j) {
        Freelancer a = heapList.get(i);
        Freelancer b = heapList.get(j);

        int sa = a.getCompositeScore();
        int sb = b.getCompositeScore();

        if (sa != sb) return sa > sb;

        // compositeScore equal, smaller ID is better
        return a.getID().compareTo(b.getID()) < 0;
    }

    // -------------------------------------------------------------------
    //  SIZE
    // -------------------------------------------------------------------
    public int size() {
        return heapList.size();
    }
}
