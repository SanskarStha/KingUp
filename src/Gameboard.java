
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The class Gameboard is to print control the flow of the game and
 * contains a set of players and characters.
 */
public class Gameboard {

    /**
     * The maximum number of characters can be placed in the same position.
     */
    public static final int FULL = 4;
    /**
     * The total number of characters
     */
    public static final int NO_OF_CHARACTER = 13;
    /**
     * The total number of player
     */
    public static final int NO_OF_PLAYERS = 4;
    /**
     * The position of Throne
     */
    public static final int THRONE = 6;
    /**
     * The scores calculation formula
     */
    public static final int[] SCORES = {0, 1, 2, 3, 4, 5, 10};

    /**
     * The name of the characters
     */
    public static final String[] CHARACTER_NAMES = {
            "Aligliero", "Beatrice", "Clemence", "Dario",
            "Ernesto", "Forello", "Gavino", "Irima",
            "Leonardo", "Merlino", "Natale", "Odessa", "Piero"
    };
    /**
     * The name of the players
     */
    public static final String[] PLAYER_NAMES = {
            "You", "Computer 1", "Computer 2", "Computer 3"
    };
    /**
     * Determine if the players are human player or not.
     */
    public static final boolean[] HUMAN_PLAYERS = {
            true, false, false, false
    };
    /**
     * A list of character
     */
    private Character[] characters;
    /**
     * A list of player
     */
    private Player[] players;


    public static void main(String[] argv) {
        new Gameboard().runOnce();
    }

    public Gameboard() {
        characters = new Character[NO_OF_CHARACTER];
        players = new Player[NO_OF_PLAYERS];

        for (int i = 0; i < NO_OF_CHARACTER; i++) {
            characters[i] = new Character(CHARACTER_NAMES[i]);
        }

        for (int i = 0; i < NO_OF_PLAYERS; i++) {
            players[i] = new Player(PLAYER_NAMES[i],characters);
        }
    }

    public void runOnce() {

        print();
        System.out.println("======= Placing stage ======= \n"
                + "Each player will take turns to place three characters on the board.\n"
                + "No character can be placed in the position 0 or 5 or 6 (Throne) at this stage.\n"
                + "A position is FULL when there are four characters placed there already.\n"
                + "The remaining character will be placed at the position 0.\n");

        placingStage();

        print();
        System.out.println("======= Playing stage ======= \n"
                + "Each player will take turn to move a character UP the board.\n"
                + "You cannot move a character that is been killed or its immediate upper position is full.\n"
                + "A voting will be trigger immediately when a character is moved to the Throne (position 6).");

        playingStage();

        print();
        System.out.println("======= Scoring stage ======= \n"
                + "This will trigger if and only if the voting result is ALL positive, i.e., no player play the veto (reject) card. \n"
                + "The score of each player is computed by the secret list of characters owned by each player.");

        scoringStage();
    }

    private void scoringStage() {
        for (Player p : players) {
            System.out.println(p);
            System.out.println("Score: " + p.getScore());
        }
    }

    private void placingStage() {
        //loop until 12 of the characters have been placed
        //and place the last character at position 0
        Scanner in = new Scanner(System.in);
        int charIndex;
        int charInSameFloor;
        int level;
        String userChar;
        for (int i = 0; i < 3; i++) { /* loop 3 times because 12 characters will be placed in 3 times and the last
                                         remaining character can be placed in position 0 automatically */
            for (int j = 0; j < 4; j++) { // loop 4 times as there are 4 players in the game
                print();
                System.out.println(players[j] + ",this is your turn to place a character");
                if (HUMAN_PLAYERS[j]) {
                    boolean unplacedChar = false;
                    do { // loop until a valid character name is entered by the user
                        System.out.println("Please pick a character");
                        userChar = in.next();
                        for (charIndex = 0; charIndex < NO_OF_CHARACTER; charIndex++) {
                            if (userChar.equals(characters[charIndex].getName())
                                    && characters[charIndex].getPosition() == -1) {
                                unplacedChar = true;
                                break;
                            }
                        }
                    } while (!unplacedChar);
                    do { // loop until a valid level number is entered by the user
                        charInSameFloor = 0;
                        System.out.println("Please enter the floor you want to place " + userChar);
                        level = in.nextInt();
                        for (int c = 0; c < NO_OF_CHARACTER; c++) {
                            if (characters[c].getPosition() == level) {
                                charInSameFloor++;
                            }
                        }
                    } while (level < 1 || level > 4 || charInSameFloor == FULL);

                    characters[charIndex].setPosition(level);

                } else {
                    players[j].placeRandomly(characters);//using placeRandomly method for computer player
                }
            }
        }
        for (charIndex = 0; charIndex < NO_OF_CHARACTER; charIndex++) {
            if (characters[charIndex].getPosition() == -1) {
                characters[charIndex].setPosition(0); //placing the remaining character in position 0
                break;
            }
        }
    }

    private void playingStage() {
        //loop until a character has been voted for the new King.
        boolean kingSelected = false;
        int charListIndex;
        Character computerChar;
        String inputChar;
        Scanner in = new Scanner(System.in);
        print();
        do {
            for (int i = 0; i < NO_OF_PLAYERS; i++) {
                System.out.println(players[i]);
                System.out.println("This is your turn to move a character up");
                if (HUMAN_PLAYERS[i]) {
                    do {
                        System.out.println("Please type the character that you want to move");
                        inputChar = in.next();
                    } while (players[i].pickCharToMove(characters,inputChar) == null);

                } else {
                    computerChar = players[i].pickCharToMoveSmartly(characters);
                    inputChar = computerChar.getName();
                }
                //Setting the position of the character to one level above its recent position
                for (charListIndex = 0; charListIndex < NO_OF_CHARACTER; charListIndex++) {
                    if (inputChar.equals(characters[charListIndex].getName())) {
                        characters[charListIndex].setPosition(characters[charListIndex].getPosition() + 1);
                        break;
                    }
                }

                print();

                if (characters[charListIndex].getPosition() == THRONE) {
                    boolean kingSelect = true;
                    for (int j = 0; j < 4; j++) { /* loop 4 times because there are 4 players in the game and each of
                                                     them has to vote */
                        if (HUMAN_PLAYERS[j]) {
                            System.out.println("Please vote. Type V for veto. Other for accept");
                            if (in.next().equals("V")) {
                                if (!players[j].vote(false)) {
                                    characters[charListIndex].setPosition(-1);
                                    kingSelect = false;
                                }
                            }
                        } else {
                            if (!players[j].voteSmartly(characters[charListIndex])) {
                                characters[charListIndex].setPosition(-1);
                                kingSelect = false;
                            }
                        }
                    }
                    print();
                    if (kingSelect) {
                        kingSelected = true;
                        break;
                    }
                }
            }
        } while (!kingSelected);
    }

    private void print() {
        int level = 6;
        for (int j = 0; j <= 6; j++) { // loop to print levels from 6 to 0
            System.out.print("Level " + level + ":\t\t");
            for (Character character : characters) { // loop to print character names in each level
                if (character.getPosition() == level) {
                    System.out.print(character.getName() + "\t");
                }
            }
            System.out.println();
            level--;
        }
        System.out.println("Unplaced/Killed Characters");
        for (int i = 0; i < NO_OF_CHARACTER; i++) {
            if (characters[i].getPosition() == -1)
                System.out.print(characters[i] + "\t");
        }
        System.out.println("\n");

    }

}
