package ut.com.isroot.stash.plugin.errors;

import com.isroot.stash.plugin.errors.YaccError;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Sean Ford
 * @since 2015-04-26
 */
public class YaccErrorTest {

    @Test
    public void testConstructor_defaultTypeIsOther() {
        YaccError error = new YaccError("error");

        assertThat(error.getType()).isEqualTo(YaccError.Type.OTHER);
    }

    @Test
    public void testPrependText() {
        YaccError error = new YaccError("my error").prependText("prepended text");

        assertThat(error.getMessage()).isEqualTo("prepended text: my error");
    }

    @Test
    public void testPrependText_typeIsPreserved() {
        YaccError error = new YaccError(YaccError.Type.BRANCH_NAME, "my error")
                .prependText("prepended text");

        assertThat(error.getType()).isEqualTo(YaccError.Type.BRANCH_NAME);
    }

    @Test
    public void testGetMessage() {
        YaccError error = new YaccError("my error");

        assertThat(error.getMessage()).isEqualTo("my error");
    }
}