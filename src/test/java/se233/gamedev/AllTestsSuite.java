package se233.gamedev;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/** รวมการทดสอบ JUnit ทั้งหมดในแพ็กเกจเกมไว้ในชุดทดสอบเดียว */
@Suite
@SelectPackages("se233.gamedev")
public class AllTestsSuite {
}
