import java.io.*;
import java.util.*;

import static java.util.Map.Entry;

public class FruitRage {
    private static int[][] matrix;
    private static int n;
    private static int p;
    private static double time;
    private static int current_depth;
    private static BufferedWriter bw;
    private static long st;
    private static Position best;
    private static int[][] bestGravityImplementedBoard;

    static class Position {
        public int i;
        public int j;

        Position(int a, int b) {
            i = a;
            j = b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Position position = (Position) o;

            if (i != position.i) return false;
            return j == position.j;

        }

        @Override
        public int hashCode() {
            int result = i;
            result = 31 * result + j;
            return result;
        }
    }

    private static void readAndSet() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("input.txt")));
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            n = Integer.parseInt(lines.get(0));
            p = Integer.parseInt(lines.get(1));
            time = Float.parseFloat(lines.get(2));
            matrix = new int[n][n];
            lines.remove(0);
            lines.remove(0);
            lines.remove(0);
            for (int i = 0; i < n; ++i) {
                line = lines.get(i);
                for (int j = 0; j < n; ++j) {
                    if (line.charAt(j) == '*')
                        matrix[i][j] = p;
                    else
                        matrix[i][j] = Character.getNumericValue(line.charAt(j));
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getIslandSize(int i, int j, int cost, int[][] board) {
        if (board[i][j] == p)
            return cost;
        int temp = board[i][j];
        board[i][j] = p;
        cost++;
        if ((i + 1 < n) && temp == board[i + 1][j]) {
            cost = getIslandSize(i + 1, j, cost, board);
        }
        if ((i - 1 >= 0) && temp == board[i - 1][j]) {
            cost = getIslandSize(i - 1, j, cost, board);
        }
        if ((j + 1 < n) && temp == board[i][j + 1]) {
            cost = getIslandSize(i, j + 1, cost, board);
        }
        if ((j - 1 >= 0) && temp == board[i][j - 1]) {
            cost = getIslandSize(i, j - 1, cost, board);
        }
        return cost;
    }

    private static int getMaxIslandSize(int[][] board) {
        int i, j, max = 0;
        for (i = 0; i < n; ++i) {
            for (j = 0; j < n; ++j) {
                if (board[i][j] != p) {
                    max = Math.max(max, getIslandSize(i, j, 0, board));
                }
            }
        }
        return max;
    }

    private static Map<Position, Integer> getIslandCoordinateScoreMap(int[][] board) {
        int i, j;
        Map<Position, Integer> islandCoordinateScoreMap = new HashMap<>();
        int score;
        for (i = 0; i < n; ++i) {
            for (j = 0; j < n; ++j) {
                if (board[i][j] != p) {
                    score = getIslandSize(i, j, 0, board);
                    islandCoordinateScoreMap.put(new Position(i, j), score * score);
                }
            }
        }
        return islandCoordinateScoreMap;
    }

    private static int[][] getCopy(int[][] input) {
        int[][] output = new int[n][n];
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                output[i][j] = input[i][j];
            }
        }
        return output;
    }

    private static void printBoard(int[][] board) throws IOException {
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (board[i][j] == p)
                    bw.write("*");
                else
                    bw.write(board[i][j] + "");
            }
            bw.write("\n");
        }
    }

    private static void printBoardSout(int[][] board) throws IOException {
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (board[i][j] == p)
                    System.out.print('*');
                else
                    System.out.print(board[i][j]);
            }
            System.out.println();
        }
    }

    private static int[][] applyGravity(int[][] board) {
        int i, j;
        for (j = 0; j < n; ++j) {
            int c = 0;
            for (i = n - 1; i >= 0; --i) {
                if (board[i][j] == p) {
                    c++;
                    continue;
                }
                board[i + c][j] = board[i][j];
            }
            for (i = 0; i < c; ++i) {
                board[i][j] = p;
            }
        }
        //printBoard(board);
        return board;
    }

    private static int[][] getGravityImplementedBoard(Position p, int[][] board) {
        getIslandSize(p.i, p.j, 0, board);
        return applyGravity(board);
    }

    private static List<Entry<Position, Integer>> getScoreSortedCoordinateList(Map<Position, Integer> islandCoordinateScoreMap, boolean ascending) {
        List<Entry<Position, Integer>> scoreSortedCoordinateList = new ArrayList<>(islandCoordinateScoreMap.entrySet());
        Collections.sort(
                scoreSortedCoordinateList,
                (c1, c2) -> {
                    if (ascending) {
                        return c1.getValue().compareTo(c2.getValue());
                    }
                    return c2.getValue().compareTo(c1.getValue());
                });
        return scoreSortedCoordinateList;
    }

    private static void printBeforeExit() throws IOException {
        bw.write(String.valueOf((char) (best.j + 1 + 64)) + (best.i + 1) + "\n");
        int[][] output = getGravityImplementedBoard(best, getCopy(matrix));
        printBoard(output);
        printBoardSout(output);
        bw.close();
        bw = new BufferedWriter(new FileWriter("score.txt"));
        bw.write(getIslandSize(best.i, best.j, 0, matrix) + "");
        bw.close();
        System.exit(0);
    }

    private static int minValue(int[][] board, int depth, int alpha, int beta, int cost) throws IOException {
        if (Double.compare(((System.currentTimeMillis() - st) / 1000.00) + 0.1, time) > 0) {
            printBeforeExit();
            System.exit(0);
        }
        if (depth == current_depth) {
            int maxIslandSize = getMaxIslandSize(board);
            return cost - (maxIslandSize * maxIslandSize);
        }
        List<Entry<Position, Integer>> scoreSortedCoordinateList = getScoreSortedCoordinateList(getIslandCoordinateScoreMap(getCopy(board)), true);
        for (Entry<Position, Integer> move : scoreSortedCoordinateList) {
            beta = Math.min(beta, maxValue(getGravityImplementedBoard(move.getKey(), getCopy(board)), depth + 1, alpha, beta, cost - move.getValue()));
            if (beta <= alpha) {
                return alpha;
            }
        }
        return beta;
    }

    private static int maxValue(int[][] board, int depth, int alpha, int beta, int cost) throws IOException {
        if (Double.compare(((System.currentTimeMillis() - st) / 1000.00) + 0.1, time) > 0) {
            printBeforeExit();
        }
        if (depth == current_depth) {
            int maxIslandSize = getMaxIslandSize(board);
            return cost + (maxIslandSize * maxIslandSize);
        }
        List<Entry<Position, Integer>> scoreSortedCoordinateList = getScoreSortedCoordinateList(getIslandCoordinateScoreMap(getCopy(board)), false);
        for (Entry<Position, Integer> move : scoreSortedCoordinateList) {
            alpha = Math.max(alpha, minValue(getGravityImplementedBoard(move.getKey(), getCopy(board)), depth + 1, alpha, beta, cost + move.getValue()));
            if (alpha >= beta) {
                return beta;
            }
        }
        return alpha;
    }

    private static void run(List<Entry<Position, Integer>> scoreSortedCoordinateList) throws IOException{
        int max = Integer.MIN_VALUE, temp;
        if (current_depth == 1) {
            best = scoreSortedCoordinateList.get(0).getKey();
            max = scoreSortedCoordinateList.get(0).getValue();
        } else {
            for (Entry<Position, Integer> move : scoreSortedCoordinateList) {
                temp = minValue(getGravityImplementedBoard(move.getKey(), getCopy(matrix)), 2, Integer.MIN_VALUE, Integer.MAX_VALUE, move.getValue());
                if (temp > max) {
                    best = move.getKey();
                    max = temp;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        st = System.currentTimeMillis();
        bw = new BufferedWriter(new FileWriter("output.txt"));
        readAndSet();
        List<Entry<Position, Integer>> scoreSortedCoordinateList = getScoreSortedCoordinateList(getIslandCoordinateScoreMap(getCopy(matrix)), false);
        System.out.println(scoreSortedCoordinateList.size());
        int depth = scoreSortedCoordinateList.size();
        if (time < 50.00) {
            current_depth = Math.min(3, depth);
            if (time < 20.00)
                current_depth = Math.min(2, depth);
            if (time < 5.00)
                current_depth = 1;
            run(scoreSortedCoordinateList);
            printBeforeExit();
        } else {
            time = time / ((scoreSortedCoordinateList.size() / 2.0) + 1);
            if (depth == 1 || depth == 2) {
                current_depth = depth;
                run(scoreSortedCoordinateList);
                printBeforeExit();
            } else {
                current_depth = 3;
                while (current_depth <= depth) {
                    run(scoreSortedCoordinateList);
                    current_depth += 2;
                }
                printBeforeExit();
            }
        }
    }
}