/**
 * AVL Tree for storing freelancers sorted by composite score (descending).
 * Allows efficient top-K extraction without linear search.
 * Composite score is calculated based on service requirements and freelancer data.
 */
class ServiceAVLTree {
    private Node root;
    private int size;
    private Service service;  // Service requirements for calculating composite score
    
    // AVL Tree Node
    private static class Node {
        Freelancer freelancer;
        int score;  // Cached composite score
        String id;  // Freelancer ID (for tie-breaking)
        Node left;
        Node right;
        int height;
        
        Node(Freelancer freelancer, int score, String id) {
            this.freelancer = freelancer;
            this.score = score;
            this.id = id;
            this.height = 1;
        }
    }
    
    // Create an AVL tree for a service
    public ServiceAVLTree(Service service) {
        this.service = service;
        this.root = null;
        this.size = 0;
    }
    
    // Calculate composite score for a freelancer based on service requirements
    private int calculateCompositeScore(Freelancer freelancer) {
        // Get service skill requirements
        int[] sSkills = service.getSkillProfile();
        int sT = sSkills[0], sC = sSkills[1], sR = sSkills[2], sE = sSkills[3], sA = sSkills[4];
        int sumS = sT + sC + sR + sE + sA;
        double sumSDivisor = 100.0 * sumS;
        
        // Get freelancer skills
        int fT = freelancer.T, fC = freelancer.C, fR = freelancer.R, fE = freelancer.E, fA = freelancer.A;
        
        // Calculate skill score: dot product of freelancer and service skills
        long dotProductLong = (long)fT * sT + (long)fC * sC + (long)fR * sR + (long)fE * sE + (long)fA * sA;
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
        double composite = 10000.0 * (0.55 * skillScore + 0.25 * ratingScore + 0.20 * reliabilityScore - burnoutPenalty);
        return (int) Math.floor(composite);
    }
    
    // Compare two entries: higher score is better, if equal, smaller ID is better
    private int compare(Node a, Node b) {
        int scoreDiff = b.score - a.score;  // Descending order (higher score first)
        if (scoreDiff != 0) return scoreDiff;
        return a.id.compareTo(b.id);  // Ascending order for IDs (smaller first)
    }
    
    // Compare freelancer with node
    private int compare(Freelancer freelancer, int score, String id, Node node) {
        int scoreDiff = score - node.score;  // Descending order
        if (scoreDiff != 0) return scoreDiff;
        return id.compareTo(node.id);  // Ascending order for IDs
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
    
    // Add a freelancer to the tree (only if available and not employed)
    public void add(Freelancer freelancer) {
        if (freelancer == null || !freelancer.available || freelancer.isEmployed) {
            return;  // Don't add unavailable or employed freelancers
        }
        
        int score = calculateCompositeScore(freelancer);
        root = insert(root, freelancer, score, freelancer.id);
    }
    
    // Recursive insert function
    private Node insert(Node node, Freelancer freelancer, int score, String id) {
        // Perform normal BST insertion
        if (node == null) {
            size++;
            return new Node(freelancer, score, id);
        }
        
        // Check if same freelancer (by ID)
        if (node.id.equals(id)) {
            // Update existing entry
            node.freelancer = freelancer;
            node.score = score;
            return node;
        }
        
        // Compare by score, then by ID
        int cmp = compare(freelancer, score, id, node);
        if (cmp < 0) {
            node.left = insert(node.left, freelancer, score, id);
        } else if (cmp > 0) {
            node.right = insert(node.right, freelancer, score, id);
        } else {
            // Same score and ID (shouldn't happen, but update just in case)
            node.freelancer = freelancer;
            node.score = score;
            return node;
        }
        
        // Update height of this ancestor node
        node.height = 1 + Math.max(height(node.left), height(node.right));
        
        // Get balance factor
        int balance = getBalance(node);
        
        // Left Left Case
        if (balance > 1 && compare(freelancer, score, id, node.left) < 0) {
            return rightRotate(node);
        }
        
        // Right Right Case
        if (balance < -1 && compare(freelancer, score, id, node.right) > 0) {
            return leftRotate(node);
        }
        
        // Left Right Case
        if (balance > 1 && compare(freelancer, score, id, node.left) > 0) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }
        
        // Right Left Case
        if (balance < -1 && compare(freelancer, score, id, node.right) < 0) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }
        
