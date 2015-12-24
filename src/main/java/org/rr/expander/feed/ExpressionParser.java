package org.rr.expander.feed;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses expressions which can be used to select a part of a html document.
 * 
 * <pre>
 * "id=article/tag=div 2" // select the second div tag under the element with the id named 'article'.
 * </pre>
 */
public class ExpressionParser {
	
	@Nonnull
	final static Logger logger = LoggerFactory.getLogger(ExpressionParser.class);

	@Nonnull
	private static final String TYPE_ID = "id";

	@Nonnull
	private static final String TYPE_TAG = "tag";

	@Nonnull
	private final String expression;

	@Nonnull
	private List<String> expressionPathSegemnts;

	public static List<ExpressionParser> createExpressionParser(@Nullable String expression) {
		return Arrays.asList(StringUtils.split(expression != null ? expression : EMPTY, "|")).stream().map(ex -> new ExpressionParser(ex).parse())
				.collect(Collectors.toList());
	}

	private ExpressionParser(@Nonnull String expression) {
		this.expression = expression != null ? expression : EMPTY;
		this.expressionPathSegemnts = new ArrayList<>();
	}

	private ExpressionParser parse() {
		if(!expressionPathSegemnts.isEmpty()) {
			throw new IllegalArgumentException("Seems to be parse was already invoked.");
		}
		expressionPathSegemnts.addAll(Arrays.asList(StringUtils.split(expression, "/")));
		return this;
	}

	public int segmentCount() {
		return expressionPathSegemnts.size();
	}

	public int getSegmentIndexAt(int index) {
		return NumberUtils.toInt(StringUtils.substringAfter(getElementValueAt(index), " "), 1);
	}

	/**
	 * Get the name of the tag for the path expression segment at the given
	 * <code>index</code>.
	 * 
	 * @param index
	 *          The path segment index.
	 * @return The value for the tag name at the given <code>index</code>. If The
	 *         is no tag at the given <code>index</code> specified,
	 *         <code>null</code> will be returned.
	 */
	public @Nullable String getTagSegmentValueAt(int index) {
		if (isElementOfType(index, TYPE_TAG)) {
			return StringUtils.substringBefore(getElementValueAt(index), " ");
		}
		return null;
	}

	public @Nullable String getIdSegmentValueAt(int index) {
		if (isElementOfType(index, TYPE_ID)) {
			return getElementValueAt(index);
		}
		return null;
	}

	private @NotNull String getElementValueAt(int index) {
		String expressionPathSegemnt = expressionPathSegemnts.get(index);
		if (StringUtils.isBlank(expressionPathSegemnt)) {
			logger.info(String.format("The segment %s of the expression '%s' have no value.", index, expressionPathSegemnt));
			return null;
		}

		return StringUtils.substringAfter(expressionPathSegemnts.get(index), "=");
	}

	private boolean isElementOfType(int index, @Nonnull String name) {
		return StringUtils.startsWithIgnoreCase(expressionPathSegemnts.get(index), name);
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("expression", expression).build();
	}
}
