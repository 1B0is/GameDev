SE233 PROJECT II - ARKANOID ROGUELIKE

โปรเจกต์นี้เป็นเกม Arkanoid แบบ Roguelike ที่มีหลายด่านและระบบเลือกอัปเกรด

การเล่น:
- เริ่มเกมจากเมนู
- ใช้ปุ่มลูกศรหรือ A/D เพื่อเลื่อนไม้พาย
- กด Space เพื่อปล่อยลูกบอลขึ้นตรง
- เก็บไอเทมลำแสงด้วยไม้พายเพื่อโจมตีอิฐ

การสร้างและทดสอบ:
    mvnw.cmd clean test package

เปิดเกมจากไฟล์ JAR:
    java -jar target/arkanoid-roguelike-1.0-SNAPSHOT.jar

ภาพประกอบอยู่ใน:
    src/main/resources/se233/gamedev/images

รายการงานที่เสร็จแล้วและงานที่ยังเหลือ:
    C:\Users\LOQ\Downloads\2026_233_project_II_tracker.md
    C:\Users\LOQ\Downloads\SE233_ProjectII_Gameplay_and_Sprites_Update.md

    # Roguelike Arkanoid - Systems Layer
    Open this folder in IntelliJ IDEA as a Maven project. Java 17 is required; IntelliJ will import dependencies and test configuration from pom.xml.

    Included: GameSession facade; PlayerProfile score/lives and persistent upgrades; StageManager and UpgradeManager with three distinct random choices; one GameException handled centrally by GameErrorHandler; SLF4J/Logback levels (INFO progression, DEBUG scoring/choices, WARN ball lost, ERROR caught domain failure); JUnit 5 tests for score, lives/game over, stage progression, upgrade effects and invalid choices.

    Commands:
    mvn clean compile
    mvn test
    mvn package
    java -jar target/roguelike-arkanoid-1.0.0.jar

    Connect the game loop/UI to GameSession.recordBrickDestroyed, loseBall, clearStage and selectUpgrade. Display currentOffers(), stage(), player().score(), and player().lives().