        return node;
    }
    
    // Update a freelancer in the tree (when their data changes)
    public void updateFreelancer(Freelancer freelancer) {
        if (freelancer == null) {
            return;
        }
        
        // Remove old entry
        root = deleteNode(root, freelancer.id);
        
        // Add updated entry if still available and not employed
        if (freelancer.available && !freelancer.isEmployed) {
            add(freelancer);
        }
    }
    
    // Remove a freelancer from the tree
    public void remove(Freelancer freelancer) {
        if (freelancer != null) {
            root = deleteNode(root, freelancer.id);
        }
    }
    
    // Remove a freelancer by ID
    public void removeById(String freelancerId) {
        root = deleteNode(root, freelancerId);
    }
    
    // Recursive delete function
    private Node deleteNode(Node root, String freelancerId) {
        // Perform standard BST delete
        if (root == null) {
            return root;
        }
        
        // Find the node to delete by ID
        if (root.id.equals(freelancerId)) {
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
                // Node with two children: get inorder successor (smallest in right subtree)
                // But we want highest score, so get rightmost in left subtree or leftmost in right
                // Actually, since we're sorted by score descending, we want the leftmost in right subtree
                Node temp = minValueNode(root.right);
                
                // Copy the inorder successor's data to this node
                root.freelancer = temp.freelancer;
                root.score = temp.score;
                root.id = temp.id;
                
                // Delete the inorder successor
                root.right = deleteNode(root.right, temp.id);
            }
        } else {
            // Search in both subtrees (since tree is sorted by score, not ID)
            // Try left subtree first
            Node leftResult = deleteNode(root.left, freelancerId);
            if (leftResult != root.left) {
                // Found and deleted in left subtree
                root.left = leftResult;
            } else {
                // Try right subtree
                root.right = deleteNode(root.right, freelancerId);
            }
        }
        
        // If tree had only one node, return
        if (root == null) {
            return root;
        }
        
        // Update height
        root.height = 1 + Math.max(height(root.left), height(root.right));
        
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
    
    // Find the node with minimum value in a subtree (leftmost node)
    private Node minValueNode(Node node) {
        Node current = node;
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }
    
    // Get top K freelancers (available and not employed, not blacklisted)
    // Traverses in-order (highest score first) and stops after K elements
    public Entry[] getTopK(int k, HashTable blacklist) {
        Entry[] result = new Entry[Math.min(k, size)];
        int[] index = {0};
        
        // Traverse in reverse in-order (right-root-left for descending order)
        reverseInOrder(root, result, index, k, blacklist);
        
        // Return only the filled entries
        Entry[] finalResult = new Entry[index[0]];
        System.arraycopy(result, 0, finalResult, 0, index[0]);
        return finalResult;
    }
    
    // Reverse in-order traversal: right -> root -> left (highest to lowest scores)
    private void reverseInOrder(Node node, Entry[] result, int[] index, int k, HashTable blacklist) {
        if (node == null || index[0] >= k) {
            return;
        }
        
        // Traverse right subtree first (higher scores)
        reverseInOrder(node.right, result, index, k, blacklist);
        
        // Process current node if we haven't reached K yet
        if (index[0] < k) {
            Freelancer f = node.freelancer;
            // Only include available, not-employed, and not-blacklisted freelancers
            if (f != null && f.available && !f.isEmployed) {
                if (blacklist == null || !blacklist.containsKey(f.id)) {
                    // Use cached score from node - it's already up-to-date!
                    // Score is recalculated when freelancer is added/updated via updateFreelancer()
                    int currentScore = node.score;
                    result[index[0]++] = new Entry(f, currentScore, f.id);
                }
            }
        }
        
        // Traverse left subtree last (lower scores)
        if (index[0] < k) {
            reverseInOrder(node.left, result, index, k, blacklist);
        }
    }
    
    // Entry class for top K results
    public static class Entry {
        public Freelancer freelancer;
        public int score;
        public String id;
        
        public Entry(Freelancer freelancer, int score, String id) {
            this.freelancer = freelancer;
            this.score = score;
            this.id = id;
        }
    }
    
    // Get size of tree
    public int size() {
        return size;
    }
    
    // Check if tree is empty
    public boolean isEmpty() {
        return root == null;
    }
}

