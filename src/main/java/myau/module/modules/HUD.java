package myau.module.modules;

import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;

import java.awt.Color;

public class HUD extends Module {

    // sound toggle used in Module#toggle()
    public final BooleanProperty toggleSound =
            new BooleanProperty("ToggleSound", true);

    // used in ModuleManager when deciding to show chat alerts
    public final BooleanProperty toggleAlerts =
            new BooleanProperty("ToggleAlerts", true);

    // used by Radar, Tracers, Scaffold, BedNuker
    public final FloatProperty scale =
            new FloatProperty("Scale", 1.0f, 0.5f, 3.0f);

    // used by Radar / BedNuker / Scaffold for text shadow
    public final BooleanProperty shadow =
            new BooleanProperty("Shadow", true);

    // RGB used by ArraylistHUD / HUD color
    public final IntProperty red =
            new IntProperty("Red", 85, 0, 255);
    public final IntProperty green =
            new IntProperty("Green", 170, 0, 255);
    public final IntProperty blue =
            new IntProperty("Blue", 255, 0, 255);

    public HUD() {
        // your Module constructor is (String name, boolean enabled[, boolean hidden])
        // there is NO Category in Module, so we just pass a boolean
        super("HUD", true);
    }

    public Color getColor(long time) {
        return new Color(red.getValue(), green.getValue(), blue.getValue());
    }

    public Color getColor(long time, float offset) {
        return getColor(time);
    }
}
