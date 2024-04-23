package name.lattuada.trading.tests.steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import name.lattuada.trading.tests.utils.CucumberLogAppender;

public class Hook {

    @Before
    public void beforeEachScenario(Scenario scenario) {
        CucumberLogAppender.clearLog();
    }

    @After
    public void afterEachScenario(Scenario scenario) {
        String logs = CucumberLogAppender.getLog();

        if (logs != null && !logs.isEmpty()) {
            scenario.log(logs);
        } else {
            scenario.log("No logs found for this scenario");
        }

        CucumberLogAppender.clearLog();
    }
}