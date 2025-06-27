import src.AVLTree;

public class Main {
    public static void main(String[] args) {
        AVLTree tree = new AVLTree();

        for (int i = 0; i < 20; i ++) {
            int value = (int) (Math.random() * 100);
            System.out.println("Inserting: " + value);
            tree.insert(value);
        }

        System.out.println("In-order traversal of the AVL tree:");
        tree.inOrderPrint();
        System.out.println();
    }

}