/**
 * AVL Tree for storing freelancers sorted by ID.
 * Provides O(log n) insertion, deletion, and search operations.
 * Also supports iteration through all freelancers.
 */
class FreelancerAVLTree {
    private Node root;
    private int size;
    
    // AVL Tree Node
    private static class Node {
        Freelancer freelancer;
        Node left;
        Node right;
        int height;
        
        Node(Freelancer freelancer) {
            this.freelancer = freelancer;
            this.height = 1;
        }
    }
    
    // Create an empty AVL tree
    public FreelancerAVLTree() {
        root = null;
        size = 0;
    }
    
    // Get the number of freelancers in the tree
    public int size() {
        return size;
    }
    
    // Check if tree is empty
    public boolean isEmpty() {
        return root == null;
    }
    
    // Get height of a node
    private int height(Node node) {
        return node == null ? 0 : node.height;
    }
    
    // Get balance factor of a node
    private int getBalance(Node node) {
        return node == null ? 0 : height(node.left) - height(node.right);
    }
    
    // Right rotate subtree rooted with y
    private Node rightRotate(Node y) {
        Node x = y.left;
        Node T2 = x.right;
        
        // Perform rotation
        x.right = y;
        y.left = T2;
        
        // Update heights
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        
        return x;
    }
    
    // Left rotate subtree rooted with x
    private Node leftRotate(Node x) {
        Node y = x.right;
        Node T2 = y.left;
        
        // Perform rotation
        y.left = x;
        x.right = T2;
        
        // Update heights
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        
        return y;
    }
    
    // Insert a freelancer into the tree (sorted by ID)
    public void add(Freelancer freelancer) {
        root = insert(root, freelancer);
        size++;
    }
    
    // Recursive insert function
    private Node insert(Node node, Freelancer freelancer) {
        // Perform normal BST insertion
        if (node == null) {
            return new Node(freelancer);
        }
        
        int cmp = freelancer.id.compareTo(node.freelancer.id);
        if (cmp < 0) {
            node.left = insert(node.left, freelancer);
        } else if (cmp > 0) {
            node.right = insert(node.right, freelancer);
        } else {
            // ID already exists, replace the freelancer
            node.freelancer = freelancer;
            size--; // Don't count duplicate as new addition
            return node;
        }
        
        // Update height of this ancestor node
        node.height = 1 + Math.max(height(node.left), height(node.right));
        
        // Get balance factor
        int balance = getBalance(node);
        
        // Left Left Case
        if (balance > 1 && freelancer.id.compareTo(node.left.freelancer.id) < 0) {
            return rightRotate(node);
        }
        
        // Right Right Case
        if (balance < -1 && freelancer.id.compareTo(node.right.freelancer.id) > 0) {
            return leftRotate(node);
        }
        
        // Left Right Case
        if (balance > 1 && freelancer.id.compareTo(node.left.freelancer.id) > 0) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }
        
        // Right Left Case
        if (balance < -1 && freelancer.id.compareTo(node.right.freelancer.id) < 0) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }
        
        return node;
    }
    
    // Remove a freelancer from the tree
    public boolean remove(Freelancer freelancer) {
        int oldSize = size;
        root = deleteNode(root, freelancer.id);
        return size < oldSize;
    }
    
    // Remove a freelancer by ID
    public boolean removeById(String freelancerId) {
        int oldSize = size;
        root = deleteNode(root, freelancerId);
        return size < oldSize;
    }
    
    // Recursive delete function
    private Node deleteNode(Node root, String freelancerId) {
        // Perform standard BST delete
        if (root == null) {
            return root;
        }
        
        int cmp = freelancerId.compareTo(root.freelancer.id);
        if (cmp < 0) {
            root.left = deleteNode(root.left, freelancerId);
        } else if (cmp > 0) {
            root.right = deleteNode(root.right, freelancerId);
        } else {
            // This is the node to be deleted
            size--;
            
            // Node with only one child or no child
            if (root.left == null || root.right == null) {
                Node temp = (root.left != null) ? root.left : root.right;
                
                // No child case
                if (temp == null) {
                    temp = root;
                    root = null;
                } else {
                    // One child case
                    root = temp;
                }
            } else {
                // Node with two children: get inorder successor
                Node temp = minValueNode(root.right);
                
                // Copy the inorder successor's data to this node
                root.freelancer = temp.freelancer;
                
                // Delete the inorder successor
                root.right = deleteNode(root.right, temp.freelancer.id);
            }
        }
        
        // If tree had only one node, return
        if (root == null) {
            return root;
        }
        
        // Update height
        root.height = Math.max(height(root.left), height(root.right)) + 1;
        
        // Get balance factor
        int balance = getBalance(root);
        
        // Left Left Case
        if (balance > 1 && getBalance(root.left) >= 0) {
            return rightRotate(root);
        }
        
        // Left Right Case
        if (balance > 1 && getBalance(root.left) < 0) {
            root.left = leftRotate(root.left);
            return rightRotate(root);
        }
        
        // Right Right Case
        if (balance < -1 && getBalance(root.right) <= 0) {
            return leftRotate(root);
        }
        
        // Right Left Case
        if (balance < -1 && getBalance(root.right) > 0) {
            root.right = rightRotate(root.right);
            return leftRotate(root);
        }
        
        return root;
    }
    
    // Find the node with minimum value in a subtree
    private Node minValueNode(Node node) {
        Node current = node;
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }
    
    // Search for a freelancer by ID
    public Freelancer findById(String freelancerId) {
        Node node = findNode(root, freelancerId);
        return node == null ? null : node.freelancer;
    }
    
    // Check if a freelancer exists in the tree
    public boolean contains(Freelancer freelancer) {
        return findById(freelancer.id) != null;
    }
    
    // Check if a freelancer ID exists in the tree
    public boolean containsById(String freelancerId) {
        return findById(freelancerId) != null;
    }
    
    // Recursive find function
    private Node findNode(Node node, String freelancerId) {
        if (node == null) {
            return null;
        }
        
        int cmp = freelancerId.compareTo(node.freelancer.id);
        if (cmp < 0) {
            return findNode(node.left, freelancerId);
        } else if (cmp > 0) {
            return findNode(node.right, freelancerId);
        } else {
            return node;
        }
    }
    
    // Get all freelancers as an array (in-order traversal)
    public Freelancer[] toArray() {
        Freelancer[] result = new Freelancer[size];
        int[] index = {0};
        inOrderTraversal(root, result, index);
        return result;
    }
    
    // Get all freelancers as an array with capacity (for compatibility)
    public Freelancer[] getArray() {
        // For compatibility with FreelancerList interface
        // Returns array with size capacity (may contain nulls)
        Freelancer[] result = new Freelancer[size];
        int[] index = {0};
        inOrderTraversal(root, result, index);
        return result;
    }
    
    // Get the size for getSize() method (compatibility)
    public int getSize() {
        return size;
    }
    
    // In-order traversal to fill array
    private void inOrderTraversal(Node node, Freelancer[] array, int[] index) {
        if (node != null) {
            inOrderTraversal(node.left, array, index);
            if (index[0] < array.length) {
                array[index[0]++] = node.freelancer;
            }
            inOrderTraversal(node.right, array, index);
        }
    }
    
}

