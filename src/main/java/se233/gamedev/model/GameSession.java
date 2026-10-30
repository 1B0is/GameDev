package se233.gamedev.model;
import se233.gamedev.exception.GameException;
import se233.gamedev.exception.InvalidStageException;
import se233.gamedev.exception.UpgradeException;
import se233.gamedev.util.GameLogger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
/** Owns score, lives, run state and roguelike stage/upgrade progression. */
public final class GameSession {
    public static final int DEFAULT_STAGE_COUNT=5;
    private final int totalStages;
    private final Random random;
    private final Set<UpgradeType> acquired=EnumSet.noneOf(UpgradeType.class);
    private int score,lives;
    private double paddleWidthMultiplier=1.0,scoreMultiplier=1.0,ballSpeedMultiplier=1.0;
    private int stageNumber=1;
    private GameState state=GameState.READY;
    private List<Upgrade> offered=List.of();
    private Stage stage;
    public GameSession(){this(3,DEFAULT_STAGE_COUNT,new Random());}
    public GameSession(int startingLives,int totalStages){this(startingLives,totalStages,new Random());}
    public GameSession(int startingLives,int totalStages,Random random){
        if(startingLives<1)throw new IllegalArgumentException("Starting lives must be positive");
        if(totalStages<1)throw new IllegalArgumentException("Total stages must be positive");
        this.lives=startingLives;this.totalStages=totalStages;this.random=random;this.stage=new Stage(1,totalStages);
    }
    public void start() throws GameException {
        requireState(GameState.READY);state=GameState.PLAYING;
        GameLogger.gameEvent("Game started: lives="+lives+", stages="+totalStages);
    }
    /** Returns the score delta after applying persistent score upgrades. */
    public int addScore(int basePoints) throws GameException {
        requireState(GameState.PLAYING);
        if(basePoints<0)throw new GameException("Score cannot be negative");
        int earned=(int)Math.round(basePoints*scoreMultiplier);
        score=Math.addExact(score,earned);GameLogger.scoreEvent(earned,score);return earned;
    }
    public void loseLife() throws GameException {
        requireState(GameState.PLAYING);
        if(lives>0)lives--;
        GameLogger.warning("Player lost a life; remaining="+lives);
        if(lives==0){state=GameState.GAME_OVER;GameLogger.gameEvent("Game over; final score="+score);}
    }
    /** Call once after the board reports that every brick was cleared. */
    public List<Upgrade> clearStage() throws GameException {
        requireState(GameState.PLAYING);
        if(!stage.isFinalStage()){
            offered=generateUpgradeChoices();state=GameState.UPGRADE;
            GameLogger.gameEvent("Stage "+stageNumber+" cleared; upgrade choice opened");
            return offered;
        }
        state=GameState.VICTORY;GameLogger.gameEvent("All stages cleared; final score="+score);return List.of();
    }
    public void chooseUpgrade(int choiceIndex) throws GameException {
        requireState(GameState.UPGRADE);
        if(choiceIndex<0||choiceIndex>=offered.size())throw new UpgradeException("Upgrade choice is outside the offered list");
        apply(offered.get(choiceIndex).getType());
        stageNumber++;
        try{stage=new Stage(stageNumber,totalStages);}catch(IllegalArgumentException ex){throw new InvalidStageException(ex.getMessage());}
        offered=List.of();state=GameState.PLAYING;
        GameLogger.gameEvent("Upgrade applied; advanced to stage "+stageNumber);
    }
    private List<Upgrade> generateUpgradeChoices(){
        List<UpgradeType> types=new ArrayList<>(List.of(UpgradeType.values()));
        Collections.shuffle(types,random);
        return types.subList(0,Math.min(3,types.size())).stream().map(Upgrade::new).toList();
    }
    private void apply(UpgradeType type) throws UpgradeException {
        if(!acquired.add(type))throw new UpgradeException("Upgrade already acquired: "+type.getDisplayName());
        switch(type){
            case WIDE_PADDLE -> paddleWidthMultiplier*=type.getMultiplier();
            case EXTRA_LIFE -> lives++;
            case POWER_BALL -> scoreMultiplier*=type.getMultiplier();
            case QUICK_BALL -> ballSpeedMultiplier*=type.getMultiplier();
        }
        GameLogger.playerAction("Selected upgrade: "+type.getDisplayName());
    }
    private void requireState(GameState expected) throws GameException {
        if(state!=expected)throw new GameException("Expected "+expected+" state but game is "+state);
    }
    public int getScore(){return score;}
    public int getLives(){return lives;}
    public int getStageNumber(){return stageNumber;}
    public int getTotalStages(){return totalStages;}
    public Stage getStage(){return stage;}
    public GameState getState(){return state;}
    public List<Upgrade> getOfferedUpgrades(){return offered;}
    public Set<UpgradeType> getAcquiredUpgrades(){return Collections.unmodifiableSet(acquired);}
    public double getPaddleWidthMultiplier(){return paddleWidthMultiplier;}
    public double getScoreMultiplier(){return scoreMultiplier;}
    public double getBallSpeedMultiplier(){return ballSpeedMultiplier;}
}
