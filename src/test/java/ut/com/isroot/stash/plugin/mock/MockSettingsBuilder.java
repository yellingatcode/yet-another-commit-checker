package ut.com.isroot.stash.plugin.mock;

import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsBuilder;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author Sean Ford
 * @since 2015-09-14
 */
public class MockSettingsBuilder implements SettingsBuilder {
    private final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

    @Nonnull
    @Override
    public SettingsBuilder add(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public SettingsBuilder add(String s, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public SettingsBuilder add(String s, int i) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public SettingsBuilder add(String s, long l) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public SettingsBuilder add(String s, double v) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public SettingsBuilder addAll(Map<String, ?> map) {
        builder.putAll(map);
        return this;
    }

    @Nonnull
    @Override
    public SettingsBuilder addAll(Settings settings) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Settings build() {
        return new MockSettings(builder.build());
    }
}
