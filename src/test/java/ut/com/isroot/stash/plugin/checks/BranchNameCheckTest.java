package ut.com.isroot.stash.plugin.checks;

import com.atlassian.bitbucket.setting.Settings;
import com.isroot.stash.plugin.checks.BranchNameCheck;
import com.isroot.stash.plugin.errors.YaccError;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Sean Ford
 * @since 2015-04-25
 */
public class BranchNameCheckTest {
    @Test
    public void testCheck_noErrorIfSettingIsNull() {
        List<YaccError> errors = new BranchNameCheck(getSettings(null), "ref/heads/foo").check();

        assertThat(errors).isEmpty();
    }

    @Test
    public void testCheck_noErrorIfSettingIsEmpty() {
        List<YaccError> errors = new BranchNameCheck(getSettings(""), "ref/heads/foo").check();

        assertThat(errors).isEmpty();
    }

    @Test
    public void testCheck_errorIfBranchNameDoesNotMatchRegex() {
        List<YaccError> errors = new BranchNameCheck(getSettings("foo"), "refs/heads/bar").check();

        assertThat(errors)
                .containsOnly(new YaccError(YaccError.Type.BRANCH_NAME,
                        "Invalid branch name. 'bar' does not match regex 'foo'"));
    }

    @Test
    public void testCheck_noErrorIfBranchNameMatchesRegex() {
        List<YaccError> errors = new BranchNameCheck(getSettings(".*"), "refs/heads/foo").check();

        assertThat(errors).isEmpty();
    }

    @Test
    public void testCheck_nonBranchRefIdsAreIgnored() {
        List<YaccError> errors = new BranchNameCheck(getSettings("foo"), "refs/tags/bar").check();

        assertThat(errors).isEmpty();
    }

    private Settings getSettings(String branchNameRegex) {
        Settings settings = mock(Settings.class);

        when(settings.getString("branchNameRegex")).thenReturn(branchNameRegex);

        return settings;
    }
}