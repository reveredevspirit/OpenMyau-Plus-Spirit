package myau.module.modules;

import myau.module.Module;
import myau.module.Category;
import myau.property.Property;
import java.awt.Color;

public class HUD extends Module {

    public final Property<Boolean> toggleSound = new Property<>("ToggleSound", true);
    public final Property<Integer> red = new Property<>("Red", 85);
    public final Property<Integer> green = new Property<>("Green", 170);
    public final Property<Integer> blue = new Property<>("Blue", 255);

    public HUD() {
        super("HUD", Category.RENDER);
        this.addProperties(toggleSound, red, green, blue);
    }

    public Color getColor(long time) {
        return new Color(red.getValue(), green.getValue(), blue.getValue());
    }

    public Color getColor(long time, float offset) {
        return getColor(time);
    }
}
