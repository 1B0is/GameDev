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
import java.util.Optional;
/** จัดการคะแนน ชีวิต สถานะการเล่น ความคืบหน้าด่าน และการเลือกอัปเกรดตลอดหนึ่งรอบเกม */
public final class GameSession {
    public static final int DEFAULT_STAGE_COUNT=5;
    public static final int MAX_UPGRADE_REROLLS=3;
    public static final double WEATHER_CHANCE_PER_STAGE=0.8;
    private static final int COMBO_STEP=3;
    private static final int MAX_COMBO_STEPS=5;
    private static final double COMBO_BONUS_PER_STEP=0.10;
    private final int totalStages;
    private final Random random;
    private final Set<UpgradeType> acquired=EnumSet.noneOf(UpgradeType.class);
    private int score;
    private double lives;
    private int comboStreak;
    private double paddleWidthMultiplier=1.0,scoreMultiplier=1.0,ballSpeedMultiplier=1.0;
    private int rerollsRemaining=MAX_UPGRADE_REROLLS;
    private int additionalBallCount;
    private int stageNumber=1;
    private GameState state=GameState.READY;
    private List<Upgrade> offered=List.of();
    private Stage stage;
    private WeatherEvent weatherEvent;
    /** สร้างรอบเกมด้วยจำนวนชีวิตเริ่มต้นและจำนวนด่านมาตรฐาน */
    public GameSession(){this(3,DEFAULT_STAGE_COUNT,new Random());}
    /** สร้างรอบเกมโดยกำหนดจำนวนชีวิตและด่าน พร้อมสุ่มอัปเกรดแบบทั่วไป */
    public GameSession(int startingLives,int totalStages){this(startingLives,totalStages,new Random());}
    /** สร้างรอบเกมพร้อมตัวสุ่มที่กำหนด เพื่อให้ทดสอบผลการสุ่มซ้ำได้ */
    public GameSession(int startingLives,int totalStages,Random random){
        if(startingLives<1)throw new IllegalArgumentException("Starting lives must be positive");
        if(totalStages<1)throw new IllegalArgumentException("Total stages must be positive");
        this.lives=startingLives;this.totalStages=totalStages;this.random=random;
        this.stage=new Stage(1,totalStages,random);
    }
    /** เปลี่ยนสถานะจากเตรียมพร้อมเป็นกำลังเล่น */
    public void start() throws GameException {
        requireState(GameState.READY);state=GameState.PLAYING;
        rollWeatherForStage();
        GameLogger.gameEvent("Game started: lives="+lives+", stages="+totalStages);
    }
    /** เพิ่มคะแนนตามตัวคูณอัปเกรดและคืนจำนวนคะแนนที่ได้รับจริง */
    public int addScore(int basePoints) throws GameException {
        requireState(GameState.PLAYING);
        if(basePoints<0)throw new GameException("Score cannot be negative");
        double weatherMultiplier = weatherEvent == null ? 1.0 : weatherEvent.getScoreMultiplier();
        int earned=(int)Math.round(basePoints*scoreMultiplier*getComboMultiplier()*weatherMultiplier);
        score=Math.addExact(score,earned);GameLogger.scoreEvent(earned,score);return earned;
    }
    /** เพิ่มคอมโบเมื่อบอลชนอิฐ โดยใช้เป็นตัวคูณกับคะแนนที่ได้ครั้งถัดไป */
    public void recordBrickHit() {
        comboStreak++;
    }

    /** รีเซ็ตคอมโบเมื่อบอลกลับมาโดนไม้พาย */
    public void resetCombo() {
        comboStreak=0;
    }

    /** ลดจำนวนชีวิต และเปลี่ยนเป็นสถานะแพ้เมื่อชีวิตหมด */
    public void loseLife() throws GameException {
        requireState(GameState.PLAYING);
        lives=Math.max(0,lives-1);
        comboStreak=0;
        GameLogger.warning("Player lost a life; remaining="+lives);
        if(lives<=0){state=GameState.GAME_OVER;endWeather();GameLogger.gameEvent("Game over; final score="+score);}
    }

