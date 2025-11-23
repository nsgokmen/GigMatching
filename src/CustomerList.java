/**
 * List to store customers.
 * Grows automatically when it gets full.
 */
class CustomerList {
    private Customer[] customers;  // The actual array storing customers
    private int size;  // How many customers are currently in the list
    private static final int INITIAL_CAPACITY = 1000;  // Start with space for 1000 customers

    // Create a new empty list
    public CustomerList() {
        customers = new Customer[INITIAL_CAPACITY];
        size = 0;
    }

    // Add a customer to the end of the list
    public void add(Customer customer) {
        // If array is full, make it bigger
        if (size >= customers.length) {
            resize();
        }
        customers[size++] = customer;
    }

    // Get how many customers are in the list
    public int size() {
        return size;
    }

    // Get the customer at a specific index
    public Customer get(int index) {
        if (index < 0 || index >= size) {
            return null;  // Invalid index
        }
        return customers[index];
    }

    // Get the internal array directly
    // Useful when we need to iterate through all customers quickly
    public Customer[] getArray() {
        return customers;
    }

    // Get the current size
    public int getSize() {
        return size;
    }

    // Make the array twice as big
    // Copies all existing customers to the new array
    private void resize() {
        Customer[] newCustomers = new Customer[customers.length * 2];
        System.arraycopy(customers, 0, newCustomers, 0, size);
        customers = newCustomers;
    }
}
