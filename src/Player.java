
import java.util.concurrent.ThreadLocalRandom;

public class Player {
    /**
     * The length of the list
     */
    private static final int LIST_LENGTH = 6;
    /**
     * A secret list of a player. This list would not be changed during the game
     */
    private Character[] myList;
    /**
     * The number of veto (reject) voting card. Decrement it when the player vote
     * reject.
     */
    private int vetoCard;
    /**
     * The name of the player.
     */
    private String name;

    /**
     * Compute the score of a player. Each player should have a list of character
     *
     * @return the score of a player.
     */
    public int getScore() {
        int score = 0;
        for (int i = 0; i < LIST_LENGTH; i++) {
            if (myList[i].getPosition() != -1) {
                score += Gameboard.SCORES[myList[i].getPosition()]; /* Using scores calculation formula
                                                                      SCORES = {0, 1, 2, 3, 4, 5, 10} */
            }
        }
        return score;
    }

    public String getName() {
        return this.name;
    }

    public void initVetoCard(int card) {
        this.vetoCard = card;
    }

    public Player(String name, Character[] list) {
        //TODO
        this.name = name;
        initVetoCard(3); // Assigning 3 veto cards to each player
        int[] uniqueCharIndex = new int[LIST_LENGTH]; /* array variable to store index of the characters
                                                         that have already been assigned to myList */
        boolean uniqueChar; // to check if the character to be assigned to myList is unique or not
        int index;
        for (int i = 0; i < uniqueCharIndex.length; i++) { /* Initialising uniqueCharIndex with -1 because
                                                              there will be initially no values in myList */
            uniqueCharIndex[i] = -1;
        }
        myList = new Character[LIST_LENGTH];
        for (int i = 0; i < LIST_LENGTH; i++) {
            do {
                index = ThreadLocalRandom.current().nextInt(0,13);
                uniqueChar = true;
                for (int j: uniqueCharIndex) {
                    if (j == index ) { /* checking if the index of the character to be assigned to myList
                                          already exist in myList */
                        uniqueChar = false;
                        break;
                    }
                }
            } while (!uniqueChar);
            myList[i] = list[index];
            uniqueCharIndex[i] = index;
        }
    }

    public boolean vote(boolean support) {
        if (support || vetoCard == 0) {
            return true;
        }
        vetoCard--; // decrease the number of veto cards when the player vote reject
        return false;
    }

    public boolean voteRandomly() {
        int support = ThreadLocalRandom.current().nextInt(0,2);
        if (support == 0 || vetoCard == 0) {
            return true;
        }
        vetoCard--; // decrease the number of veto cards when the player vote reject
        return false;
    }

    public Character placeRandomly(Character[] list) {
        int charListIndex;
        int charInSameFloor;
        int level;
        do { // Loop until the character to be chosen is not placed yet
            charListIndex = ThreadLocalRandom.current().nextInt(0,13);
        } while (list[charListIndex].getPosition() != -1);

        do { // Loop until the level in which the character to be placed has less than 4 characters
            charInSameFloor = 0;
            level = ThreadLocalRandom.current().nextInt(1,5);
            for (int c = 0; c < Gameboard.NO_OF_CHARACTER; c++) {
                if (list[c].getPosition() == level) {
                    charInSameFloor++;
                }
            }
        } while (charInSameFloor == Gameboard.FULL);

        list[charListIndex].setPosition(level);
        return list[charListIndex];

    }

    public boolean voteSmartly(Character character) {
        for (int i = 0; i < LIST_LENGTH; i++) {
            if (myList[i].getName().equals(character.getName()) || vetoCard == 0) {
                return true;
            }
        }
        vetoCard--; // decrease the number of veto cards when the player vote reject
        return false;
    }

    public Character pickCharToMoveRandomly(Character[] list) {
        int noOfUpperPosChar;
        int charListIndex;
        do {
            noOfUpperPosChar = 0;
            charListIndex = ThreadLocalRandom.current().nextInt(0,13);
            if (list[charListIndex].getPosition() == -1) {
                noOfUpperPosChar = Gameboard.FULL; /* assigning noOfUpperPosChar the value of Gameboard.FULL
                                                      so that the loop continues to pick another charListIndex
                                                      as the character of the charListIndex that was picked has
                                                      already been killed */
            } else {
                for (int i = 0; i < Gameboard.NO_OF_CHARACTER; i++){
                    if (list[i].getPosition() == list[charListIndex].getPosition() + 1) {
                        noOfUpperPosChar++; /* counting the number of characters whose positions are one level above
                                               than that of the character that is supposed to be selected */
                    }
                }
            }
        } while (noOfUpperPosChar == Gameboard.FULL);

        return list[charListIndex];
    }