    /** ลดชีวิตครึ่งหน่วยเมื่อถูกอุกกาบาต โดยยังใช้กฎเกมโอเวอร์เดียวกับชีวิตเต็ม */
    public void loseHalfLife() throws GameException {
        requireState(GameState.PLAYING);
        lives=Math.max(0,lives-0.5);
        comboStreak=0;
        GameLogger.warning("Meteor hit; player lost half a life; remaining="+lives);
        if(lives<=0){state=GameState.GAME_OVER;endWeather();GameLogger.gameEvent("Game over; final score="+score);}
    }
    /** จบด่านปัจจุบัน แล้วเปิดตัวเลือกอัปเกรดหรือจบเกมด้วยชัยชนะ */
    public List<Upgrade> clearStage() throws GameException {
        requireState(GameState.PLAYING);
        comboStreak=0;
        endWeather();
        if(!stage.isFinalStage()){
            offered=generateUpgradeChoices();state=GameState.UPGRADE;
            GameLogger.gameEvent("Stage "+stageNumber+" cleared; upgrade choice opened");
            return offered;
        }
        state=GameState.VICTORY;GameLogger.gameEvent("All stages cleared; final score="+score);return List.of();
    }
    /** สุ่มชุดอัปเกรดใหม่โดยหักโควตาการสุ่มซ้ำหนึ่งครั้ง */
    public List<Upgrade> rerollUpgrades() throws GameException {
        requireState(GameState.UPGRADE);
        if(rerollsRemaining == 0)throw new UpgradeException("No upgrade rerolls remaining");
        rerollsRemaining--;
        offered=generateUpgradeChoices(offered.stream().map(Upgrade::getType).collect(
                java.util.stream.Collectors.toSet()));
        GameLogger.playerAction("Rerolled upgrade choices; remaining="+rerollsRemaining);
        return offered;
    }
    /** ใช้อัปเกรดตามลำดับที่เลือก แล้วเตรียมด่านถัดไป */
    public void chooseUpgrade(int choiceIndex) throws GameException {
        requireState(GameState.UPGRADE);
        if(choiceIndex<0||choiceIndex>=offered.size())throw new UpgradeException("Upgrade choice is outside the offered list");
        apply(offered.get(choiceIndex).getType());
        stageNumber++;
        try{stage=new Stage(stageNumber,totalStages,random);}catch(IllegalArgumentException ex){throw new InvalidStageException(ex.getMessage());}
        offered=List.of();state=GameState.PLAYING;
        rollWeatherForStage();
        GameLogger.gameEvent("Upgrade applied; advanced to stage "+stageNumber);
    }
    /** สร้างตัวเลือกอัปเกรดโดยไม่มีชนิดที่ต้องกันออกเพิ่มเติม */
    private List<Upgrade> generateUpgradeChoices(){
        return generateUpgradeChoices(Set.of());
    }
    /** สุ่มตัวเลือกที่ไม่ซ้ำและยังไม่เคยได้รับ โดยเว้นชนิดที่ระบุไว้ */
    private List<Upgrade> generateUpgradeChoices(Set<UpgradeType> exclude){
        List<UpgradeType> types=new ArrayList<>(List.of(UpgradeType.values()));
        types.removeIf(type -> acquired.contains(type) || exclude.contains(type));
        if(types.size()<Math.min(3, UpgradeType.values().length - acquired.size())){
            types=new ArrayList<>(List.of(UpgradeType.values()));
            types.removeIf(acquired::contains);
        }
        Collections.shuffle(types,random);
        return types.subList(0,Math.min(3,types.size())).stream().map(Upgrade::new).toList();
    }
    /** เพิ่มอัปเกรดที่เลือกลงในรอบเกมและปรับค่าความสามารถที่เกี่ยวข้อง */
    private void apply(UpgradeType type) throws UpgradeException {
        if(!acquired.add(type))throw new UpgradeException("Upgrade already acquired: "+type.getDisplayName());
        switch(type){
            case WIDE_PADDLE -> paddleWidthMultiplier*=type.getMultiplier();
            case EXTRA_LIFE -> lives++;
            case POWER_BALL -> scoreMultiplier*=type.getMultiplier();
            case QUICK_BALL -> ballSpeedMultiplier*=type.getMultiplier();
            case MULTI_BALL -> additionalBallCount++;
            case PIERCING_BALL -> { }
        }
        GameLogger.playerAction("Selected upgrade: "+type.getDisplayName());
    }
    /** ตรวจสอบว่าสถานะเกมตรงกับสถานะที่เมธอดต้องการ */
    private void requireState(GameState expected) throws GameException {
        if(state!=expected)throw new GameException("Expected "+expected+" state but game is "+state);
    }
    /** สุ่มสภาพอากาศของด่านปัจจุบันและบันทึกเหตุการณ์ที่เริ่ม */
    private void rollWeatherForStage() {
        weatherEvent=WeatherEvent.roll(random,WEATHER_CHANCE_PER_STAGE).orElse(null);
        if(weatherEvent!=null) {
            GameLogger.gameEvent("Weather started: "+weatherEvent.getType()+" on stage "+stageNumber);
        } else {
            GameLogger.gameEvent("No weather event on stage "+stageNumber);
        }
    }

    /** ปิดสภาพอากาศของด่านและบันทึกเหตุการณ์สิ้นสุด */
    private void endWeather() {
        if(weatherEvent!=null&&weatherEvent.isActive()) {
            weatherEvent.end();
            GameLogger.gameEvent("Weather ended: "+weatherEvent.getType()+" on stage "+stageNumber);
        }
    }
    public int getComboStreak(){return comboStreak;}
    public double getComboMultiplier(){
        int comboSteps=Math.min(comboStreak/COMBO_STEP,MAX_COMBO_STEPS);
        return 1.0+comboSteps*COMBO_BONUS_PER_STEP;
    }
    public int getScore(){return score;}
    public double getLives(){return lives;}
    public int getStageNumber(){return stageNumber;}
    public int getTotalStages(){return totalStages;}
    public Stage getStage(){return stage;}
    public GameState getState(){return state;}
    public List<Upgrade> getOfferedUpgrades(){return offered;}
    public Set<UpgradeType> getAcquiredUpgrades(){return Collections.unmodifiableSet(acquired);}
    public double getPaddleWidthMultiplier(){return paddleWidthMultiplier;}
    public double getScoreMultiplier(){return scoreMultiplier;}
    public double getBallSpeedMultiplier(){return ballSpeedMultiplier;}
    public int getRerollsRemaining(){return rerollsRemaining;}
    public int getAdditionalBallCount(){return additionalBallCount;}
    public boolean hasUpgrade(UpgradeType type){return acquired.contains(type);}
    public Optional<WeatherEvent> getWeatherEvent(){return Optional.ofNullable(weatherEvent);}
    public double getWeatherScoreMultiplier(){
        return weatherEvent==null?1.0:weatherEvent.getScoreMultiplier();
    }
}
