import java.util.Scanner;

public class Levenshtein {
    public static int levenshtein(String str1, String str2) {
        int rep = 1, ins = 1, del = 1;
        int m = str1.length();
        int n = str2.length();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            dp[i][0] = i * del;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j * ins;
        }

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int replaceCost = dp[i - 1][j - 1] + rep;
                    int insertCost = dp[i][j - 1] + ins;
                    int deleteCost = dp[i - 1][j] + del;
                    dp[i][j] = Math.min(replaceCost, Math.min(insertCost, deleteCost));
                }
            }
        }

        return dp[m][n];
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String str1 = scanner.nextLine();

        String str2 = scanner.nextLine();

        int distance = levenshtein(str1, str2);
        System.out.println(distance);
    }
}
