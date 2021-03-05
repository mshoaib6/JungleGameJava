package hk.edu.polyu.comp.comp2021.jungle.model;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.Scanner;

/**
 * @author Muhammad Shoaib
 */
public class JungleGame {
    private int[][] layout = {
            { 0, 0, 2, 3, 2, 0, 0 }, // Explanation of the symbols:
            { 0, 0, 0, 2, 0, 0, 0 }, // 0 = Normal board square
            { 0, 0, 0, 0, 0, 0, 0 }, // 1 = Water / River
            { 0, 1, 1, 0, 1, 1, 0 }, // 2 = Trap
            { 0, 1, 1, 0, 1, 1, 0 }, // 3 = Den (Team BLUE) //Player Y
            { 0, 1, 1, 0, 1, 1, 0 }, // 4 = Den (Team RED)  //Player X
            { 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 2, 0, 0, 0 },
            { 0, 0, 2, 4, 2, 0, 0 },
    };
    private BoardSquare[][] board = new BoardSquare[9][7];
    private boolean isComplete = false;
    private boolean isStarted = false;
    private boolean isSaved = false;
    private boolean newPlayer = true;
    private boolean forceStop = false;
    private boolean nextTurn = false;

    private Player playerX = new Player();
    private Player playerY = new Player();
    private Player turn = playerX; // always Player X start the game first
    private Player winner;

    private String[][] data = {
            { "G3", "B2", "F2", "C3", "E3", "A1", "G1", "A3", "", "8" }, // Player X
            { "A7", "F8", "B8", "E7", "C7", "G9", "A9", "G7", "", "8" } // Player Y
    };
    private String[] playerXData = data[0];
    private String[] playerYData = data[1];

    /*
     * The upper part of the board is defined as team BLUE The lower part is defined
     * as team RED
     */

    /**
     * @author Muhammad Shoaib
     */
    public JungleGame() {

    }

    /**
     * @literal method that start the game
     */
    public void start() {
        startOperation();
        while (!isStarted) {
            isStarted = true;
            startGamePack();
        }
    }

    private void startOperation() {
        Scanner input = new Scanner(System.in);
        System.out.println("Welcome Players!\n" + "\tType 'start' to start a new game\n"
                + "\tType 'open [filePath]' to open a saved game");
        String cmd = input.nextLine();
        startOpCmd(cmd);
    }

    /**
     * Handle the user input
     * @param userInput the command that the user inputted
     * @return true, if the command is valid
     *          false, if the command is invalid
     */
    public boolean startOpCmd(String userInput) {
        while (true) {
            String[] cmd = userInput.split(" ");
            try {
                switch (cmd[0]) {
                    case "open":
                        openCmd(cmd[1]);
                        return true;
                    case "start":
                        if (cmd.length != 1) {
                            errorMessage();
                            return false;
                        }
                        playerInitialize();
                        return true;
                }
            } catch (Exception e) {
                errorMessage();
            }
        }
    }

    private void startGamePack() {
        mapInitialize();
        playerChessInitialize();
        // printBoard();
        gameStart();

        if (isStarted) {
            System.out.println("\n\n\t\tEnd Game...\n\n");
            System.out.printf("\t%s wins the game !!!", winner.getName());
        }
    }

    /**
     * @literal a function that initialize both player's chess
     */
    public void playerChessInitialize() {
        chessInitialize(playerX, playerXData);
        chessInitialize(playerY, playerYData);
    }

    private void playerInitialize() {
        if (!newPlayer) {
            playerX.setName(data[0][8]);
            playerY.setName(data[1][8]);
            playerX.setNumberOfChessSurvive(Integer.parseInt(data[0][9]));
            playerY.setNumberOfChessSurvive(Integer.parseInt(data[1][9]));
            return;
        }

        Scanner input = new Scanner(System.in);
        System.out.print("Please Enter the Player's name of Team X: ");
        playerX.setName(input.next());

        System.out.print("Please Enter the Player's name of Team Y: ");
        playerY.setName(input.next());
    }

