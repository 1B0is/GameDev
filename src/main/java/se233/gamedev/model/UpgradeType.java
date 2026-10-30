package se233.gamedev.model;
public enum UpgradeType {
    WIDE_PADDLE("Wide Paddle","Paddle width +20%",1.20),
    EXTRA_LIFE("Extra Life","Gain one life",1.0),
    POWER_BALL("Power Ball","Brick score +25%",1.25),
    QUICK_BALL("Quick Ball","Ball speed +10%",1.10);
    private final String displayName,description;
    private final double multiplier;
    UpgradeType(String name,String description,double multiplier){this.displayName=name;this.description=description;this.multiplier=multiplier;}
    public String getDisplayName(){return displayName;}
    public String getDescription(){return description;}
    public double getMultiplier(){return multiplier;}
}