    public Character pickCharToMove(Character[] list, String name) {
        int noOfUpperPosChar = 0;
        boolean charFound = false;
        int charListIndex;
        for (charListIndex = 0; charListIndex < Gameboard.NO_OF_CHARACTER; charListIndex++){
            if (list[charListIndex].getName().equals(name)){
                charFound = true;
                if (list[charListIndex].getPosition() == -1) {
                    return null;
                } else {
                    for (int j = 0; j < Gameboard.NO_OF_CHARACTER; j++) {
                        if (list[j].getPosition() == list[charListIndex].getPosition() + 1) {
                            noOfUpperPosChar++; /* counting the number of characters whose positions are one level above
                                               than that of the character that is supposed to be selected */
                        }
                    }
                }
                break;
            }
        }
        if (!charFound || noOfUpperPosChar == Gameboard.FULL) {
            return null;
        }

        return list[charListIndex];
    }

    public Character pickCharToMoveSmartly(Character[] list) {
        /* The logic here is to choose a character to be moved based on the number of characters that have
           not been killed yet and if the character to be chosen is one of the characters from myList.
           If the character is from myList and the number of alive characters is more than 9 then move the characters
           that are in position 0 to 2. If the character is from my myList and the number of alive characters is less
           than or equal to 9 then move the characters that are in position 3 to 5. In this way, the characters in
           myList have a high chance of being selected as the King and not getting killed because lower number of alive
           characters indicate that other players have used most or all of their veto cards so, the character in
           throne position has high chance of being selected as the King.
         */
        int noOfUpperPosChar;
        boolean charToMoveSelected = false;
        boolean charInMyList;
        int charListIndex;
        int aliveChars = 0;

        for (int i = 0; i < Gameboard.NO_OF_CHARACTER; i++) { /* to check the total number of characters
                                                                 that have not been killed yet */
            if (list[i].getPosition() != -1)
                aliveChars += 1;
        }

        do {
            charInMyList = false;
            do {
                noOfUpperPosChar = 0;
                charListIndex = ThreadLocalRandom.current().nextInt(0,13);
                if (list[charListIndex].getPosition() == -1) {
                    noOfUpperPosChar = Gameboard.FULL;/* assigning noOfUpperPosChar the value of Gameboard.FULL
                                                      so that the loop continues to pick another charListIndex
                                                      as the character of the charListIndex that was picked has
                                                      already been killed */
                } else {
                    for (int i = 0; i < Gameboard.NO_OF_CHARACTER; i++){
                        if (list[i].getPosition() == list[charListIndex].getPosition() + 1) {
                            noOfUpperPosChar++;
                        }
                    }
                }
            } while (noOfUpperPosChar == Gameboard.FULL);

            for (int i = 0; i < LIST_LENGTH; i++) {
                if (list[charListIndex].getName().equals(list[i].getName())) {
                    charInMyList = true;
                    if (aliveChars > 9) {
                        if (list[charListIndex].getPosition() >= 0 && list[charListIndex].getPosition() <= 2) {
                            charToMoveSelected = true;
                            break;
                        }
                    } else {
                        if (list[charListIndex].getPosition() >= 3 && list[charListIndex].getPosition() <= 5 ) {
                            charToMoveSelected = true;
                            break;
                        }
                    }
                }
            }
            if (!charInMyList) {
                charToMoveSelected = true;
            }

        } while (!charToMoveSelected);

        return list[charListIndex];
    }

    public String toString() {
        String playerAndCharList = "";
        playerAndCharList += this.name + "\t\tVeto Card :" + this.vetoCard + "\n";
        for (int i = 0; i < LIST_LENGTH; i++) {
            playerAndCharList += myList[i] + "\t";
        }
        return playerAndCharList;
    }
}
