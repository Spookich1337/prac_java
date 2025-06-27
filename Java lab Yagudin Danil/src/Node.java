package src;

public class Node {
        int val;
        Node left, right;
        int height;

        Node(int val) {
            this.val = val;
            this.height = 1;
        }
}