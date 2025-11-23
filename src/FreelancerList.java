/**
 * List to store freelancers.
 * Grows automatically when it gets full.
 */
class FreelancerList {
    private Freelancer[] array;  // The actual array storing freelancers
    private int size;  // How many freelancers are currently in the list
    private static final int INITIAL_CAPACITY = 16;  // Start with space for 16 freelancers

    // Create a new empty list
    public FreelancerList() {
        array = new Freelancer[INITIAL_CAPACITY];
        size = 0;
    }
    
    // Make sure the list has at least 'capacity' spaces
    public void ensureCapacity(int capacity) {
        if (capacity > array.length) {
            Freelancer[] newArray = new Freelancer[capacity];
            System.arraycopy(array, 0, newArray, 0, size);  // Copy existing elements
            array = newArray;
        }
    }

    // Add a freelancer to the end of the list
    public void add(Freelancer freelancer) {
        // If array is full, make it bigger
        if (size >= array.length) {
            resize();
        }
        array[size++] = freelancer;
    }

    // Get the freelancer at a specific index
    public Freelancer get(int index) {
        if (index < 0 || index >= size) {
            return null;  // Invalid index
        }
        return array[index];
    }

    // Get how many freelancers are in the list
    public int size() {
        return size;
    }

    // Remove the freelancer at a specific index
    // Shifts all freelancers after it one position to the left
    public void remove(int index) {
        if (index < 0 || index >= size) {
            return;  // Invalid index
        }
        // Shift everything after this index one position left
        for (int i = index; i < size - 1; i++) {
            array[i] = array[i + 1];
        }
        size--;
    }

    // Remove a specific freelancer from the list
    // Finds it and removes it, shifting others to fill the gap
    public void remove(Freelancer freelancer) {
        Freelancer[] a = array;
        int s = size;
        for (int i = 0; i < s; i++) {
            if (a[i] == freelancer) {
                // Found it, shift everything after it left
                for (int j = i; j < s - 1; j++) {
                    a[j] = a[j + 1];
                }
                size--;
                return;
            }
        }
    }

    // Create a new array with all freelancers in this list
    public Freelancer[] toArray() {
        Freelancer[] result = new Freelancer[size];
        System.arraycopy(array, 0, result, 0, size);
        return result;
    }
    
    // Get the internal array directly
    // Useful when we need to iterate through all freelancers quickly
    public Freelancer[] getArray() {
        return array;
    }
    
    // Get the current size
    public int getSize() {
        return size;
    }

    // Make the array twice as big
    // Copies all existing freelancers to the new array
    private void resize() {
        Freelancer[] newArray = new Freelancer[array.length * 2];
        System.arraycopy(array, 0, newArray, 0, size);
        array = newArray;
    }
}
