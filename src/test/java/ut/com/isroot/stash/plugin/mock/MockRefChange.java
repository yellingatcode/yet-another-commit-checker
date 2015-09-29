package ut.com.isroot.stash.plugin.mock;

import com.atlassian.bitbucket.repository.MinimalRef;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.repository.RefType;

import javax.annotation.Nonnull;

/**
 * @author Sean Ford
 * @since 2015-09-27
 */
public class MockRefChange implements RefChange {
    private String refId;
    private String fromHash;
    private String toHash;
    private RefChangeType type;

    public MockRefChange() {
        this.refId = "refs/heads/master";
        this.type = RefChangeType.UPDATE;
    }

    public MockRefChange(String refId) {
        this.refId = refId;
    }

    @Nonnull
    @Override
    public MinimalRef getRef() {
        return new MinimalRef() {
            @Nonnull
            @Override
            public String getDisplayId() {
                throw new UnsupportedOperationException();
            }

            @Nonnull
            @Override
            public String getId() {
                return refId;
            }

            @Nonnull
            @Override
            public RefType getType() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Nonnull
    @Override
    public String getRefId() {
        return refId;
    }

    @Nonnull
    @Override
    public String getFromHash() {
        return fromHash;
    }

    @Nonnull
    @Override
    public String getToHash() {
        return toHash;
    }

    @Nonnull
    @Override
    public RefChangeType getType() {
        return type;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public void setFromHash(String fromHash) {
        this.fromHash = fromHash;
    }

    public void setToHash(String toHash) {
        this.toHash = toHash;
    }

    public void setType(RefChangeType type) {
        this.type = type;
    }
}
