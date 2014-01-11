package com.isroot.stash.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkResponseHandler;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
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
        return applicationLinkService.getPrimaryApplicationLink(JiraApplicationType.class);
    }

    @Override
    public boolean doesJiraApplicationLinkExist()
    {
        return getJiraApplicationLink() != null;
    }


    @Override
    public boolean doesIssueExist(String issueId)
    {
        checkNotNull(issueId, "issueId is null");
        checkNotNull(getJiraApplicationLink(), "applicationLink is null");

        try
        {
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
                    if(response.isSuccessful())
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            });
        }
        catch(Exception ex)
        {
            log.error("exception while communicating with JIRA", ex);

            return false;
        }
    }

    @Override
    public boolean doesIssueMatchJqlQuery(String jqlQuery, String issueId)
    {
        checkNotNull(jqlQuery, "jqlQuery is null");
        checkNotNull(issueId, "issueId is null");
        checkNotNull(getJiraApplicationLink(), "applicationLink is null");

        try
        {
            ApplicationLinkRequest req = getJiraApplicationLink().createAuthenticatedRequestFactory().createRequest(Request.MethodType.POST, "/rest/api/2/search");
            req.setHeader("Content-Type", "application/json");

            Map<String, String> request = Maps.newHashMap();
            request.put("jql", jqlQuery);
            req.setEntity(new Gson().toJson(request));
            String jsonResponse = req.execute();

            Set<String> foundIssues = extractIssueIdsFromJqlSearch(jsonResponse);

            log.debug("issues found for JQL Query \"{}\": {}", jqlQuery, foundIssues);

            return foundIssues.contains(issueId);
        }
        catch(Exception ex)
        {
            log.error("exception while communicating with JIRA", ex);

            return false;
        }
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
