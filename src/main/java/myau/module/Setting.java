package myau.module;

public abstract class Setting {
    protected final String name;

    public Setting(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
