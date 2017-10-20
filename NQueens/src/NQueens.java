import java.io.*;
import java.util.*;

public class NQueens {

    private static int n;
    private static int salamanderToBePlaced;
    private static String searchType;
    private static int[] salamanderPossiblyBePlaced;
    private static BufferedWriter bw;

    /*Common functions*/

    private static int[][] readAndSet() {
        int[][] matrix = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("input.txt")));
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            searchType = lines.get(0);
            n = Integer.parseInt(lines.get(1));
            matrix = new int[n][n];
            salamanderToBePlaced = Integer.parseInt(lines.get(2));
            lines.remove(0);
            lines.remove(0);
            lines.remove(0);
            for (int i = 0; i < n; ++i) {
                line = lines.get(i);
                for (int j = 0; j < n; ++j) {
                    matrix[i][j] = Character.getNumericValue(line.charAt(j));
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return matrix;
    }

    private static void printBoard(int[][] matrix) throws IOException {
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                bw.write(matrix[i][j] + "");
            }
            bw.write("\n");
        }
    }

    static private int setAndGetSpeculativeNumberOfSalamander(int[][] matrix) {
        int i, j;
        salamanderPossiblyBePlaced = new int[n];
        for (j = n - 1; j >= 0; --j) {
            for (i = 0; i < n; ++i) {
                if (matrix[i][j] == 0) {
                    salamanderPossiblyBePlaced[j] = 1;
                    break;
                }
            }
            ++i;
            for (; i < n; ++i) {
                if (matrix[i][j] == 0 && matrix[i - 1][j] == 2) {
                    salamanderPossiblyBePlaced[j]++;
                }
            }
            if (j != (n - 1)) {
                salamanderPossiblyBePlaced[j] += salamanderPossiblyBePlaced[j + 1];
            }
        }
        return salamanderPossiblyBePlaced[0];
    }

    /*BFS*/

    private static int[][] intervalsForBFS = new int[1000][2];
    private static int intervalCountForBFS = 0;
    private static Map<Integer, Set<Integer>> conflictPositions = new HashMap<>();
    private static Queue<List<Integer>> salamanderQueue = new LinkedList<>();

    private static void setIntervalsForBFS(int[][] matrix) {
        int start, end, i, j, current;
        for (j = 0; j < n; ++j) {
            for (i = 0; i < n; ++i) {
                if (matrix[i][j] == 0)
                    break;
            }
            current = 0;
            start = end = i;
            for (; i < n; ++i) {
                if (matrix[i][j] == 2 && current == 0) {
                    end = i - 1;
                    intervalsForBFS[intervalCountForBFS][0] = (j * n) + start;/*new SalamanderSA.Position(start,j);*/
                    intervalsForBFS[intervalCountForBFS][1] = (j * n) + end;/*new SalamanderSA.Position(end,j)*/
                    ;
                    intervalCountForBFS++;
                    current = 2;
                }
                if (matrix[i][j] == 0 && current == 2) {
                    start = i;
                    current = 0;
                }
            }
            if (matrix[n - 1][j] == 0) {
                end = n - 1;
                intervalsForBFS[intervalCountForBFS][0] = (j * n) + start;/*new SalamanderSA.Position(start,j);*/
                intervalsForBFS[intervalCountForBFS][1] = (j * n) + end;/*new SalamanderSA.Position(end,j);*/
                intervalCountForBFS++;
            }
        }
    }

    private static void setConflictPositionsForBFS(int[][] matrix) {
        int i, j, p, q;
        Set<Integer> conflictSet;
        for (j = 0; j < n; ++j) {
            for (i = 0; i < n; ++i) {
                conflictSet = new HashSet<>();
                if (matrix[i][j] == 0) {
                    for (p = j + 1; p < n; ++p) {
                        if (matrix[i][p] == 2) {
                            break;
                        }
                        conflictSet.add((p * n) + i);
                    }
                    for (p = i, q = j; p < n && q < n; ++p, ++q) {
                        if (matrix[p][q] == 2) {
                            break;
                        }
                        conflictSet.add((q * n) + p);
                    }
                    for (p = i, q = j; p >= 0 && q < n; --p, ++q) {
                        if (matrix[p][q] == 2) {
                            break;
                        }
                        conflictSet.add((q * n) + p);
                    }
                }
                conflictPositions.put((j * n) + i, conflictSet);
            }
        }
    }

    private static int getIntervalForBFS(int element) {
        for (int i = 0; i < intervalCountForBFS; ++i) {
            if (intervalsForBFS[i][0] <= element && intervalsForBFS[i][1] >= element) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isSafeToAdd(List<Integer> combinationList, int element) {
        Set<Integer> setOfUnsafePositions = new HashSet<>();
        for (int i = 0; i < combinationList.size(); ++i) {
            setOfUnsafePositions.addAll(conflictPositions.get(combinationList.get(i)));
        }
        return !setOfUnsafePositions.contains(element);
    }

    private static void printCombinationListForBFS(List<Integer> combinationList, int[][] matrix) throws IOException {
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (combinationList.contains((j * n) + i)) {
                    System.out.print(1);
                    bw.write(1 + "");
                } else {
                    System.out.print(matrix[i][j]);
                    bw.write(matrix[i][j] + "");
                }
            }
            bw.write("\n");
            System.out.println();
        }
    }

    private static void bfs(int[][] matrix) throws IOException {
        int lastSetInterval, i, j;
        List<Integer> combinationList, nextCombinationList;
        setIntervalsForBFS(matrix);
        setConflictPositionsForBFS(matrix);
        if (salamanderToBePlaced > setAndGetSpeculativeNumberOfSalamander(matrix)) {
            System.out.println("FAIL");
            bw.write("FAIL\n");
            return;
        }
        for (j = 0; j < n; ++j) {
            if (salamanderToBePlaced > salamanderPossiblyBePlaced[j]) {
                break;
            }
            for (i = 0; i < n; ++i) {
                if (matrix[i][j] != 2) {
                    combinationList = new ArrayList<>();
                    combinationList.add((j * n) + i);
                    salamanderQueue.add(combinationList);
                }
            }
        }
        if (salamanderToBePlaced == 1) {
            System.out.println("OK");
            bw.write("OK\n");
            printCombinationListForBFS(salamanderQueue.remove(), matrix);
            return;
        }
        while (salamanderQueue.size() != 0) {
            combinationList = salamanderQueue.remove();
            lastSetInterval = getIntervalForBFS(combinationList.get(combinationList.size() - 1));
            for (j = lastSetInterval + 1; j < intervalCountForBFS; ++j) {
                if ((salamanderToBePlaced - combinationList.size()) > (intervalCountForBFS - j)) {
                    break;
                }
                for (i = intervalsForBFS[j][0]; i <= intervalsForBFS[j][1]; ++i) {
                    if (isSafeToAdd(combinationList, i)) {
                        nextCombinationList = new ArrayList<>(combinationList);
                        nextCombinationList.add(i);
                        if (nextCombinationList.size() == salamanderToBePlaced) {
                            System.out.println("OK");
                            bw.write("OK\n");
                            printCombinationListForBFS(nextCombinationList, matrix);
                            return;
                        }
                        salamanderQueue.add(nextCombinationList);
                    }
                }
            }
        }
        System.out.println("FAIL");
        bw.write("FAIL\n");
    }

    /*DFS*/

    private static int[][] solution;

    private static int getNextPossibleSquareInColumn(int curRow, int col) {
        for (int i = curRow + 1; i < n; ++i) {
            if (solution[i][col] == 2 && ((i + 1) < n)) {
                if (solution[i + 1][col] != 2) {
                    return i + 1;
                } else {
                    continue;
                }
            }
        }
        return -1;
    }

    private static boolean isSafeForDFS(int i, int j) {
        int a, b;
        for (a = j - 1; a >= 0; --a) {
            if (solution[i][a] == 2)
                break;
            if (solution[i][a] == 1)
                return false;
        }

        for (a = i, b = j; a < n && b >= 0; ++a, --b) {
            if (solution[a][b] == 1)
                return false;
            if (solution[a][b] == 2)
                break;
        }
        for (a = i, b = j; a >= 0 && b >= 0; --a, --b) {
            if (solution[a][b] == 1)
                return false;
            if (solution[a][b] == 2)
                break;
        }
        return true;
    }

    private static boolean salamanderDFS(int i, int j, int toBePlaced) throws IOException {
        solution[i][j] = 1;
        toBePlaced--;
        if (toBePlaced == 0) {
            System.out.println("OK");
            bw.write("OK\n");
            printBoard(solution);
            return true;
        }
        int nextPossibleSquareInColumn = getNextPossibleSquareInColumn(i, j);
        while (nextPossibleSquareInColumn != -1) {
            int k = nextPossibleSquareInColumn;
            while (k < n && (solution[k][j] != 2)) {
                if (isSafeForDFS(k, j)) {
                    if (salamanderDFS(k, j, toBePlaced)) {
                        return true;
                    }
                }
                k++;
            }
            nextPossibleSquareInColumn = getNextPossibleSquareInColumn(nextPossibleSquareInColumn, j);
        }
        for (int l = j + 1; l < n; ++l) {
            if (toBePlaced > salamanderPossiblyBePlaced[l]) {
                break;
            }
            for (int k = 0; k < n; ++k) {
                if (solution[k][l] != 2 && isSafeForDFS(k, l)) {
                    if (salamanderDFS(k, l, toBePlaced)) {
                        return true;
                    }
                }
            }
        }
        toBePlaced++;
        solution[i][j] = 0;
        return false;
    }

    private static void dfs(int[][] matrix) throws IOException {
        solution = matrix;
        if (setAndGetSpeculativeNumberOfSalamander(matrix) < salamanderToBePlaced) {
            System.out.println("FAIL");
            bw.write("FAIL\n");
            return;
        }
        for (int j = 0; j < n; ++j) {
            if (salamanderToBePlaced > salamanderPossiblyBePlaced[j]) {
                break;
            }
            for (int i = 0; i < n; ++i) {
                if (solution[i][j] != 2) {
                    if (salamanderDFS(i, j, salamanderToBePlaced)) {
                        return;
                    }
                }
            }
        }
        System.out.println("FAIL");
        bw.write("FAIL\n");

    }

    /*Simulated Annealing*/

    private static class Position {
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

    private static Random rn = new Random();
    private static Date endTime = new Date(new Date().getTime() + 288000);
    private static Position[][] intervalsForSA = new Position[1000][2];
    private static int intervalCountForSA = 0;
    private static Set<Integer> freeIntervals = new HashSet<>();

    /*private static double getBoltzmannDistribution(int energyDelta) {
        System.out.println(Math.pow(Math.exp(1), ((energyDelta) / ((100.0) / ++round))));
        return Math.pow(Math.exp(1), ((energyDelta) / ((100.0) / ++round)));
    }*/

    private static int[][] getRandomPositionMatrixForSA(int[][] matrix) {
        int[][] random = matrix;
        int ranSet, setCount = salamanderToBePlaced;
        Position start, end;
        while (setCount > 0) {
            ranSet = rn.nextInt(intervalCountForSA);
            if (freeIntervals.contains(ranSet)) {
                freeIntervals.remove(ranSet);
                setCount--;
                start = intervalsForSA[ranSet][0];
                end = intervalsForSA[ranSet][1];
                if (start.i == end.i) {
                    random[start.i][start.j] = 1;
                    continue;
                }
                random[start.i + rn.nextInt(end.i - start.i + 1)][start.j] = 1;
            }
        }
        return random;
    }

    private static void setIntervalsForSA(int[][] matrix) {
        int start, end, i, j, current;
        for (j = 0; j < n; ++j) {
            for (i = 0; i < n; ++i) {
                if (matrix[i][j] == 0)
                    break;
            }
            current = 0;
            start = end = i;
            for (; i < n; ++i) {
                if (matrix[i][j] == 2 && current == 0) {
                    end = i - 1;
                    intervalsForSA[intervalCountForSA][0] = new Position(start, j);
                    intervalsForSA[intervalCountForSA][1] = new Position(end, j);
                    freeIntervals.add(intervalCountForSA);
                    intervalCountForSA++;
                    current = 2;
                }
                if (matrix[i][j] == 0 && current == 2) {
                    start = i;
                    current = 0;
                }
            }
            if (matrix[n - 1][j] == 0) {
                end = n - 1;
                intervalsForSA[intervalCountForSA][0] = new Position(start, j);
                intervalsForSA[intervalCountForSA][1] = new Position(end, j);
                freeIntervals.add(intervalCountForSA);
                intervalCountForSA++;
            }
        }
    }

    private static int getIntervalForSA(Position randomConflictPosition) {
        for (int i = 0; i < intervalCountForSA; ++i) {
            if (intervalsForSA[i][0].j == randomConflictPosition.j) {
                if ((randomConflictPosition.i >= intervalsForSA[i][0].i) && (randomConflictPosition.i <= intervalsForSA[i][1].i)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static Position getRandomReplacementPosition(Position randomConflictPosition) {
        Position randomReplacementPosition;
        int conflictInterval = getIntervalForSA(randomConflictPosition), randInterval, replaceInterval;
        Position start, end;
        if (freeIntervals.size() == 0) {
            start = intervalsForSA[conflictInterval][0];
            end = intervalsForSA[conflictInterval][1];
            if (start.i == end.i) {
                return randomConflictPosition;
            }
            while (true) {
                randomReplacementPosition = new Position(start.i + rn.nextInt(end.i - start.i + 1), start.j);
                if (!randomConflictPosition.equals(randomReplacementPosition)) {
                    return randomReplacementPosition;
                }
            }
        } else {
            //iterate over free sets and get a random free interval
            Iterator<Integer> setIterator = freeIntervals.iterator();
            randInterval = rn.nextInt(freeIntervals.size());
            replaceInterval = 0;
            while (randInterval-- >= 0) {
                replaceInterval = setIterator.next();
            }
            freeIntervals.remove(replaceInterval);
            freeIntervals.add(conflictInterval);

            start = intervalsForSA[replaceInterval][0];
            end = intervalsForSA[replaceInterval][1];
            if (start.i == end.i) {
                return start;
            }
            randomReplacementPosition = new Position(start.i + rn.nextInt(end.i - start.i + 1), start.j);
            return randomReplacementPosition;
        }
    }

    private static Set<Position> getConflictingQueensSet(int[][] matrix) {
        int i, j, k;
        Set<Position> conflictedPositions = new HashSet<>();
        Set<Position> conflicts;
        //row check
        for (i = 0; i < n; ++i) {
            conflicts = new HashSet<>();
            for (j = 0; j < n; ++j) {
                if (matrix[i][j] == 1) {
                    conflicts.add(new Position(i, j));
                }
                if (matrix[i][j] == 2) {
                    if (conflicts.size() > 1) {
                        conflictedPositions.addAll(conflicts);
                    }
                    conflicts = new HashSet<>();
                }
            }
            if (conflicts.size() > 1) {
                conflictedPositions.addAll(conflicts);
            }
        }

        //major diagonal check
        for (i = 0; i < n * 2; i++) {
            conflicts = new HashSet<>();
            for (j = 0; j <= i; j++) {
                k = i - j;
                if (k < n && j < n) {
                    if (matrix[k][j] == 1) {
                        conflicts.add(new Position(k, j));
                    }
                    if (matrix[k][j] == 2) {
                        if (conflicts.size() > 1) {
                            conflictedPositions.addAll(conflicts);
                        }
                        conflicts = new HashSet<>();
                    }
                }
            }
            if (conflicts.size() > 1) {
                conflictedPositions.addAll(conflicts);
            }
        }


        //minor diagonal check
        k = -1;
        for (i = n - 1; i > 0; --i) {
            conflicts = new HashSet<>();
            k++;
            for (j = 0; j <= k; ++j) {
                if (matrix[j][i + j] == 1) {
                    conflicts.add(new Position(j, i + j));
                }
                if (matrix[j][i + j] == 2) {
                    if (conflicts.size() > 1) {
                        conflictedPositions.addAll(conflicts);
                    }
                    conflicts = new HashSet<>();
                }
            }
            if (conflicts.size() > 1) {
                conflictedPositions.addAll(conflicts);
            }
        }
        k = -1;
        for (i = n - 1; i >= 0; --i) {
            conflicts = new HashSet<>();
            k++;
            for (j = 0; j <= k; ++j) {
                if (matrix[i + j][j] == 1) {
                    conflicts.add(new Position(i + j, j));
                }
                if (matrix[i + j][j] == 2) {
                    if (conflicts.size() > 1) {
                        conflictedPositions.addAll(conflicts);
                    }
                    conflicts = new HashSet<>();
                }
            }
            if (conflicts.size() > 1) {
                conflictedPositions.addAll(conflicts);
            }
        }
        return conflictedPositions;
    }

    private static void sa(int[][] matrix) throws IOException {
        int percentage, randPosition, currentValue, nextValue;
        int[][] tempMatrix, bestSolution = null;
        setIntervalsForSA(matrix);
        if (intervalCountForSA < salamanderToBePlaced) {
            System.out.println("FAIL");
            bw.write("FAIL\n");
            return;
        }
        Position randConflictPosition = null, randReplacePosition;
        Set<Position> currentSet, nextSet;
        Iterator<Position> positionIterator;
        tempMatrix = getRandomPositionMatrixForSA(matrix);
        currentSet = getConflictingQueensSet(tempMatrix);
        currentValue = currentSet.size();
        if (currentValue == 0) {
            System.out.println("OK");
            bw.write("OK\n");
            printBoard(tempMatrix);
            return;
        }
        while (endTime.getTime() - new Date().getTime() > 0) {
            //get randomConflictPosition
            randPosition = rn.nextInt(currentValue);
            positionIterator = currentSet.iterator();
            while (randPosition-- >= 0) {
                randConflictPosition = positionIterator.next();
            }

            //get randomReplacePosition
            randReplacePosition = getRandomReplacementPosition(randConflictPosition);

            tempMatrix[randConflictPosition.i][randConflictPosition.j] = 0;
            tempMatrix[randReplacePosition.i][randReplacePosition.j] = 1;

            nextSet = getConflictingQueensSet(tempMatrix);
            nextValue = nextSet.size();
            if (nextValue == 0) {
                bestSolution = tempMatrix;
                System.out.println("OK");
                bw.write("OK\n");
                printBoard(tempMatrix);
                return;
            }
            if (nextValue <= currentValue) {
                currentValue = nextValue;
                currentSet = nextSet;
                bestSolution = tempMatrix;
            } else {
                percentage = (int) (((endTime.getTime() - new Date().getTime()) / 576000.0) * 100)/*(int) (getBoltzmannDistribution(currentValue - nextValue) * 100)*/;
                if (rn.nextInt(100) < percentage) {
                    currentValue = nextValue;
                    currentSet = nextSet;
                } else {
                    tempMatrix[randConflictPosition.i][randConflictPosition.j] = 1;
                    tempMatrix[randReplacePosition.i][randReplacePosition.j] = 0;
                    freeIntervals.add(getIntervalForSA(randReplacePosition));
                    freeIntervals.remove(getIntervalForSA(randConflictPosition));
                }
            }
        }
        System.out.println("FAIL");
        bw.write("FAIL\n");
    }

    public static void main(String[] args) throws IOException {
        int[][] matrix = readAndSet();
        bw = new BufferedWriter(new FileWriter("output.txt"));
        if (salamanderToBePlaced == 0) {
            System.out.println("OK");
            bw.write("OK\n");
            printBoard(matrix);
            bw.close();
            return;
        }
        switch (searchType) {
            case "BFS":
                bfs(matrix);
                break;
            case "DFS":
                dfs(matrix);
                break;
            case "SA":
                sa(matrix);
                break;
        }
        bw.close();
    }
}
