package com.isroot.stash.plugin;

import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.isroot.stash.plugin.errors.YaccError;

import java.util.List;

/**
 * @author Sean Ford
 * @since 2014-01-14
 */
public interface YaccService {
    List<YaccError> checkRefChange(Repository repository, Settings settings,
            RefChange refChange);
}
