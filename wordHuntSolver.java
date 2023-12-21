import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

class HW13 extends App {
    
    // adopted from HW09 to build our custom dictionary trie
    static class DictNode  {
        DictNode[] children;
        boolean isTerminal;
        boolean hasChildren;
        
        DictNode() {
            this.children = new DictNode[26];
            this.isTerminal = false;
            this.hasChildren = false;
        }
    }
    
    static class Trie {
        DictNode root;
        Trie() { root = new DictNode(); }
        
        boolean addWord(String word) {
            DictNode curr = root;
            for (char letter : word.toCharArray()) {
                int letterIndex = letter - 'A';
                if (curr.children[letterIndex] == null) {
                    DictNode child = new DictNode();
                    curr.children[letterIndex] = child;
                    if (!curr.hasChildren) {
                        curr.hasChildren = true;
                    }
                    curr = child;
                } else {
                    curr = curr.children[letterIndex];
                }
            }
            if (!curr.isTerminal) {
                curr.isTerminal = true;
                return true;
            }
            return false;
        }
        
        boolean contains(ArrayList<Character> word) {
            DictNode curr = root;
            for (char letter : word) {
                int letterIndex = letter - 'A';
                if (curr.children[letterIndex] == null) {
                    return false;
                } else {
                    curr = curr.children[letterIndex];
                }
            }
            if (curr.isTerminal) {
                return true;
            }
            return false;
        }
        
        // helps us terminate DFS early
        boolean checkChildren(ArrayList<Character> prefix) {
            DictNode curr = root;
            for (char letter : prefix) {
                int letterIndex = letter - 'A';
                if (curr.children[letterIndex] == null) {
                    return false;
                } else {
                    curr = curr.children[letterIndex];
                }
            }
            return curr.hasChildren;
        }
    }
    
    // represents each letter in grid
    static class LetterNode {
        char letter;
        ArrayList<LetterNode> adjacent;
        boolean visited;
        
        LetterNode(char letter) {
            this.letter = letter;
            this.adjacent = new ArrayList<>();
            this.visited = false;
        }
    }
    
    // collection of letters
    static class Board {
        int dims;
        LetterNode[][] letterArray;
        ArrayList<String> validWords;
        
        // sets up & clears board
        void initialize() {
            validWords = new ArrayList<String>();
            letterArray = new LetterNode[dims][dims];
            for (int c = 0; c < (dims * dims); c++) {
                int row = c / dims;
                int col = c % dims;
                letterArray[row][col] = new LetterNode(' ');
            }
            for (int r = 0; r < dims; r++) {
                for (int c = 0; c < dims; c++) {
                    for (int dr = -1; dr < 2; dr++) {
                        for (int dc = -1; dc < 2; dc++) {
                            int adjRow = r + dr;
                            int adjCol = c + dc;
                            if (adjCol >= 0 && adjCol < dims && adjRow >= 0 &&
                                adjRow < dims && (adjRow != r || adjCol != c)) {
                                LetterNode currNode = letterArray[c][r];
                                currNode.adjacent.add(letterArray[adjCol][adjRow]);
                            }
                        }
                    }
                }
            } 
        }
        
        // recursive DFS
        void traverse() {
            validWords = new ArrayList<>();
            for (int c = 0; c < dims; c++) {
                for (int r = 0; r < dims; r++) {
                    dfs(letterArray[r][c], new ArrayList<>(), validWords);
                }
            }
            Collections.sort(validWords, Collections.reverseOrder(new LenComparator()));
        } 
        
        void setLetter(int row, int col, char letter) {
            letterArray[row][col].letter = letter;
        }
        
        char getLetter(int row, int col) {
            return letterArray[row][col].letter;
        }
        
        Board(int dims) {
            this.dims = dims;
        }
    }
    
