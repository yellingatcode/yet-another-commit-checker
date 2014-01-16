package com.isroot.stash.plugin;

import com.atlassian.stash.content.Changeset;
import com.atlassian.stash.content.ChangesetsBetweenRequest;
import com.atlassian.stash.history.HistoryService;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.server.ApplicationPropertiesService;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageRequest;
import com.atlassian.stash.util.PageRequestImpl;
import com.google.common.collect.Sets;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * @author Sean Ford
 * @since 2013-10-26
 */
public class ChangesetsServiceImpl implements ChangesetsService
{
    private final HistoryService historyService;
    private final ApplicationPropertiesService applicationPropertiesService;

    public ChangesetsServiceImpl(HistoryService historyService, ApplicationPropertiesService applicationPropertiesService)
    {
        this.historyService = historyService;
        this.applicationPropertiesService = applicationPropertiesService;
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public Set<Changeset> getNewChangesets(Repository repository, RefChange refChange)
    {
        ChangesetsBetweenRequest request = new ChangesetsBetweenRequest.Builder(repository)
                .exclude(getBranches(repository))
                .include(refChange.getToHash())
                .build();

        Page<Changeset> page = historyService.getChangesetsBetween(request, new PageRequestImpl(0, PageRequest.MAX_PAGE_LIMIT));

        Set<Changeset> changesets = Sets.newHashSet();
        for (Changeset changeset : page.getValues())
        {
            changesets.add(changeset);
        }

        return changesets;
    }

    private Set<String> getBranches(Repository repository)
    {
        try
        {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            File repoDir = applicationPropertiesService.getRepositoryDir(repository);
            org.eclipse.jgit.lib.Repository jGitRepo = builder.setGitDir(repoDir).build();

            Set<String> refHeads = Sets.newHashSet();

            for (String ref : jGitRepo.getAllRefs().keySet())
            {
                if (ref.startsWith("refs/heads/"))
                {
                    refHeads.add(ref);
                }
            }

            return refHeads;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
