package com.isroot.stash.plugin;

import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;

import java.util.List;

/**
 * @author Sean Ford
 * @since 2014-01-14
 */
public interface YaccService
{
	public List<String> checkRefChange(Repository repository, Settings settings, RefChange refChange);
}