    // helper function for DFS
    static void dfs(LetterNode currLetter, ArrayList<Character> path, ArrayList<String> result) {
        if (!trie.checkChildren(path) || currLetter.visited) {
            return;
        }
        
        path.add(currLetter.letter);
        currLetter.visited = true;
        
        if (path.size() > 2 && trie.contains(path)) {
            String completeWord = "";
            for (char c : path) {
                completeWord += c;
            }
            if (!result.contains(completeWord)) {
                result.add(completeWord);
            }
        }
        
        for (LetterNode neighbor : currLetter.adjacent) {
            dfs(neighbor, path, result);
        }
        currLetter.visited = false;
        if (!path.isEmpty()) {
            path.remove(path.size() - 1);
        }
    }
    
    // helps sort words (since we want the longest words for most points)
    static class LenComparator implements Comparator<String> {
        public int compare(String word1, String word2) {
            return Integer.compare(word1.length(), word2.length());
        }
    }
    
    boolean inBetween(double x, double x_min, double x_max) {
        return (x > x_min) && (x < x_max);
    }
    
    // ensures board is filled out
    boolean readyToTraverse(Board board, int dims) {
        for (int r = 0; r < dims; r++) {
            for (int c = 0; c < dims; c++) {
                if (board.getLetter(r, c) == ' ') {
                    return false;
                }
            }
        }
        return true;
    }
    
    static Trie trie;
    int dims;
    Board board;
    boolean traversed;
    boolean squareSelected;
    int selectedX;
    int selectedY;
    boolean lettersValid;
    // colors for board :D
    Vector3 sageGreen = new Vector3(0.75, 0.8, 0.75);
    Vector3 lightSage = new Vector3(0.9, 0.93, 0.9);
    Vector3 darkSage = new Vector3(0.25, 0.4, 0.25);
    Vector3 medSage = new Vector3(0.45, 0.6, 0.45);
  
    void setup() {
        trie = new Trie();
        // sets up dictionary given custom list of words
        try {
            File file = new File("words_list.txt"); // words_list.txt is adopted from Collins Scrabble Words (2019)
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String word = scanner.nextLine().trim();
                trie.addWord(word); 
            }
            scanner.close();
        } catch (FileNotFoundException rawr) { 
            System.err.println("File not found: " + rawr.getMessage() + ". Make sure words_list.txt is downloaded!");
        }
        
        dims = 4;
        board = new Board(dims);
        board.initialize();
        traversed = false;
        lettersValid = true;
        
