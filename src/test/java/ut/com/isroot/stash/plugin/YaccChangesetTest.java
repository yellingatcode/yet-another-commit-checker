package ut.com.isroot.stash.plugin;

import com.isroot.stash.plugin.YaccChangeset;
import com.isroot.stash.plugin.YaccPerson;
import org.junit.Test;
import static org.fest.assertions.api.Assertions.assertThat;


/**
 * @author Sean Ford
 * @since 2014-05-01
 */
public class YaccChangesetTest
{
    @Test
    public void testConstructor_trailingNewLineInCommitMessageIsRemoved()
    {
        YaccChangeset yaccChangeset = new YaccChangeset("id", new YaccPerson("Name", "email@address.com"),
                "contains trailing newline\n", 0);

        assertThat(yaccChangeset.getMessage()).isEqualTo("contains trailing newline");

    }
}
