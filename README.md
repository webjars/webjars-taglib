# WebJars-Taglib

## Introduction
This project contains a simple taglib for locating WebJars resources without knowing their full path. Typical usage is to avoid embedding the version number of your libs in JSP pages.

The simplest usage is shown below:

	<%@ taglib uri="http://www.webjars.org/tags" prefix="wj"%>
	
	<wj:locate path="bootstrap.css" />
	
will output something like

	/bootstrap/2.3.1/css/bootstrap.css
	
If you prefer assigning a variable with the path, simply add the `var` attribute and use that variable in your JSP page either like this (using for example Spring's `<url />` tag):

    <wj:locate path="bootstrap.css" var="v"/>
    <link href="<s:url value='/webjars${v}' />" rel="stylesheet">
or like this

    <wj:locate path="bootstrap.css" var="v">
	    <link href="<s:url value='/webjars${v}' />" rel="stylesheet">
	</wj:locate>

## Matching multiple resources
It is possible to match several resources and iterate over each result, using the `prefix` attribute instead of `path`:

	<wj:locate prefix="/bootstrap" var="v">
		<%-- Do something with ${v}--%>
	</wj:locate>

## Advanced usage
There are other attributes one can use, namely:

### Omit file extension
	<wj:locate path="bootstrap.css" omitExtension="false" />
	
will output something like

	/bootstrap/2.3.1/css/bootstrap
instead of

	/bootstrap/2.3.1/css/bootstrap.css
This is useful _e.g._ when using [require.js](http://requirejs.org)

### Controlling variable exposition scope
By default, the path is emitted as an attribute with _page_ scope. If you want to use another scope, simply use the `scope` attribute with values `request`, `session` or `application`:

	<wj:locate path="bootstrap.css" var="v" scope="session" />

### Changing the relative root for results
The default behavior is to remove the `META-INF/resources/webjars` prefix from resource paths. If you want to emit paths with a longer (or shorter) prefix, just change that value thru the use of the `relativeTo` attribute:

	<wj:locate path="bootstrap.css" relativeTo="META-INF/resources" />
	
will output something like

	/webjars/bootstrap/2.3.1/css/bootstrap