        System.out.println("solve a 4x4 Word Hunt / Boggle board in seconds! click any cell and begin typing letters, and press 'search words'");
        System.out.println("words are checked against the Collins Scrabble Words (2019) list and arranged from longest to shortest (for maximum points).");
        System.out.println("press 'reset' to clear the current board and solve a new board.");                      
    }
    
    void loop() {
        double gridWidth = 8.0;
        int dims = 4;
        double squareWidth = gridWidth / dims;
        double wordsListX = 17;
        double wordsListY = 10.3;
        Vector2 gridLowerLeft = new Vector2(3.0, 3.8);
        Vector2 gridUpperRight = new Vector2(gridLowerLeft.x + gridWidth, gridLowerLeft.y + gridWidth);
        double gridCenterX = (gridLowerLeft.x + gridUpperRight.x) / 2;
        
        drawCornerRectangle(new Vector2(1.75, 12.625), new Vector2(22.25, 13.25), lightSage);
        drawString("★ word hunt solver! ★", new Vector2(12.0, 14.2), darkSage, 26, true);
        drawString("click on a cell and start typing letters to fill out the board, then press 'search words' :3", 
                   new Vector2(12.0, 12.85), medSage, 12, true);
        drawCornerRectangle(new Vector2(gridLowerLeft.x, 1.5), new Vector2(gridUpperRight.x, 3.0), sageGreen);
        drawString("search words", new Vector2(gridCenterX, 2.25), darkSage, 18, true);
        drawCornerRectangle(new Vector2(wordsListX - 4, 1.5), new Vector2(wordsListX + 4, 3.0), sageGreen);
        drawString("reset board", new Vector2(wordsListX, 2.25), darkSage, 18, true);
        drawCornerRectangle(new Vector2(wordsListX - 4, gridLowerLeft.y), 
                            new Vector2(wordsListX + 4, gridUpperRight.y), lightSage);
        
        for (int i = 0; i < dims + 1; i++) { // draw grid
            drawLine(new Vector2(gridLowerLeft.x + squareWidth * i, gridLowerLeft.y), 
                     new Vector2(gridLowerLeft.x + squareWidth * i, gridUpperRight.y), sageGreen);
            drawLine(new Vector2(gridLowerLeft.x, gridLowerLeft.y + squareWidth * i), 
                     new Vector2(gridUpperRight.x, gridLowerLeft.y + squareWidth * i), sageGreen);
        }
        
        for (int r = 0; r < dims; r++) { // print board letters
            for (int c = 0; c < dims; c++) {
                double x = gridLowerLeft.x + squareWidth * (c + 0.5);
                double y = gridLowerLeft.y + squareWidth * (3 - r + 0.5);
                drawString("" + board.getLetter(r, c), new Vector2(x, y), medSage, 32, true);
            }
        }
       
        if (inBetween(mousePosition.x, wordsListX - 4, wordsListX + 4) &&
            inBetween(mousePosition.y, 1.5, 3.0) && mousePressed) {
            board.initialize(); // reset board
        }
        
        if (inBetween(mousePosition.x, gridLowerLeft.x, gridUpperRight.x) && inBetween(mousePosition.y, 1.5, 3.0) && mousePressed) {
            if (readyToTraverse(board, dims)) {
                board.traverse();
                traversed = true;
            } else {
                System.out.println("must fill in the entire board to find words!");
            }
        }
        
        if (traversed) { // prints found words (top 20 if more than 20 words are generated)
            int validWordsLen;
            if (board.validWords.size() >= 20) {
                validWordsLen = 20;
            } else {
                validWordsLen = board.validWords.size();
            }
            drawString("found words", new Vector2(wordsListX, 11.1), darkSage, 18, true);
            for (int i = 0; i < validWordsLen / 2; i++) {
                drawString(board.validWords.get(i), new Vector2(wordsListX - 2, wordsListY - i * 0.65), medSage, 14, true);
            }
            for (int i = validWordsLen / 2; i < validWordsLen; i++) {
                drawString(board.validWords.get(i), new Vector2(wordsListX + 2, wordsListY - (i - 10) * 0.65), 
                           medSage, 14, true);
            }
        }
        
        if (inBetween(mousePosition.x, gridLowerLeft.x, gridUpperRight.x) &&
            inBetween(mousePosition.y, gridLowerLeft.y, gridUpperRight.y) && mousePressed) { // edit selected cell
            squareSelected = true;
            selectedX = (int) Math.floor((mousePosition.x - gridLowerLeft.x) / squareWidth);
            selectedY = dims - (int) Math.floor((mousePosition.y - gridLowerLeft.y) / squareWidth) - 1;
        }
        
        if (squareSelected && keyAnyPressed) {
            if ('a' <= keyLastPressed && keyLastPressed <= 'z') {
                board.setLetter(selectedY, selectedX, (char) (keyLastPressed - 32)); // set letter & move to next cell
                if (selectedX < dims - 1) {
                    selectedX++;
                } else if (selectedX == dims - 1) {
                    if (selectedY < dims - 1) {
                        selectedY++;
                        selectedX = 0;
                    }
                }
            }
        }
    }
    
    public static void main(String[] arguments) {
        App app = new HW13();
        app.setWindowBackgroundColor(0.87, 0.9, 0.87);
        app.setWindowSizeInWorldUnits(24.0, 16.0);
        app.setWindowCenterInWorldUnits(12.0, 8.0);
        app.setWindowHeightInPixels(512);
        app.setWindowTopLeftCornerInPixels(96, 64);
        app.run();
    }
}