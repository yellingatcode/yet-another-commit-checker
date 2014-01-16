package ut.com.isroot.stash.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.isroot.stash.plugin.JiraService;
import com.isroot.stash.plugin.JiraServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Sean Ford
 * @since 2014-01-15
 */
public class JiraServiceImplTest
{
	@Mock private ApplicationLinkService applicationLinkService;

	private JiraService jiraService;

	@Before
	public void setup()
	{
		MockitoAnnotations.initMocks(this);

		jiraService = new JiraServiceImpl(applicationLinkService);
	}

	@Test
	public void testDoesJiraApplicationLinkExist_returnsFalseIfLinkDoesNotExist() throws Exception
	{
		when(applicationLinkService.getPrimaryApplicationLink(JiraApplicationType.class)).thenReturn(null);

		assertThat(jiraService.doesJiraApplicationLinkExist()).isFalse();
	}

	@Test
	public void testDoesJiraApplicationLinkExist_returnsTrueIfLinkExists() throws Exception
	{
		when(applicationLinkService.getPrimaryApplicationLink(JiraApplicationType.class)).thenReturn(mock(ApplicationLink.class));

		assertThat(jiraService.doesJiraApplicationLinkExist()).isTrue();
	}
}
