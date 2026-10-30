/** กำหนดโมดูล JavaFX และการเปิดแพ็กเกจให้ระบบโหลดหน้าจอ FXML */
module se233.gamedev {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;

    opens se233.gamedev.controller to javafx.fxml;
    opens se233.gamedev to javafx.fxml;

    exports se233.gamedev.controller;
    exports se233.gamedev;
    exports se233.gamedev.model;
    exports se233.gamedev.view;
    exports se233.gamedev.exception;
    exports se233.gamedev.util;
}