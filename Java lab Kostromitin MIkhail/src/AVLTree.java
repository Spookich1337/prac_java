public class AVLTree {
    private Node root;

    public AVLTree() {
        this.root = null;
    }

    /** Построение сбалансированного дерева из массива */
    public AVLTree(int[] arr) {
        // Сортируем «на месте»
        java.util.Arrays.sort(arr);
        this.root = buildFromSorted(arr, 0, arr.length - 1);
    }

    /** Рекурсивное создание идеально сбалансированного дерева */
    private Node buildFromSorted(int[] a, int lo, int hi) {
        if (lo > hi) return null;
        int mid = lo + (hi - lo) / 2;
        Node node = new Node(a[mid]);
        node.left = buildFromSorted(a, lo, mid - 1);
        node.right = buildFromSorted(a, mid + 1, hi);
        return balance(node);
    }

    /** Вставка значения */
    public void insert(int val) {
        root = insert(root, val);
    }

    private Node insert(Node node, int val) {
        if (node == null)
            return new Node(val);
        if (val < node.val)
            node.left = insert(node.left, val);
        else if (val > node.val)
            node.right = insert(node.right, val);
        // иначе дубликат — не вставляем
        return balance(node);
    }

    /** Удаление узла со значением val */
    public void remove(int val) {
        root = remove(root, val);
    }

    private Node remove(Node node, int val) {
        if (node == null) return null;
        if (val < node.val) {
            node.left = remove(node.left, val);
        } else if (val > node.val) {
            node.right = remove(node.right, val);
        } else {
            // нашли узел
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;
            // оба поддерева есть — найдём минимальный в правом
            Node min = findMin(node.right);
            node.val = min.val;
            node.right = removeMin(node.right);
        }
        return balance(node);
    }

    /** Удалить минимальный узел в дереве и вернуть корень оставшегося */
    private Node removeMin(Node node) {
        if (node.left == null) return node.right;
        node.left = removeMin(node.left);
        return balance(node);
    }

    /** Найти минимальный узел */
    private Node findMin(Node node) {
        while (node.left != null) node = node.left;
        return node;
    }

    /** Балансировка поддерева с корнем node */
    private Node balance(Node node) {
        updateHeight(node);
        int diff = height(node.left) - height(node.right);
        if (diff == 2) {
            if (height(node.left.left) >= height(node.left.right))
                node = rotateRight(node);
            else
                node = doubleRotateRight(node);
        } else if (diff == -2) {
            if (height(node.right.right) >= height(node.right.left))
                node = rotateLeft(node);
            else
                node = doubleRotateLeft(node);
        }
        return node;
    }

    private int height(Node node) {
        return node == null ? 0 : node.height;
    }

    private void updateHeight(Node node) {
        node.height = Math.max(height(node.left), height(node.right)) + 1;
    }

    /** Малый правый поворот*/
    private Node rotateRight(Node y) {
        Node x = y.left;
        y.left = x.right;
        x.right = y;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    /** Малый левый поворот*/
    private Node rotateLeft(Node y) {
        Node x = y.right;
        y.right = x.left;
        x.left = y;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    /** Большой правый поворот*/
    private Node doubleRotateRight(Node y) {
        y.left = rotateLeft(y.left);
        return rotateRight(y);
    }

    /** Большой левый поворот*/
    private Node doubleRotateLeft(Node y) {
        y.right = rotateRight(y.right);
        return rotateLeft(y);
    }

    /** Проверка, что дерево — AVL */
    public boolean isAVL() {
        return isAVL(root);
    }

    private boolean isAVL(Node node) {
        if (node == null) return true;
        int diff = Math.abs(height(node.left) - height(node.right));
        if (diff > 1) return false;
        return isAVL(node.left) && isAVL(node.right);
    }

    /** Минимальная разница между соседними значениями в дереве */
    public int minNodeDiff() {
        return minNodeDiff(root, Integer.MAX_VALUE);
    }

    private int minNodeDiff(Node node, int best) {
        if (node == null) return best;
        if (node.left != null)
            best = Math.min(best, Math.abs(node.val - node.left.val));
        if (node.right != null)
            best = Math.min(best, Math.abs(node.val - node.right.val));
        best = minNodeDiff(node.left, best);
        best = minNodeDiff(node.right, best);
        return best;
    }

    /** Обходы «на месте» — печатаем сразу в консоль */
    public void inOrder() {
        inOrder(root);
        System.out.println();
    }
    private void inOrder(Node node) {
        if (node == null) return;
        inOrder(node.left);
        System.out.print(node.val + " ");
        inOrder(node.right);
    }

    public void preOrder() {
        preOrder(root);
        System.out.println();
    }
    private void preOrder(Node node) {
        if (node == null) return;
        System.out.print(node.val + " ");
        preOrder(node.left);
        preOrder(node.right);
    }

    public void postOrder() {
        postOrder(root);
        System.out.println();
    }
    private void postOrder(Node node) {
        if (node == null) return;
        postOrder(node.left);
        postOrder(node.right);
        System.out.print(node.val + " ");
    }

    /** Визуальный вывод структуры дерева */
    public void printTree() {
        printTree(root, "", true);
    }
    private void printTree(Node node, String prefix, boolean isTail) {
        if (node == null) return;
        System.out.println(prefix + (isTail ? "└── " : "├── ") + node.val);
        printTree(node.left, prefix + (isTail ? "    " : "│   "), false);
        printTree(node.right, prefix + (isTail ? "    " : "│   "), true);
    }

    /** Получить корень*/
    public Node getRoot() {
        return root;
    }
}