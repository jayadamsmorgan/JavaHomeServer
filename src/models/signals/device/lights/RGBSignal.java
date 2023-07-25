package models.signals.device.lights;

public class RGBSignal {

    protected static final int MAX_COLOR = 255;
    protected static final int MIN_COLOR = 0;

    public RGBSignal(int red, int green, int blue) {
        setRed(red);
        setGreen(green);
        setBlue(blue);
    }

    private int red;
    private int green;
    private int blue;

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        if (red > MAX_COLOR)
            red = MAX_COLOR;
        else if (red < MIN_COLOR)
            red = MIN_COLOR;
        this.red = red;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        if (green > MAX_COLOR)
            green = MAX_COLOR;
        else if (green < MIN_COLOR) {
            green = MIN_COLOR;
        }
        this.green = green;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        if (blue > MAX_COLOR)
            blue = MAX_COLOR;
        else if (blue < MIN_COLOR)
            blue = MIN_COLOR;
        this.blue = blue;
    }
}