    /**
     * @literal initialize the map
     */
    public void mapInitialize() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                switch (layout[i][j]) {
                    case 0:
                        board[i][j] = new BoardSquare(" ", i, j);
                        break;
                    case 1:
                        board[i][j] = new WaterSquare(i, j);
                        break;
                    case 2:
                        board[i][j] = new TrapSquare(i, j);
                        break;
                    case 3:
                        board[i][j] = new DenSquare(i, j, playerY);
                        break;
                    case 4:
                        board[i][j] = new DenSquare(i, j, playerX);
                        break;
                }
            }
        }
    }

    private void chessInitialize(Player player, String[] teamPos) {
        // String[] teamXPos = { "G3", "B2", "F2", "C3", "E3", "A1", "G1", "A3" };
        // String[] teamYPos = { "A7", "F8", "B8", "E7", "C7", "G9", "A9", "G7" };
        int pos = 0;
        Chess[] team = { new Rat(), new Cat(), new Dog(), new Wolf(), new Leopard(), new Tiger(), new Lion(),
                new Elephant() };

        for (Chess i : team) {
            i.setTeam(player);
            i.move(teamPos[pos++]);
        }
        player.setChessOwn(team);
    }

    /**
     * Handle the user input with the operation "move", "open", "save", "stop"
     * @param userInput the command that user inputted
     * @return true if the command is valid , vice versa
     * @throws Exception only part of the command is valid
     */
    public boolean cmdHandler(String userInput) throws Exception {
        String[] cmd = userInput.split(" ");
        switch (cmd[0]) {
            case "move":
                nextTurn = moveCmd(cmd[1], cmd[2]);
                return nextTurn;
            case "save":
                saveCmd(cmd[1]);
                nextTurn = false;
                return true;
            case "open":
                if (isSaved) {
                    openCmd(cmd[1]);
                    //forceStop = true;
                    return true;
                }
                System.out
                        .println("\n\tPlease save the current data first !!!\n" + "\tType save [filePath] to save the data\n");
                nextTurn = false;
                return false;
            case "stop":
                forceStop = true;
                return true;
            default:
                errorMessage();
                nextTurn = false;
                return false;
        }
    }

    /**
     * @literal A function to let the player to take turns
     */
    public void gameStart() {
        while (isStarted && !forceStop && !this.isComplete && playerX.getNumberOfChessSurvive() != 0 && playerY.getNumberOfChessSurvive() != 0) {
            printBoard();
            Scanner input = new Scanner(System.in);
            System.out.printf("Input the cmd (%s's turn): ", turn.getName());
            boolean valid;
            try {
                valid = cmdHandler(input.nextLine());
                if (!valid) errorMessage();
            } catch (Exception e) {
                errorMessage();
            }
            // change the turn for other player
            if (nextTurn)
                turn = turn.equals(playerX) ? playerY : playerX;
        }
    }

    /**
     * Save the map into a text file
     * @param filePath the directory being saved
     */
    public void saveCmd(String filePath) {
        try {
            FileWriter file = new FileWriter(filePath);
            saveFile(playerX, file);
            saveFile(playerY, file);
            file.write((turn.equals(playerX)) ? "0" : "1");
            file.close();
            isSaved = true;

        } catch (Exception e) {
            errorMessage();
        }
    }

    /**
     * Save the data of a player into the file
     * @param player The data of the player
     * @param file The location of the file
     * @throws IOException exception for writing file
     */
    private void saveFile(Player player, FileWriter file) throws IOException {
        for (Chess i : player.getChessOwn()) {
            if (i.getPos() == null)
                file.write("");
            else {
                file.write(i.getPos().getY() + 'A');
                file.write(9 - i.getPos().getX() + '0');
            }
            file.write(",");
        }
        file.write(player.getName() + "," + player.getNumberOfChessSurvive() + "\n");
    }

    /**
     * Open the file to get the data inside
     * @param filePath The path of the file
     * @throws Exception The exception of opening a file
     */
    public void openCmd(String filePath) throws Exception {
        int index = 0;

        File file = new File(filePath);
        Scanner input = new Scanner(file);
        while (index < 2) {
            String[] line = input.nextLine().split(",");
            System.arraycopy(line, 0, data[index], 0, line.length);
            index++;
        }
        // System.out.println(Arrays.toString(data[0]));
        turn = (input.nextLine().equals("0")) ? playerX : playerY;
        input.close();
        newPlayer = false;
        isStarted = false;
        playerInitialize();
    }

    /**
     * Move the player's chess
     * @param pos1 The current position of that chess
     * @param pos2 The target position of that chess
     * @return true, if the move command is valid
     *          false, if the move command is invalid
     */
    public boolean moveCmd(String pos1, String pos2) {
        boolean moved;
        try {
            int x = posDecode(pos1.substring(0, 1).toUpperCase());
            int y = posDecode(pos1.substring(1));
            // prevent moving the "NULL" chess to cause error
            Chess chess = board[y][x].getHoldChess();
            if (!chess.getTeam().equals(turn))
                throw new ArithmeticException();
            moved = board[y][x].getHoldChess().move(pos2);
            isSaved = false;
        } catch (Exception e) {
            errorMessage();
            return false;
        }
        return moved;
    }

    /**
     * A Decoder of the position
     * @param str The position of a chess in chess board format e.g. A, 3
     * @return A integer that represent the corresponding index of the array
     */
    private int posDecode(String str) {
        if (str.matches("[A-G]"))
            return str.charAt(0) - 'A';
        return 9 - Integer.parseInt(str);
    }

    /**
     * Print the game board
     */
    public void printBoard() {
        // String[] axis = {"A", "B", "C", "D", "E", "F", "G"};
        for (int i = 0; i < board.length; i++) {
            System.out.printf("%d ", board.length - i);
            for (int j = 0; j < board[i].length; j++) {
                BoardSquare current = board[i][j];
                if (current.getHoldChess() == null)
                    System.out.printf("|  %s  ", current.getSymbol());
                else if (current.getHoldChess().getTeam().equals(playerX))
                    System.out.printf("|[%s]", current.getHoldChess().getName().substring(0, 3));
                else
                    System.out.printf("|(%s)", current.getHoldChess().getName().substring(0, 3));
            }
            System.out.println("|");
        }

        System.out.println("     A     B     C     D     E     F     G");
    }

    /**
     * Print the error message
     */
    private void errorMessage() {
        System.out.println("\n\t...Invalid Command...\n");
    }

    /**
     * @author Muhammad Shoaib
     */
    private class Player {
        private String name;
        private int numberOfChessSurvive = 8;
        private Chess[] chessOwn;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getNumberOfChessSurvive() {
            return numberOfChessSurvive;
        }

        public void setNumberOfChessSurvive(int numberOfChessSurvive) {
            this.numberOfChessSurvive = numberOfChessSurvive;
        }

        public Chess[] getChessOwn() {
            return chessOwn;
        }

        public void setChessOwn(Chess[] chessOwn) {
            this.chessOwn = chessOwn;
        }

        public void reduceNumberOfChessSurvive() {
            this.numberOfChessSurvive--;
        }
    }

    /**
     * @author Muhammad Shoaib
     */
    class BoardSquare {
        private int x;
        private int y;
        private Chess holdChess;
        private String symbol;

        /**
         *
         * @param symbol The symbol that will be printed on the map
         * @param x the x-axis
         * @param y the y-axis
         */
        BoardSquare(String symbol, int x, int y) {
            this.x = x;
            this.y = y;
            this.setHoldChess(null);
            this.symbol = symbol;
        }

        /**
         * Get the integer x
         * @return x
         */
        public int getX() {
            return x;
        }

        /**
         * Get the integer y
         * @return y
         */
        public int getY() {
            return y;
        }

        /**
         * The Square holds a chess
         * @param holdChess the Chess to be held
         */
        public void setHoldChess(Chess holdChess) {
            this.holdChess = holdChess;
        }

        /**
         * Get the chess that this square is holding currently
         * @return Chess being hold
         */
        public Chess getHoldChess() {
            return holdChess;
        }

        /**
         * Get the symbol of this square
         * @return String of the symbol
         */
        public String getSymbol() {
            return symbol;
        }

        /**
         * Add a new chess on this square
         * @param chess The chess want to add in this square
         * @return true, if the chess is valid for holding in this square
         *          false, if the chess is invalid
         */
        boolean add(Chess chess) {
            /*
             * if (chess == null) return;
             */
            if (holdChess == null || chess.eat(holdChess)) {
                if (chess.getPos() != null)
                    chess.getPos().setHoldChess(null);
                chess.setPos(this);
                holdChess = chess;

                chess.setCurRank(chess.getRanking());
                return true;
            }
            return false;
        }
    }

    /**
     * @author Muhammad Shoaib
     */
    class WaterSquare extends BoardSquare {
        /**
         * Allocating the WaterSquare in a specific position
         * @param x the x-axis
         * @param y the y-axis
         */
        WaterSquare(int x, int y) {
            super(".", x, y);
        }

        @Override
        boolean add(Chess chess) {
            if (chess.getRanking() == 1)
                return super.add(chess);
            return false;
        }
    }

    /**
     * @author Muhammad Shoaib
     */
    class TrapSquare extends BoardSquare {

        /**
         * Allocating the TrapSquare in a specific position
         * @param x the x-axis
         * @param y the y-axis
         */
        TrapSquare(int x, int y) {
            super("X", x, y);
        }

        @Override
        boolean add(Chess chess) {
            if (super.add(chess)) {
                chess.setCurRank(0);
                return true;
            }
            return false;
        }
    }

    /**
     * The DenSquare in the board
     * @author Muhammad Shoaib
     */
    class DenSquare extends BoardSquare {
        private Player team;

        /**
         * Allocating the TrapSquare in a specific position and assign it to a Player
         * @param x x-axis
         * @param y y-axis
         * @param team Player's team
         */
        DenSquare(int x, int y, Player team) {
            super("O", x, y);
            this.team = team;
        }

        /**
         * Get the player who owning this den
         * @return The owner of this den
         */
        public Player getTeam() {
            return team;
        }

        @Override
        boolean add(Chess chess) {
            if (chess.getTeam().equals(this.getTeam()))
                return false;
            boolean valid = super.add(chess);
            if (!this.team.equals(chess.getTeam())) {
                isComplete = true;
                winner = chess.getTeam();
            }
            return valid;
        }
    }

    /**
     * Abstract class Chess, the basic model of the Chess
     * @author Muhammad Shoaib
     */
    abstract class Chess {
        private String name;
        private int ranking;
        private int curRank;
        private BoardSquare pos;
        private Player team;

        /**
         * Design the chess with name and ranking
         * @param name the name of the chess
         * @param ranking the ranking of the chess
         */
        Chess(String name, int ranking) {
            this.name = name;
            this.ranking = ranking;
            this.curRank = this.ranking;
            this.setPos(null);
        }

        /**
         * The name of this chess
         * @return String, the name of this chess
         */
        public String getName() {
            return name;
        }

        /**
         * Get the ranking of this chess
         * @return int, the ranking of this chess
         */
        public int getRanking() {
            return ranking;
        }

        /**
         * Get the current ranking of this chess, in case this chess is in a trap
         * @return int, the current ranking of this chess
         */
        public int getCurRank() {
            return curRank;
        }

        /**
         * Set the current ranking of this chess
         * @param curRank the ranking of the chess, if the chess is in a trap then it will be 0
         */
        public void setCurRank(int curRank) {
            this.curRank = curRank;
        }

        /**
         * Get the position of this chess, in other words, get the square which is holding this chess
         * @return BoardSquare, the position of the chess
         */
        public BoardSquare getPos() {
            return pos;
        }

        /**
         * Set the position of this chess, in order words, set a square to hold this chess
         * @param pos BoardSquare, the square that will hold this chess
         */
        public void setPos(BoardSquare pos) {
            this.pos = pos;
        }

        /**
         * Get the owner of this chess
         * @return Player, the player who owns this chess
         */
        public Player getTeam() {
            return team;
        }

        /**
         * Set the owner of this chess
         * @param team The owner of this chess
         */
        public void setTeam(Player team) {
            this.team = team;
        }

        /**
         * Move this chess to a specific position
         * @param pos the desired position
         * @return true, if the move is valid
         *          false, if the move is invalid
         */
        boolean move(String pos) {
            if (pos.equals(""))
                return false;

            BoardSquare curPos = this.pos;
            int x = posDecode(pos.substring(0, 1).toUpperCase());
            int y = posDecode(pos.substring(1));
            if (curPos != null) {
                int x_dis = Math.abs(x - curPos.getY());
                int y_dis = Math.abs(y - curPos.getX());
                if ((x_dis == 0 && y_dis == 1) || (x_dis == 1 && y_dis == 0)){
                    return board[y][x].add(this);
                    //return true;
                }
            } else {
                return board[y][x].add(this);
                //return true;
            }
            return false;
        }

        /**
         * Decide whether this chess can eat the targeted chess
         * @param chess the targeted chess
         * @return true, if the targeted chess is eatable
         *          false, if the targeted chess is not eatable
         */
        boolean eat(Chess chess) {
            if (this.curRank >= chess.getCurRank() && !chess.getTeam().equals(this.team)) {
                changeChessNumber(chess);
                chess.setPos(null);
                return true;
            }
            return false;
        }

        /**
         * Change the number of chess still survive in this game
         * @param chess the chess of its team
         */
        void changeChessNumber(Chess chess) {
            if (chess.getTeam().equals(playerX))
                playerX.reduceNumberOfChessSurvive();
            else
                playerY.reduceNumberOfChessSurvive();
        }
    }

    /**
     * The Chess Rat
     * @author Muhammad Shoaib
     */
    class Rat extends Chess {

        /**
         * @literal The constructor of Rat
         */
        Rat() {
            super("Rat", 1);
        }

        @Override
        public boolean eat(Chess chess) {
            if ((this.getCurRank() >= chess.getCurRank() || chess.getCurRank() == 8) && !chess.getTeam().equals(this.getTeam())
                    && this.getPos().getClass().equals(chess.getPos().getClass())) {
                changeChessNumber(chess);
                chess.setPos(null);
                return true;
            }
            return false;
        }
    }

    /**
     * The Chess Cat
     * @author Muhammad Shoaib
     */
    class Cat extends Chess {
        /**
         * @literal The constructor of Cat
         */
        Cat() {
            super("Cat", 2);
        }
    }

    /**
     * The chess Dog
     * @author Muhammad Shoaib
     */
    class Dog extends Chess {
        /**
         * @literal The constructor of Dog
         */
        Dog() {
            super("Dog", 3);
        }
    }

    /**
     * The chess Wolf
     * @author Muhammad Shoaib
     */
    class Wolf extends Chess {
        /**
         * @literal The constructor of Wolf
         */
        Wolf() {
            super("Wolf", 4);
        }
    }

    /**
     * The chess Leopard
     * @author Muhammad Shoaib
     */
    class Leopard extends Chess {
        /**
         * @literal The constructor of Leopard
         */
        Leopard() {
            super("Leopard", 5);
        }
    }

    /**
     * The chess Tiger
     * @author Muhammad Shoaib
     */
    class Tiger extends Chess {
        /**
         * @literal The constructor of Tiger
         */
        Tiger() {
            super("Tiger", 6);
        }

        /**
         * The constructor of Tiger (for making Lion)
         * @param name The name of the chess
         * @param rank The ranking of the chess
         */
        Tiger(String name, int rank) {
            super(name, rank);
        }

        @Override
        boolean move(String pos) {
            if (pos.equals(""))
                return false;

            BoardSquare curPos = this.getPos();
            int x = posDecode(pos.substring(0, 1).toUpperCase());
            int y = posDecode(pos.substring(1));

            if (curPos != null) {
                int x_dis = this.getPos().getX() - y;
                int y_dis = this.getPos().getY() - x;
                // System.out.println(x_dis + " " + y_dis);

                // handle the horizontal jump
                if (x_dis == 0 && y_dis == -3) {
                    for (int i = 1; i < 3; i++) {
                        if (board[this.getPos().getX()][this.getPos().getY() + i].getHoldChess() != null ||
                                !(board[this.getPos().getX()][this.getPos().getY() + i] instanceof WaterSquare))
                            return false;
                    }
                    board[y][x].add(this);
                    return true;
                }

                // handle the horizontal jump
                if (x_dis == 0 && y_dis == 3) {
                    for (int i = 1; i < 3; i++) {
                        if (board[this.getPos().getX()][this.getPos().getY() - i].getHoldChess() != null ||
                                !(board[this.getPos().getX()][this.getPos().getY() - i] instanceof WaterSquare))
                            return false;
                    }
                    board[y][x].add(this);
                    return true;
                }

                // handle the vertical jump
                if (x_dis == 4 && y_dis == 0) {
                    for (int i = 1; i < 4; i++) {
                        if (board[this.getPos().getX() - i][this.getPos().getY()].getHoldChess() != null ||
                                !(board[this.getPos().getX() - i][this.getPos().getY()] instanceof WaterSquare))
                            return false;
                    }
                    board[y][x].add(this);
                    System.out.println("OK");
                    return true;
                }

                // handle the vertical jump
                if (x_dis == -4 && y_dis == 0) {
                    for (int i = 1; i < 4; i++) {
                        if (board[this.getPos().getX() + i][this.getPos().getY()].getHoldChess() != null ||
                                !(board[this.getPos().getX() + i][this.getPos().getY()] instanceof WaterSquare))
                            return false;
                    }
                    board[y][x].add(this);
                    return true;
                }
            }
            return super.move(pos);
        }
    }

    /**
     * The Chess Lion
     * @author Muhammad Shoaib
     */
    class Lion extends Tiger {
        /**
         * @literal The constructor of Lion
         */
        Lion() {
            super("Lion", 7);
        }
    }

    /**
     * The chess Elephant
     * @author Muhammad Shoaib
     */
    class Elephant extends Chess {
        /**
         * @literal The constructor of Elephant
         */
        Elephant() {
            super("Elephant", 8);
        }

        @Override
        public boolean eat(Chess chess) {
            if (this.getCurRank() >= chess.getCurRank() && chess.getCurRank() != 1 && !chess.getTeam().equals(this.getTeam())) {

                changeChessNumber(chess);
                chess.setPos(null);
                return true;
            }
            return false;
        }
    }
}