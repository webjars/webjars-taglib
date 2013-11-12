package org.webjars.taglib;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.Tag;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.webjars.WebJarAssetLocator;

/**
 * Unit test for LocateTag.
 * 
 * @author Eric Bottard
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class LocateTagTest {

	@InjectMocks
	private LocateTag locateTag = new LocateTag();

	@Mock
	private WebJarAssetLocator mockAssetLocator;

	@Mock
	private JspWriter mockJspWriter;

	@Mock
	private PageContext mockPageGontext;;

	@Test(expected = JspException.class)
	public void pathAndPrefixAreExclusive() throws JspException {
		locateTag.setPath("bootstrap");
		locateTag.setPrefix("/bootstrap");

		locateTag.doStartTag();
	}

	@Test(expected = JspException.class)
	public void prefixRequiresVar() throws JspException {
		locateTag.setPrefix("/bootstrap");
		locateTag.doStartTag();
	}

	@Test(expected = JspException.class)
	public void pathOrPrefixRequired() throws JspException {
		locateTag.doStartTag();
	}

	@Test
	public void testIteration() throws Exception {
		locateTag.setPrefix("/bootstrap");
		locateTag.setVar("myvar");

		Set<String> matches = new LinkedHashSet<String>(
				Arrays.asList(
						"META-INF/resources/webjars/bootstrap/2.2.1/bootstrap.css",
						"META-INF/resources/webjars/bootstrap/2.2.1/bootstrap.js",
						"META-INF/resources/webjars/bootstrap/2.2.1/bootstrap.min.css"
						));
		when(mockAssetLocator.listAssets("/bootstrap")).thenReturn(matches);

		verifyScopeContains("myvar", PageContext.PAGE_SCOPE,
				"/bootstrap/2.2.1/bootstrap.css",
				"/bootstrap/2.2.1/bootstrap.js",
				"/bootstrap/2.2.1/bootstrap.min.css");
	}

	@Test(expected = IllegalArgumentException.class)
	public void pathNotFoundPropagates()
			throws Exception {

		when(mockAssetLocator.getFullPath("bootstrap")).thenThrow(
				IllegalArgumentException.class);
		when(mockPageGontext.getOut()).thenReturn(mockJspWriter);

		locateTag.setOmitExtension(true);
		locateTag.setPath("bootstrap");

		locateTag.doStartTag();
	}

	@Test
	public void test_attribute_assignment()
			throws Exception {

		when(mockAssetLocator.getFullPath("bootstrap")).thenReturn(
				"META-INF/resources/webjars/bootstrap/2.2.1/bootstrap.css");
		when(mockPageGontext.getOut()).thenReturn(mockJspWriter);

		locateTag.setVar("myvar");
		locateTag.setPath("bootstrap");

		verifyScopeContains("myvar", PageContext.PAGE_SCOPE,
				"/bootstrap/2.2.1/bootstrap.css");
	}

	@Test
	public void test_attribute_assignment_non_default_scope()
			throws Exception {

		when(mockAssetLocator.getFullPath("bootstrap")).thenReturn(
				"META-INF/resources/webjars/bootstrap/2.2.1/bootstrap.css");
		when(mockPageGontext.getOut()).thenReturn(mockJspWriter);

		locateTag.setVar("myvar");
		locateTag.setPath("bootstrap");
		locateTag.setScope("request");

		verifyScopeContains("myvar", PageContext.REQUEST_SCOPE,
				"/bootstrap/2.2.1/bootstrap.css");
	}

	@Test
	public void test_trim_extension()
			throws Exception {

		when(mockAssetLocator.getFullPath("bootstrap")).thenReturn(
				"META-INF/resources/webjars/bootstrap/2.2.1/bootstrap.css");
		when(mockPageGontext.getOut()).thenReturn(mockJspWriter);

		locateTag.setOmitExtension(true);
		locateTag.setPath("bootstrap");

		verifyOutContains("/bootstrap/2.2.1/bootstrap");

	}

	@Test
	public void trim_extension_should_not_fail_when_no_extension()
			throws Exception {

		when(mockAssetLocator.getFullPath("bootstrap")).thenReturn(
				"META-INF/resources/webjars/bootstrap/2.2.1/bootstrap");
		when(mockPageGontext.getOut()).thenReturn(mockJspWriter);

		locateTag.setOmitExtension(true);
		locateTag.setPath("bootstrap");

		verifyOutContains("/bootstrap/2.2.1/bootstrap");

	}

	private void verifyOutContains(String expected) throws JspException,
			IOException {
		assertEquals(Tag.EVAL_BODY_INCLUDE, locateTag.doStartTag());
		assertEquals(Tag.EVAL_PAGE, locateTag.doEndTag());

		verify(mockJspWriter).print(expected);
	}

	private void verifyScopeContains(String name, int scope, String... values)
			throws JspException, IOException {
		assertEquals(Tag.EVAL_BODY_INCLUDE, locateTag.doStartTag());
		for (int i = 0; i < values.length; i++) {
			String value = values[i];
			verify(mockPageGontext).setAttribute(name, value, scope);
			if (i != values.length - 1) {
				assertEquals(IterationTag.EVAL_BODY_AGAIN,
						locateTag.doAfterBody());
			} else {
				assertEquals(Tag.SKIP_BODY, locateTag.doAfterBody());
			}
		}
		assertEquals(Tag.EVAL_PAGE, locateTag.doEndTag());
		verifyNoMoreInteractions(mockPageGontext);
	}

}
