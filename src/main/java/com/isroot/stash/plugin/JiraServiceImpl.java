package com.isroot.stash.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkResponseHandler;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseStatusException;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Sean Ford
 * @since 2013-10-20
 */
public class JiraServiceImpl implements JiraService
{
    private static final Logger log = LoggerFactory.getLogger(JiraServiceImpl.class);

    private final ApplicationLinkService applicationLinkService;

    public JiraServiceImpl(ApplicationLinkService applicationLinkService)
    {
        this.applicationLinkService = applicationLinkService;
    }

    private ApplicationLink getJiraApplicationLink()
    {
        ApplicationLink applicationLink = applicationLinkService.getPrimaryApplicationLink(JiraApplicationType.class);

		if(applicationLink == null)
		{
			throw new IllegalStateException("Primary JIRA application link does not exist!");
		}

		return applicationLink;
    }

    @Override
    public boolean doesJiraApplicationLinkExist()
    {
		return applicationLinkService.getPrimaryApplicationLink(JiraApplicationType.class) != null;
    }

    @Override
    public boolean doesIssueExist(IssueKey issueKey) throws CredentialsRequiredException, ResponseException
    {
        checkNotNull(issueKey, "issueKey is null");

		ApplicationLinkRequest req = getJiraApplicationLink().createAuthenticatedRequestFactory().createRequest(Request.MethodType.GET, "/rest/api/2/issue/"+issueKey.getFullyQualifiedIssueKey());

		return req.execute(new ApplicationLinkResponseHandler<Boolean>()
		{
			@Override
			public Boolean credentialsRequired(Response response) throws ResponseException
			{
				return false;
			}

			@Override
			public Boolean handle(Response response) throws ResponseException
			{
				return response.isSuccessful();
			}
		});
    }

    @Override
    public boolean doesProjectExist(String projectKey) throws CredentialsRequiredException, ResponseException
    {
        checkNotNull(projectKey, "projectKey is null");

        ApplicationLinkRequest req = getJiraApplicationLink().createAuthenticatedRequestFactory().createRequest(Request.MethodType.GET, "/rest/api/2/project/"+projectKey);

        String jsonResponse = req.execute();
        JsonObject response = new JsonParser().parse(jsonResponse).getAsJsonObject();
        return (projectKey.equals(response.get("key").getAsString()));
    }

    @Override
    public boolean doesIssueMatchJqlQuery(String jqlQuery, IssueKey issueKey) throws CredentialsRequiredException, ResponseException
    {
        checkNotNull(jqlQuery, "jqlQuery is null");
        checkNotNull(issueKey, "issueKey is null");

        ApplicationLinkRequest req = getJiraApplicationLink().createAuthenticatedRequestFactory().createRequest(Request.MethodType.POST, "/rest/api/2/search");
		req.setHeader("Content-Type", "application/json");

		Map<String, String> request = Maps.newHashMap();
		request.put("jql", jqlQuery);
		req.setEntity(new Gson().toJson(request));

		String jsonResponse = null;

		try
		{
			jsonResponse = req.execute();
		}
		catch(ResponseStatusException e)
		{
			if(e.getResponse().getStatusCode() == 400)
			{
				throw new IllegalArgumentException("JQL query is invalid");
			}
		}

		Set<IssueKey> foundIssues = extractIssueIdsFromJqlSearch(jsonResponse);

		log.debug("issues found for JQL Query \"{}\": {}", jqlQuery, foundIssues);

		return foundIssues.contains(issueKey);
    }

    private Set<IssueKey> extractIssueIdsFromJqlSearch(String json) throws ResponseException {

        JsonObject response = new JsonParser().parse(json).getAsJsonObject();

        JsonArray issues = response.get("issues").getAsJsonArray();

        Set<IssueKey> s = Sets.newHashSet();

        for (JsonElement element : issues)
        {
            try
            {
                s.add(new IssueKey(element.getAsJsonObject().get("key").getAsString()));
            }
            catch (InvalidIssueKeyException ex)
            {
                log.error("unexpected exception while trying to parse JIRA issue key from jql response", ex);
                throw new ResponseException("Could not parse JIRA issue key", ex);
            }
        }

        return s;
    }

}
