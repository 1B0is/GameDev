package se233.gamedev.model;
import java.util.Objects;
public final class Upgrade {
    private final UpgradeType type;
    public Upgrade(UpgradeType type){this.type=Objects.requireNonNull(type,"type");}
    public UpgradeType getType(){return type;}
    public String getName(){return type.getDisplayName();}
    public String getDescription(){return type.getDescription();}
    @Override public boolean equals(Object other){return other instanceof Upgrade u&&u.type==type;}
    @Override public int hashCode(){return type.hashCode();}
    @Override public String toString(){return getName()+" - "+getDescription();}
}
