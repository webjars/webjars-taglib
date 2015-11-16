package org.webjars.taglib;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.webjars.WebJarAssetLocator;

/**
 * JSP tag that allows searching for one or multiple webjars resource(s). Will
 * either output the found path(s), or assign it(them) to a scope attribute
 * (iterating over the results).
 * 
 * @author Eric Bottard
 */
@SuppressWarnings("serial")
public class LocateTag extends TagSupport {

	private static final String SCOPE_APPLICATION = "application";

	private static final String SCOPE_REQUEST = "request";

	private static final String SCOPE_SESSION = "session";

	/**
	 * Used to locate resources by classpath scanning.
	 */
	protected WebJarAssetLocator assetLocator = new WebJarAssetLocator();

	/**
	 * Holds the result(s).
	 */
	private Iterator<String> result;

	/**
	 * The id of the WebJar to search. If specified the path must be the exact
	 * path of the file within the WebJar.
	 *
	 * @since 0.3
	 */
	private String webjar;

	/**
	 * Whether to remove the resource extension before returning the result.
	 * Useful <i>e.g.</> for <a href="http://requirejs.org/">require.js</a>.
	 * 
	 * <p>
	 * Default is {@literal false}.
	 * </p>
	 */
	private boolean omitExtension = false;

	/**
	 * The path we're looking for, <i>e.g.</i> {@literal bootstrap.css}.
	 * Exclusive with {@link #prefix}.
	 */
	private String path = null;

	/**
	 * When looking for several matches, the prefix they should all share,
	 * excluding the {@literal /META-INF/resources/webjars} part. Exclusive with
	 * {@link #path}.
	 */
	private String prefix = null;

	/**
	 * The in-jar path prefix to chop off before advertising the result.
	 * Defaults to {@value WebJarAssetLocator#WEBJARS_PATH_PREFIX}.
	 */
	private String relativeTo = WebJarAssetLocator.WEBJARS_PATH_PREFIX;

	/**
	 * When setting an attribute instead of emitting the result, the scope in
	 * which the var is set. Defaults to page scope.
	 */
	private int scope = PageContext.PAGE_SCOPE;

	/**
	 * If set, instead of outputting the value, will assign the result to an
	 * attribute with that name. Mandatory when using the {@link #prefix}
	 * approach.
	 */
	private String var = null;

	/**
	 * Remove the leading prefix and maybe the extension of the path.
	 */
	private String chop(String fullPath) {
		if (!fullPath.startsWith(relativeTo)) {
			throw new IllegalStateException("Found resource '" + fullPath
					+ "' does not start with 'relativeTo' prefix '"
					+ relativeTo + "'");
		}
		fullPath = fullPath.substring(relativeTo.length());

		if (omitExtension) {
			int lastDot = fullPath.lastIndexOf('.');
			int lastSlash = fullPath.lastIndexOf('/');
			if (lastDot > lastSlash) {
				fullPath = fullPath.substring(0, lastDot);
			}
		}
		return fullPath;
	}

	@Override
	public int doAfterBody() throws JspException {
		return emitOne() ? EVAL_BODY_AGAIN : SKIP_BODY;
	}

	@Override
	public int doEndTag() throws JspException {
		// For the case when the tag was self-closing
		emitOne();
		reset();
		return EVAL_PAGE;
	}

	@Override
	public int doStartTag() throws JspException {
		if (!((prefix == null) ^ (path == null))) {
			throw new JspException(
					"Either one of prefix or path must be set (but not both)");
		}
		if (prefix != null && var == null) {
			throw new JspException(
					"Use of prefix attribute requires use of var attribute");
		}

		if (path != null) {
			if (webjar != null) {
				result = Collections
						.singletonList(assetLocator.getFullPathExact(webjar, path))
						.iterator();
			} else {
				result = Collections
						.singletonList(assetLocator.getFullPath(path)).iterator();
			}
		} else {
			result = assetLocator.listAssets(prefix).iterator();
		}

		emitOne();
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * @return true if some value was emitted
	 */
	private boolean emitOne() throws JspException {
		if (result.hasNext()) {
			String r = chop(result.next());
			if (this.var == null) {
				// print the path to the writer
				try {
					pageContext.getOut().print(r);
				} catch (IOException e) {
					throw new JspException(e);
				}
			}
			else {
				// store the path as a variable
				pageContext.setAttribute(var, r, scope);
			}
			return true;
		} else {
			return false;
		}
	}

	public String getWebjar() {
		return webjar;
	}

	public boolean getOmitExtension() {
		return omitExtension;
	}

	public String getPath() {
		return path;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getRelativeTo() {
		return relativeTo;
	}

	public String getVar() {
		return var;
	}

	private void reset() {
		this.path = null;
		this.scope = PageContext.PAGE_SCOPE;
		this.prefix = null;
		this.var = null;
		this.relativeTo = WebJarAssetLocator.WEBJARS_PATH_PREFIX;
		this.webjar = null;
		this.omitExtension = false;
		if (result.hasNext()) {
			throw new IllegalStateException(
					"Not all results consumed. At least '" + result.next()
							+ "' remains");
		}
		this.result = null;
	}

	public void setWebjar(String webjar) {
		this.webjar = webjar;
	}

	public void setOmitExtension(boolean omitExtension) {
		this.omitExtension = omitExtension;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setRelativeTo(String relativeTo) {
		this.relativeTo = relativeTo;
	}

	public void setScope(String scope) {
		if (scope.equals(SCOPE_REQUEST)) {
			this.scope = PageContext.REQUEST_SCOPE;
		}
		else if (scope.equals(SCOPE_SESSION)) {
			this.scope = PageContext.SESSION_SCOPE;
		}
		else if (scope.equals(SCOPE_APPLICATION)) {
			this.scope = PageContext.APPLICATION_SCOPE;
		}
		else {
			this.scope = PageContext.PAGE_SCOPE;
		}
	}

	public void setVar(String var) {
		this.var = var;
	}

}