module se233.gamedev {
    requires javafx.controls;
    requires javafx.fxml;


    opens se233.gamedev to javafx.fxml;
    exports se233.gamedev;
}