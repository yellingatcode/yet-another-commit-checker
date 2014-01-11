package com.isroot.stash.plugin;

import com.atlassian.stash.content.Changeset;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;

import java.util.Set;

/**
 * @author Sean Ford
 * @since 2013-10-26
 */
public interface ChangesetsService
{
    public Set<Changeset> getNewChangesets(Repository repository, RefChange refChange);
}
