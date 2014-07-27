package com.isroot.stash.plugin;

import com.atlassian.stash.commit.CommitService;
import com.atlassian.stash.content.Changeset;
import com.atlassian.stash.content.ChangesetsBetweenRequest;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.server.ApplicationPropertiesService;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageRequest;
import com.atlassian.stash.util.PageRequestImpl;
import com.google.common.collect.Sets;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
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
    private final CommitService commitService;
    private final ApplicationPropertiesService applicationPropertiesService;

    public ChangesetsServiceImpl(CommitService commitService, ApplicationPropertiesService applicationPropertiesService)
    {
        this.commitService = commitService;
        this.applicationPropertiesService = applicationPropertiesService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<YaccChangeset> getNewChangesets(Repository repository, RefChange refChange)
    {
        try
        {
            org.eclipse.jgit.lib.Repository jGitRepo = getJGitRepo(repository);

            ChangesetsBetweenRequest request = new ChangesetsBetweenRequest.Builder(repository)
                    .exclude(getBranches(repository))
                    .include(refChange.getToHash())
                    .build();

            Page<Changeset> page = commitService.getChangesetsBetween(request, new PageRequestImpl(0, PageRequest.MAX_PAGE_LIMIT));

            Set<YaccChangeset> changesets = Sets.newHashSet();
            for (Changeset changeset : page.getValues())
            {
                final RevWalk walk = new RevWalk(jGitRepo);
                final RevCommit commit = walk.parseCommit(ObjectId.fromString(changeset.getId()));

                /* Note that we use committer, instead of author -- for most commits, these will be identical. Where
                 * this differs is if a patch *author* submits a patch (eg, consider an external contribution), and
                 * the *committer* actually applies the patch.
                 *
                 * By validating the committer here, we can allow surrogate commits on behalf of patch submitters,
                 * while still ensuring that the authenticated user is either the author *or* the committer.
                 */
                final PersonIdent ident = commit.getCommitterIdent();
                final String message = commit.getFullMessage();
                final YaccPerson committer = new YaccPerson(ident.getName(), ident.getEmailAddress());
                final YaccChangeset yaccChangeset = new YaccChangeset(changeset.getId(), committer, message, commit.getParentCount());

                changesets.add(yaccChangeset);
            }

            return changesets;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Set<String> getBranches(Repository repository)
    {
        try
        {
            org.eclipse.jgit.lib.Repository jGitRepo = getJGitRepo(repository);

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

    private org.eclipse.jgit.lib.Repository getJGitRepo (Repository repository) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        File repoDir = applicationPropertiesService.getRepositoryDir(repository);
        return builder.setGitDir(repoDir).build();
    }
}
