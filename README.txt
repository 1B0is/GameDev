PATCH FILES FOR D:\SE\Project\GameDev

Copy these paths into the existing project with the same relative paths:
- src/main/java/se233/gamedev/model/GameSession.java
- src/main/java/se233/gamedev/model/GameState.java
- src/main/java/se233/gamedev/model/Stage.java
- src/main/java/se233/gamedev/model/Upgrade.java
- src/main/java/se233/gamedev/model/UpgradeType.java
- src/main/java/se233/gamedev/exception/GameException.java
- src/main/java/se233/gamedev/exception/InvalidStageException.java
- src/main/java/se233/gamedev/exception/UpgradeException.java
- src/main/java/se233/gamedev/util/GameLogger.java
- src/main/java/se233/gamedev/util/GameErrorHandler.java
- src/test/java/se233/gamedev/model/GameSessionTest.java
- src/test/java/se233/gamedev/model/UpgradeSelectionTest.java

The existing pom.xml already covers Java 21, JavaFX, JUnit, compile/test/package lifecycle, runtime dependency copying and a launcher manifest. No pom.xml or module-info.java change is required; java.logging is already declared. Route game actions through GameErrorHandler.run(action) so all GameException subclasses are caught and logged at one boundary.
