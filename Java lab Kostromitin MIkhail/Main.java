public class Main {
    public static void main(String[] args) {
        int[] array = {6, 11, 4, 3, 45, 1};

        AVLTree tree = new AVLTree(array);

        tree.printTree();
        System.out.println();

        tree.insert(5);
        tree.insert(10);
        tree.insert(2);
        tree.insert(7);
        System.out.println(Boolean.toString(tree.isAVL()));
        
        tree.printTree();

    }
}