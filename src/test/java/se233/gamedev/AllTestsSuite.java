package se233.gamedev;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/** Runs all JUnit tests under the game package as one explicit suite. */
@Suite
@SelectPackages("se233.gamedev")
public class AllTestsSuite {
}
