package ut.com.isroot.stash.plugin.checks;

import com.atlassian.stash.setting.Settings;
import com.isroot.stash.plugin.checks.BranchNameCheck;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Sean Ford
 * @since 2015-04-25
 */
public class BranchNameCheckTest {
    @Test
    public void testCheck_noErrorIfSettingIsNull() {
        List<String> errors = new BranchNameCheck(getSettings(null), "ref/heads/foo").check();

        assertThat(errors).isEmpty();
    }

    @Test
    public void testCheck_noErrorIfSettingIsEmpty() {
        List<String> errors = new BranchNameCheck(getSettings(""), "ref/heads/foo").check();

        assertThat(errors).isEmpty();
    }

    @Test
    public void testCheck_errorIfBranchNameDoesNotMatchRegex() {
        List<String> errors = new BranchNameCheck(getSettings("foo"), "refs/heads/bar").check();

        assertThat(errors)
                .containsExactly("refs/heads/bar: Invalid branch name. 'bar' does not match regex 'foo'");
    }

    @Test
    public void testCheck_noErrorIfBranchNameMatchesRegex() {
        List<String> errors = new BranchNameCheck(getSettings(".*"), "refs/heads/foo").check();

        assertThat(errors).isEmpty();
    }

    @Test
    public void testCheck_nonBranchRefIdsAreIgnored() {
        List<String> errors = new BranchNameCheck(getSettings("foo"), "refs/tags/bar").check();

        assertThat(errors).isEmpty();
    }

    private Settings getSettings(String branchNameRegex) {
        Settings settings = mock(Settings.class);

        when(settings.getString("branchNameRegex")).thenReturn(branchNameRegex);

        return settings;
    }
}