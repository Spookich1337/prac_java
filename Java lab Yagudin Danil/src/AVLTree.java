package src;

public class AVLTree {   
    private Node root;

    //открытые методы для работы с деревом

    public void insert(int val) {
        root = insert(root, val);
    }

    public void delete(int val) {
        root = delete(root, val);
    }

    public boolean isBalanced() {
        return check(root);
    }

    public int height() {
        return getHeight(root);
    }

    // приватные мотоды 

    private int getHeight(Node node) {
        return node == null ? 0 : node.height;
    }

    private int balanceFactor(Node node) {
        return getHeight(node.right) - getHeight(node.left);
    }

    private void fixHeight(Node node) {
        node.height = Math.max(getHeight(node.left), getHeight(node.right)) + 1;
    }

    private Node rightRotation(Node node) {
        Node ret = node.left;
        node.left = ret.right;
        ret.right = node;

        fixHeight(node);
        fixHeight(ret);
        return ret;
    }

    private Node leftRotation(Node node) {
        Node ret = node.right;
        node.right = ret.left;
        ret.left = node;

        fixHeight(node);
        fixHeight(ret);
        return ret;
    }

    private Node lrRotation(Node node) {
        node.left = leftRotation(node.left);
        return rightRotation(node);
    }

    private Node rlRotation(Node node) {
        node.right = rightRotation(node.right);
        return leftRotation(node);
    }

    private Node insert(Node node, int val) {
        if (node == null) return new Node(val);

        if (val < node.val) {
            node.left = insert(node.left, val);
            if (getHeight(node.left) - getHeight(node.right) == 2) {
                if (val < node.left.val)
                    node = rightRotation(node);
                else
                    node = lrRotation(node);
            }
        } else {
            node.right = insert(node.right, val);
            if (getHeight(node.right) - getHeight(node.left) == 2) {
                if (val < node.right.val)
                    node = rlRotation(node);
                else
                    node = leftRotation(node);
            }
        }

        fixHeight(node);
        return node;
    }

    private Node balance(Node node) {
        fixHeight(node);
        int bf = balanceFactor(node);

        if (bf == -2) {
            if (balanceFactor(node.left) > 0)
                node.left = leftRotation(node.left);
            return rightRotation(node);
        }

        if (bf == 2) {
            if (balanceFactor(node.right) < 0)
                node.right = rightRotation(node.right);
            return leftRotation(node);
        }

        return node;
    }

    private Node findMin(Node node) {
        return node.left == null ? node : findMin(node.left);
    }

    private Node deleteMin(Node node) {
        if (node.left == null)
            return node.right;
        node.left = deleteMin(node.left);
        return balance(node);
    }

    private Node delete(Node node, int val) {
        if (node == null) return null;

        if (val < node.val) {
            node.left = delete(node.left, val);
        } else if (val > node.val) {
            node.right = delete(node.right, val);
        } else {
            Node l = node.left;
            Node r = node.right;
            if (r != null) {
                Node min = findMin(r);
                min.right = deleteMin(r);
                min.left = l;
                return balance(min);
            } else {
                return l;
            }
        }
        return balance(node);
    }

    private int getHigh(Node node) {
        if (node == null) return 0;
        int highL = getHigh(node.left);
        int highR = getHigh(node.right);
        return Math.max(highL, highR) + 1;
    }

    private boolean compareHigh(Node node) {
        return Math.abs(getHigh(node.left) - getHigh(node.right)) <= 1;
    }

    private boolean check(Node node) {
        if (node == null) return true;
        if (!compareHigh(node)) return false;
        return check(node.left) && check(node.right);
    }

    // вывод дерева в терминал

    public void inOrderPrint() {
        inOrder(root);
        System.out.println();
    }

    private void inOrder(Node node) {
        if (node == null) return;
        inOrder(node.left);
        System.out.print(node.val + " ");
        inOrder(node.right);
    }
}