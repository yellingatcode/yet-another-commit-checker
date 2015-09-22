package it.com.isroot.stash.plugin;

import com.atlassian.stash.async.AsyncTestUtils;
import com.atlassian.stash.async.WaitCondition;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import com.atlassian.webdriver.stash.StashTestedProduct;
import com.atlassian.webdriver.stash.page.StashLoginPage;
import org.hamcrest.Description;

/**
 * @author Sean Ford
 * @since 2015-09-21
 */
public class YaccTestUtils {

    /**
     * Reset YACC settings data so that tests can start at a clean slate.
     * This is sort of hackish... maybe the Atlassian integration test
     * API has a way to reset all the data?
     */
    public static void resetData(StashTestedProduct stash) {
        YaccRepoSettingsPage settingsPage = stash.visit(StashLoginPage.class)
                .loginAsSysAdmin(YaccRepoSettingsPage.class);

        settingsPage.clickEditYacc()
                .clearSettings();
        settingsPage.clickSubmit();
        settingsPage.clickDisable();

        YaccGlobalSettingsPage globalSettingsPage = stash.visit(YaccGlobalSettingsPage.class);
        globalSettingsPage.clearSettings();
        globalSettingsPage.clickSubmit();

        stash.getTester().getDriver().manage().deleteAllCookies();
    }

    public static void waitForStashToBoot(final WebDriverTester tester) {
        AsyncTestUtils.waitFor(new WaitCondition() {
            @Override
            public boolean test() throws Exception {
                tester.gotoUrl(System.getProperty("http.stash.url") + "/status");
                return tester.getDriver().getPageSource().contains("RUNNING");
            }

            @Override
            public void describeFailure(Description description) throws Exception {

            }
        }, 600000, 5000);
    }

}
