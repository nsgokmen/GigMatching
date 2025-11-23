/**
 * Hash table to store key-value pairs.
 * Uses chaining to handle collisions (multiple keys that hash to same bucket).
 */
class HashTable {
    // Each bucket is a linked list of entries
    private static class Entry {
        String key;
        Object value;
        Entry next;  // Next entry in the chain

        Entry(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    private Entry[] buckets;  // Array of linked lists
    private int size;  // Number of key-value pairs stored
    private static final int INITIAL_CAPACITY = 16;  // Start with 16 buckets
    private static final double LOAD_FACTOR = 0.75;  // Resize when 75% full

    // Create hash table with given initial capacity
    // Capacity is rounded up to next power of 2 (makes some operations faster)
    public HashTable(int initialCapacity) {
        int capacity = 1;
        while (capacity < initialCapacity) {
            capacity = capacity * 2;  // Keep doubling until we reach or exceed initialCapacity
        }
        buckets = new Entry[capacity];
        size = 0;
    }

    // Create hash table with default capacity
    public HashTable() {
        this(INITIAL_CAPACITY);
    }

    // Store a key-value pair
    // If key already exists, update the value
    public void put(String key, Object value) {
        Entry[] b = buckets;
        // If table is getting too full, make it bigger
        if (size >= b.length * LOAD_FACTOR) {
            resize();
            b = buckets;  // Get new buckets after resize
        }
        
        // Figure out which bucket this key should go in
        int mask = b.length - 1;  // Since length is power of 2, this works as modulo
        int h = key.hashCode();  // Get hash code of the key
        int index = (h & 0x7FFFFFFF) & mask;  // Make sure it's positive and fits in buckets
        
        Entry entry = b[index];
        
        // Check if bucket is empty
        if (entry == null) {
            b[index] = new Entry(key, value);
            size++;
            return;
        }
        
        // Check first entry in chain
        if (entry.key == key || entry.key.equals(key)) {
            entry.value = value;  // Update existing key
            return;
        }
        
        // Walk through the chain to find the key or end
        Entry prev = entry;
        entry = entry.next;
        while (entry != null) {
            if (entry.key == key || entry.key.equals(key)) {
                entry.value = value;  // Update existing key
                return;
            }
            prev = entry;
            entry = entry.next;
        }
        
        // Key not found, add new entry at end of chain
        prev.next = new Entry(key, value);
        size++;
    }

    // Get the value for a key, or null if key doesn't exist
    // Unrolls first few iterations for common case (most chains are short)
    public Object get(String key) {
        Entry[] b = buckets;
        int mask = b.length - 1;
        int h = key.hashCode();
        int index = (h & 0x7FFFFFFF) & mask;
        Entry e = b[index];
        
        // Check first entry
        if (e == null) return null;
        String k = e.key;
        if (k == key || k.equals(key)) return e.value;
        
        // Check second entry
        e = e.next;
        if (e == null) return null;
        k = e.key;
        if (k == key || k.equals(key)) return e.value;
        
        // Check third entry
        e = e.next;
        if (e == null) return null;
        k = e.key;
        if (k == key || k.equals(key)) return e.value;
        
        // Continue for longer chains (rare case)
        e = e.next;
        while (e != null) {
            k = e.key;
            if (k == key || k.equals(key)) return e.value;
            e = e.next;
        }
        return null;
    }

    // Check if a key exists in the table
    // Same logic as get() but returns boolean
    public boolean containsKey(String key) {
        Entry[] b = buckets;
        int mask = b.length - 1;
        int h = key.hashCode();
        int index = (h & 0x7FFFFFFF) & mask;
        Entry e = b[index];
        
        // Check first entry
        if (e == null) return false;
        String k = e.key;
        if (k == key || k.equals(key)) return true;
        
        // Check second entry
        e = e.next;
        if (e == null) return false;
        k = e.key;
        if (k == key || k.equals(key)) return true;
        
        // Check third entry
        e = e.next;
        if (e == null) return false;
        k = e.key;
        if (k == key || k.equals(key)) return true;
        
        // Continue for longer chains (rare case)
        e = e.next;
        while (e != null) {
            k = e.key;
            if (k == key || k.equals(key)) return true;
            e = e.next;
        }
        return false;
    }

    // Remove a key-value pair
    public void remove(String key) {
        int mask = buckets.length - 1;
        int h = key.hashCode();
        int index = (h & 0x7FFFFFFF) & mask;
        Entry entry = buckets[index];
        
        if (entry == null) return;
        
        // If it's the first entry in chain, just remove it
        if (entry.key == key || entry.key.equals(key)) {
            buckets[index] = entry.next;
            size--;
            return;
        }
        
        // Walk through chain to find the entry
        Entry prev = entry;
        entry = entry.next;
        while (entry != null) {
            if (entry.key == key || entry.key.equals(key)) {
                prev.next = entry.next;  // Skip this entry
                size--;
                return;
            }
            prev = entry;
            entry = entry.next;
        }
    }

    // Get number of key-value pairs stored
    public int size() {
        return size;
    }

    // Make the hash table bigger when it gets too full
    // Rehashes all entries into new buckets
    private void resize() {
        Entry[] oldBuckets = buckets;
        int newCapacity = oldBuckets.length * 2;  // Double the size
        Entry[] newBuckets = new Entry[newCapacity];
        int mask = newCapacity - 1;
        
        // Go through all old buckets
        for (Entry entry : oldBuckets) {
            // Go through all entries in this bucket's chain
            while (entry != null) {
                Entry next = entry.next;  // Remember next before we modify entry
                
                // Figure out which new bucket this entry should go in
                int newIndex = (entry.key.hashCode() & 0x7FFFFFFF) & mask;
                
                // Add to front of new bucket's chain
                entry.next = newBuckets[newIndex];
                newBuckets[newIndex] = entry;
                
                // Move to next entry in chain
                entry = next;
            }
        }
        
        buckets = newBuckets;
    }
}
