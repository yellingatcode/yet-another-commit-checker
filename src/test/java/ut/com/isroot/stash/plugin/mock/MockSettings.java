package ut.com.isroot.stash.plugin.mock;

import com.atlassian.stash.setting.Settings;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Sean Ford
 * @since 2015-09-14
 */
public class MockSettings implements Settings {
    private final Map<String, Object> values;

    public MockSettings(Map<String, Object> values) {
        this.values = ImmutableMap.copyOf(values);
    }

    @SuppressWarnings("unchecked")
    private <T> T get(String key) {
        checkNotNull(key, "key");
        return (T) values.get(key);
    }

    @Nullable
    @Override
    public String getString(String key) {
        return get(key);
    }

    @Nonnull
    @Override
    public String getString(String key, String defaultValue) {
        return Objects.firstNonNull(getString(key), defaultValue);
    }

    @Nullable
    @Override
    public Boolean getBoolean(String s) {
        return null;
    }

    @Override
    public boolean getBoolean(String s, boolean b) {
        return false;
    }

    @Nullable
    @Override
    public Integer getInt(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(String key, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Long getLong(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLong(String key, long defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Double getDouble(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> asMap() {
        return values;
    }
}
