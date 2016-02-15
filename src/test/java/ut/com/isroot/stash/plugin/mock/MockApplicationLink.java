package ut.com.isroot.stash.plugin.mock;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.auth.AuthenticationProvider;

import java.net.URI;

/**
 * @author Sean Ford
 * @since 2016-02-14
 */
public class MockApplicationLink implements ApplicationLink {
    private String name;

    public MockApplicationLink(String name) {
        this.name = name;
    }

    @Override
    public Object getProperty(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object putProperty(String s, Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object removeProperty(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationId getId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationType getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getDisplayUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getRpcUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPrimary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSystem() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequestFactory createAuthenticatedRequestFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequestFactory createAuthenticatedRequestFactory(Class<? extends AuthenticationProvider> aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequestFactory createImpersonatingAuthenticatedRequestFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequestFactory createNonImpersonatingAuthenticatedRequestFactory() {
        throw new UnsupportedOperationException();
    }
}
