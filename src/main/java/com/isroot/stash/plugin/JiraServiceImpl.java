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
    public boolean doesIssueExist(String issueId) throws CredentialsRequiredException, ResponseException
    {
        checkNotNull(issueId, "issueId is null");

		ApplicationLinkRequest req = getJiraApplicationLink().createAuthenticatedRequestFactory().createRequest(Request.MethodType.GET, "/rest/api/2/issue/"+issueId);

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
    public boolean doesIssueMatchJqlQuery(String jqlQuery, String issueId) throws CredentialsRequiredException, ResponseException
    {
        checkNotNull(jqlQuery, "jqlQuery is null");
        checkNotNull(issueId, "issueId is null");

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

		Set<String> foundIssues = extractIssueIdsFromJqlSearch(jsonResponse);

		log.debug("issues found for JQL Query \"{}\": {}", jqlQuery, foundIssues);

		return foundIssues.contains(issueId);
    }

    private Set<String> extractIssueIdsFromJqlSearch(String json)
    {
        JsonObject response = new JsonParser().parse(json).getAsJsonObject();

        JsonArray issues = response.get("issues").getAsJsonArray();

        Set<String> s = Sets.newHashSet();

        for (JsonElement element : issues)
        {
            s.add(element.getAsJsonObject().get("key").getAsString());
        }

        return s;
    }

}
