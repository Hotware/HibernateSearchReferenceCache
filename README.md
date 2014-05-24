HibernateSearchQueryExtension
=====================

[![Gitter chat](https://badges.gitter.im/s4ke/HibernateSearchQueryExtension.png)](https://gitter.im/s4ke/HibernateSearchQueryExtension)

This project is an attempt (currently more of a proof of concept) to
make queries in Hibernate even easier than with the already
existing DSL.

Consider the following Situation:

We have a fulltext enabled search that takes
multiple parameters. And we have a Parameter-Wrapper
that looks something like this:

<pre><code>
public class QueryParameters {

	private String name;
	
	//more fields

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	//more getters & setters

}
</code></pre>

Then we have to do the queries by getting the
data out of the Parameter-Wrapper and then
building our query by hand. This gets repetitive
after some time and all that code looks alike.

But there's another (easier) way
---------------------------------

That's where this extension comes into play.
You only have to add annotations to your
Parameter-Wrapper class like this:

<pre><code>
@Queries(@Query(must = @Must(@SearchField(fieldName = "name", propertyName = "name"))))
public class PlaceQueryBean extends BaseQueryBean<Place> {

	private String name;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
</code></pre>

Pass this to a Searcher, the Searcher builds the query
completely on its own and then returns
the FullTextQuery.

This easier usage doesn't mean that we loose control, though.
A QueryBean has the power to do all the stuff
you could with a hand written query
(if you want to do the query yourself, you still can do that (by overriding customQuery(...))
with callbacks from the Searcher.
