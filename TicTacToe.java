import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class TicTacToe extends JFrame {
    private JButton[][] buttons = new JButton[3][3];
    private JLabel statusLabel;
    private JButton restartButton;
    private JButton undoButton;
    private boolean playerXTurn = true;
    private int moveCount = 0;
    private boolean gameOver = false;
    private boolean vsComputer = false;

    private int xWins = 0;
    private int oWins = 0;
    private int draws = 0;
    private JLabel scoreLabel;

    private Move lastMove = null;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ModeSelectionScreen());
    }

    public TicTacToe(boolean computerMode) {
        this.vsComputer = computerMode;
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Tic Tac Toe");
        setSize(450, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(0xEDE7FB));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0xEDE7FB));

        scoreLabel = new JLabel(getScoreText(), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 18));
        scoreLabel.setForeground(new Color(0x8B5FBF));
        scoreLabel.setOpaque(true);
        scoreLabel.setBackground(new Color(0xFBE7F0));
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        topPanel.add(scoreLabel, BorderLayout.NORTH);

        statusLabel = new JLabel(vsComputer ? "Your Turn (X)" : "Player X's Turn", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 24));
        statusLabel.setForeground(new Color(0x8B5FBF));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(0xFFFFFF));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(statusLabel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3, 3, 8, 8));
        boardPanel.setBackground(new Color(0xEDE7FB));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j] = new JButton("");
                buttons[i][j].setFont(new Font("Segoe UI", Font.BOLD, 60));
                buttons[i][j].setFocusPainted(false);
                buttons[i][j].setBackground(new Color(0xFFFFFF));
                buttons[i][j].setForeground(new Color(0x333333));
                buttons[i][j].setBorder(BorderFactory.createLineBorder(new Color(0xE3AADD), 2));

                final int row = i;
                final int col = j;

                buttons[i][j].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (buttons[row][col].getText().equals("") && !gameOver) {
                            buttons[row][col].setBackground(new Color(0xFBE7F0));
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (buttons[row][col].getText().equals("") && !gameOver) {
                            buttons[row][col].setBackground(new Color(0xFFFFFF));
                        }
                    }
                });

                buttons[i][j].addActionListener(new ButtonClickListener(i, j));
                boardPanel.add(buttons[i][j]);
            }
        }
        add(boardPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(new Color(0xEDE7FB));

        undoButton = new JButton("Undo Move");
        undoButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        undoButton.setBackground(new Color(0xBCE7F5));
        undoButton.setForeground(Color.WHITE);
        undoButton.setFocusPainted(false);
        undoButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        undoButton.setEnabled(false);
        undoButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (undoButton.isEnabled()) {
                    undoButton.setBackground(new Color(0xC3C7F3));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (undoButton.isEnabled()) {
                    undoButton.setBackground(new Color(0xBCE7F5));
                }
            }
        });
        undoButton.addActionListener(e -> undoLastMove());

        restartButton = new JButton("Restart Game");
        restartButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        restartButton.setBackground(new Color(0xF58CBA));
        restartButton.setForeground(Color.WHITE);
        restartButton.setFocusPainted(false);
        restartButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        restartButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                restartButton.setBackground(new Color(0xE3AADD));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                restartButton.setBackground(new Color(0xF58CBA));
            }
        });
        restartButton.addActionListener(e -> restartGame());

        bottomPanel.add(undoButton);
        bottomPanel.add(restartButton);
        add(bottomPanel, BorderLayout.SOUTH);

        if (vsComputer) {
            undoButton.setEnabled(false);
            undoButton.setVisible(false);
        } else {
            undoButton.setVisible(true);
        }

        setVisible(true);
    }

    private class ButtonClickListener implements ActionListener {
        private int row;
        private int col;

        public ButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (gameOver || !buttons[row][col].getText().equals("")) {
                return;
            }

            if (vsComputer && !playerXTurn) {
                return;
            }

            makeMove(row, col);

            if (vsComputer && !gameOver && !playerXTurn) {
                javax.swing.Timer timer = new javax.swing.Timer(500, evt -> {
                    computerMove();
                });
                timer.setRepeats(false);
                timer.start();
            }
        }
    }

    private void makeMove(int row, int col) {
        lastMove = new Move(row, col, playerXTurn);

        if (playerXTurn) {
            buttons[row][col].setText("X");
            buttons[row][col].setForeground(new Color(0xF58CBA));
            buttons[row][col].setBackground(new Color(0xFBE7F0));
        } else {
            buttons[row][col].setText("O");
            buttons[row][col].setForeground(new Color(0x7BBFD6));
            buttons[row][col].setBackground(new Color(0xFBE7F0));
        }

        moveCount++;
        undoButton.setEnabled(!gameOver && moveCount > 0);

        if (checkWinner()) {
            gameOver = true;
            String winner = playerXTurn ? "X" : "O";

            if (playerXTurn) {
                xWins++;
            } else {
                oWins++;
            }

            updateScoreLabel();

            if (vsComputer) {
                statusLabel.setText(playerXTurn ? "You Win!" : "Computer Wins!");
            } else {
                statusLabel.setText("Player " + winner + " Wins!");
            }
            statusLabel.setForeground(new Color(0x8B5FBF));
            statusLabel.setBackground(new Color(0xF5D5E8));
            undoButton.setEnabled(false);
            return;
        }

        if (moveCount == 9) {
            gameOver = true;
            draws++;
            updateScoreLabel();
            statusLabel.setText("It's a Draw!");
            statusLabel.setForeground(new Color(0x8B5FBF));
            statusLabel.setBackground(new Color(0xD5E8F5));
            undoButton.setEnabled(false);
            return;
        }

        playerXTurn = !playerXTurn;

        if (vsComputer) {
            statusLabel.setText(playerXTurn ? "Your Turn (X)" : "Computer's Turn (O)");
        } else {
            statusLabel.setText("Player " + (playerXTurn ? "X" : "O") + "'s Turn");
        }
    }

    private void computerMove() {
        if (gameOver) return;

        int[] move = findBestMove();
        if (move != null) {
            makeMove(move[0], move[1]);
        }
    }

    private int[] findBestMove() {
        if (moveCount >= 9) return null;

        int[] blockOrWin = checkForWinOrBlock("O");
        if (blockOrWin != null) return blockOrWin;

        blockOrWin = checkForWinOrBlock("X");
        if (blockOrWin != null) return blockOrWin;

        if (buttons[1][1].getText().equals("")) {
            return new int[]{1, 1};
        }

        int[][] corners = {{0, 0}, {0, 2}, {2, 0}, {2, 2}};
        for (int[] corner : corners) {
            if (buttons[corner[0]][corner[1]].getText().equals("")) {
                return corner;
            }
        }

        ArrayList<int[]> emptySpots = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().equals("")) {
                    emptySpots.add(new int[]{i, j});
                }
            }
        }

        if (!emptySpots.isEmpty()) {
            return emptySpots.get(new Random().nextInt(emptySpots.size()));
        }

        return null;
    }

    private int[] checkForWinOrBlock(String player) {
        for (int i = 0; i < 3; i++) {
            if (buttons[i][0].getText().equals(player) &&
                    buttons[i][1].getText().equals(player) &&
                    buttons[i][2].getText().equals("")) {
                return new int[]{i, 2};
            }
            if (buttons[i][0].getText().equals(player) &&
                    buttons[i][2].getText().equals(player) &&
                    buttons[i][1].getText().equals("")) {
                return new int[]{i, 1};
            }
            if (buttons[i][1].getText().equals(player) &&
                    buttons[i][2].getText().equals(player) &&
                    buttons[i][0].getText().equals("")) {
                return new int[]{i, 0};
            }
        }

        for (int j = 0; j < 3; j++) {
            if (buttons[0][j].getText().equals(player) &&
                    buttons[1][j].getText().equals(player) &&
                    buttons[2][j].getText().equals("")) {
                return new int[]{2, j};
            }
            if (buttons[0][j].getText().equals(player) &&
                    buttons[2][j].getText().equals(player) &&
                    buttons[1][j].getText().equals("")) {
                return new int[]{1, j};
            }
            if (buttons[1][j].getText().equals(player) &&
                    buttons[2][j].getText().equals(player) &&
                    buttons[0][j].getText().equals("")) {
                return new int[]{0, j};
            }
        }

        if (buttons[0][0].getText().equals(player) &&
                buttons[1][1].getText().equals(player) &&
                buttons[2][2].getText().equals("")) {
            return new int[]{2, 2};
        }
        if (buttons[0][0].getText().equals(player) &&
                buttons[2][2].getText().equals(player) &&
                buttons[1][1].getText().equals("")) {
            return new int[]{1, 1};
        }
        if (buttons[1][1].getText().equals(player) &&
                buttons[2][2].getText().equals(player) &&
                buttons[0][0].getText().equals("")) {
            return new int[]{0, 0};
        }

        if (buttons[0][2].getText().equals(player) &&
                buttons[1][1].getText().equals(player) &&
                buttons[2][0].getText().equals("")) {
            return new int[]{2, 0};
        }
        if (buttons[0][2].getText().equals(player) &&
                buttons[2][0].getText().equals(player) &&
                buttons[1][1].getText().equals("")) {
            return new int[]{1, 1};
        }
        if (buttons[1][1].getText().equals(player) &&
                buttons[2][0].getText().equals(player) &&
                buttons[0][2].getText().equals("")) {
            return new int[]{0, 2};
        }

        return null;
    }

    private void undoLastMove() {
        if (lastMove == null || gameOver) {
            return;
        }

        buttons[lastMove.row][lastMove.col].setText("");
        buttons[lastMove.row][lastMove.col].setBackground(new Color(0xFFFFFF));
        moveCount--;
        playerXTurn = lastMove.wasXTurn;
        lastMove = null;

        if (moveCount == 0) {
            undoButton.setEnabled(false);
        }

        if (vsComputer) {
            statusLabel.setText(playerXTurn ? "Your Turn (X)" : "Computer's Turn (O)");
        } else {
            statusLabel.setText("Player " + (playerXTurn ? "X" : "O") + "'s Turn");
        }
        statusLabel.setBackground(new Color(0xFFFFFF));
    }

    private boolean checkWinner() {
        String currentPlayer = playerXTurn ? "X" : "O";

        for (int i = 0; i < 3; i++) {
            if (buttons[i][0].getText().equals(currentPlayer) &&
                    buttons[i][1].getText().equals(currentPlayer) &&
                    buttons[i][2].getText().equals(currentPlayer)) {
                highlightWinningButtons(buttons[i][0], buttons[i][1], buttons[i][2]);
                return true;
            }
        }

        for (int j = 0; j < 3; j++) {
            if (buttons[0][j].getText().equals(currentPlayer) &&
                    buttons[1][j].getText().equals(currentPlayer) &&
                    buttons[2][j].getText().equals(currentPlayer)) {
                highlightWinningButtons(buttons[0][j], buttons[1][j], buttons[2][j]);
                return true;
            }
        }

        if (buttons[0][0].getText().equals(currentPlayer) &&
                buttons[1][1].getText().equals(currentPlayer) &&
                buttons[2][2].getText().equals(currentPlayer)) {
            highlightWinningButtons(buttons[0][0], buttons[1][1], buttons[2][2]);
            return true;
        }

        if (buttons[0][2].getText().equals(currentPlayer) &&
                buttons[1][1].getText().equals(currentPlayer) &&
                buttons[2][0].getText().equals(currentPlayer)) {
            highlightWinningButtons(buttons[0][2], buttons[1][1], buttons[2][0]);
            return true;
        }

        return false;
    }

    private void highlightWinningButtons(JButton btn1, JButton btn2, JButton btn3) {
        Color winColor = new Color(0xE3AADD);
        btn1.setBackground(winColor);
        btn2.setBackground(winColor);
        btn3.setBackground(winColor);
    }

    private void restartGame() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setBackground(new Color(0xFFFFFF));
                buttons[i][j].setEnabled(true);
            }
        }

        playerXTurn = true;
        moveCount = 0;
        gameOver = false;
        lastMove = null;
        undoButton.setEnabled(false);

        if (vsComputer) {
            statusLabel.setText("Your Turn (X)");
        } else {
            statusLabel.setText("Player X's Turn");
        }
        statusLabel.setForeground(new Color(0x8B5FBF));
        statusLabel.setBackground(new Color(0xFFFFFF));
    }

    private String getScoreText() {
        return String.format("X: %d | O: %d | Draws: %d", xWins, oWins, draws);
    }

    private void updateScoreLabel() {
        scoreLabel.setText(getScoreText());
    }

    private static class Move {
        int row;
        int col;
        boolean wasXTurn;

        Move(int row, int col, boolean wasXTurn) {
            this.row = row;
            this.col = col;
            this.wasXTurn = wasXTurn;
        }
    }
}

class ModeSelectionScreen extends JFrame {

    public ModeSelectionScreen() {
        setTitle("Tic Tac Toe - Select Mode");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0xEDE7FB));

        JLabel titleLabel = new JLabel("Choose Game Mode", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 28));
        titleLabel.setForeground(new Color(0x8B5FBF));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));
        add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 10, 20));
        buttonPanel.setBackground(new Color(0xEDE7FB));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));

        JButton friendButton = createModeButton("Play with Friend", new Color(0xF58CBA), new Color(0xE3AADD));
        friendButton.addActionListener(e -> {
            dispose();
            new TicTacToe(false);
        });

        JButton computerButton = createModeButton("Play with Computer", new Color(0xBCE7F5), new Color(0xC3C7F3));
        computerButton.addActionListener(e -> {
            dispose();
            new TicTacToe(true);
        });

        buttonPanel.add(friendButton);
        buttonPanel.add(computerButton);

        add(buttonPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private JButton createModeButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 20));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }
}
