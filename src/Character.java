
public class Character {
    public static final int OUT_OF_GAME = -1;
    private String name;
    private int position;

    public Character(String name) {
        this.name = name;
        this.position = OUT_OF_GAME;
    }

    public int getPosition() {
        return this.position;
    }

    public boolean setPosition(int pos) {
        if (pos - this.getPosition() == 1 || this.getPosition() == OUT_OF_GAME ||
                this.getPosition() == Gameboard.THRONE) {
            this.position = pos;
            return true;
        }
        return false;
    }

    public String toString() {
        return this.name + "[" + this.position + "]";
    }

    public String getName() {
        return this.name;
    }

}
