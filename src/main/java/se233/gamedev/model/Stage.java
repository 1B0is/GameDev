package se233.gamedev.model;
/** Stage progression data used by the game loop and stage HUD. */
public final class Stage {
    private final int number;
    private final int totalStages;
    private final int brickGoal;
    private int bricksDestroyed;
    public Stage(int number,int totalStages){
        if(totalStages<1||number<1||number>totalStages)throw new IllegalArgumentException("Stage number must be within 1..totalStages");
        this.number=number;this.totalStages=totalStages;this.brickGoal=10+(number-1)*2;
    }
    public int getNumber(){return number;}
    public int getTotalStages(){return totalStages;}
    public int getBrickGoal(){return brickGoal;}
    public int getBricksDestroyed(){return bricksDestroyed;}
    public int getBricksRemaining(){return Math.max(0,brickGoal-bricksDestroyed);}
    public boolean recordBrickDestroyed(){if(bricksDestroyed>=brickGoal)return false;bricksDestroyed++;return true;}
    public boolean isCleared(){return bricksDestroyed>=brickGoal;}
    public boolean isFinalStage(){return number==totalStages;}
}
