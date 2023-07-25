package models.signals.device.lights;

public class BrightnessSignal {

    public static final int MAX_BRIGHTNESS = 255;
    public static final int MIN_BRIGHTNESS = 0;

    public BrightnessSignal(int brightness) {
        setBrightness(brightness);
    }

    private int brightness;

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        if (brightness > MAX_BRIGHTNESS) {
            brightness = MAX_BRIGHTNESS;
        } else if (brightness < MIN_BRIGHTNESS) {
            brightness = MIN_BRIGHTNESS;
        }
        this.brightness = brightness;
    }
}
