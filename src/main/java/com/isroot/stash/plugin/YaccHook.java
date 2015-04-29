package com.isroot.stash.plugin;

import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.hook.repository.PreReceiveRepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.RefChangeType;
import com.atlassian.stash.setting.Settings;
import com.google.common.collect.Lists;
import com.isroot.stash.plugin.errors.YaccError;
import com.isroot.stash.plugin.errors.YaccErrorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * @author Sean Ford
 * @since 2013-05-11
 */
public final class YaccHook implements PreReceiveRepositoryHook {
    private static final Logger log = LoggerFactory.getLogger(YaccHook.class);

    private final YaccService yaccService;

    public YaccHook(YaccService yaccService) {
        this.yaccService = yaccService;
    }

    @Override
    public boolean onReceive(@Nonnull RepositoryHookContext repositoryHookContext,
                             @Nonnull Collection<RefChange> refChanges, @Nonnull HookResponse hookResponse) {
        List<YaccError> errors = Lists.newArrayList();
        Settings settings = repositoryHookContext.getSettings();

        for (RefChange rf : refChanges) {
            if (rf.getType() == RefChangeType.DELETE) {
                continue;
            }

            for(YaccError e : yaccService.checkRefChange(repositoryHookContext.getRepository(),
                    settings, rf)) {
                errors.add(e.prependText(rf.getRefId()));
            }
        }

        if (errors.isEmpty()) {
            log.debug("push allowed");

            return true;
        } else {
            YaccErrorBuilder errorBuilder = new YaccErrorBuilder(settings);

            hookResponse.err().print(errorBuilder.getErrorMessage(errors));

            log.debug("push rejected");

            return false;
        }
    }


}
