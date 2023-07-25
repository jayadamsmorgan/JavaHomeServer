package models.signals.device.lights;

public class RGBWSignal extends RGBSignal {

    private int white;

    public RGBWSignal(int red, int green, int blue, int white) {
        super(red, green, blue);
        setWhite(white);
    }


    public int getWhite() {
        return white;
    }

    public void setWhite(int white) {
        if (white > MAX_COLOR)
            white = MAX_COLOR;
        else if (white < MIN_COLOR)
            white = MIN_COLOR;
        this.white = white;
    }
}
