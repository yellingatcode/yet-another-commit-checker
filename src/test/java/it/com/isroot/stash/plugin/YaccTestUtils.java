package it.com.isroot.stash.plugin;

import com.atlassian.bitbucket.async.AsyncTestUtils;
import com.atlassian.bitbucket.async.WaitCondition;
import com.atlassian.webdriver.bitbucket.BitbucketTestedProduct;
import com.atlassian.webdriver.bitbucket.page.BitbucketLoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
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
    public static void resetData(BitbucketTestedProduct stash) {
        YaccRepoSettingsPage settingsPage = stash.visit(BitbucketLoginPage.class)
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
                tester.gotoUrl(System.getProperty("http.bitbucket.url") + "/status");
                return tester.getDriver().getPageSource().contains("RUNNING");
            }

            @Override
            public void describeFailure(Description description) throws Exception {

            }
        }, 600000, 5000);
    }

}
