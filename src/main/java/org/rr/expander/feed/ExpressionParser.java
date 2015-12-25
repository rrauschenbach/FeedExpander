package org.rr.expander.feed;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.math.NumberUtils.toInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses expressions which can be used to select a part of a html document. A valid expression can
 * be separated in segments with a slash. A segment always have a type and a value separated by an
 * equality character. The type can be {@link #TYPE_TAG} or {@link #TYPE_ID}. The value is a free
 * word having optionally a numeric value as index separated with a whitespace.
 * 
 * The following example results in two segments.
 * 
 * <pre>
 * "id=article/tag=div 2"
 * </pre>
 */
public class ExpressionParser {
	
	@Nonnull
	final static Logger logger = LoggerFactory.getLogger(ExpressionParser.class);

	@Nonnull
	private final String expression;

	@Nonnull
	private List<String> expressionPathSegments;
	
	public static enum SEGMENT_TYPE {
    TAG("tag"), ID("id");
		
    String name;
 
    SEGMENT_TYPE(String name) {
      this.name = name;
    }
  }

	public static List<ExpressionParser> createExpressionParser(@Nullable String expression) {
		return Arrays.asList(split(expression != null ? expression : EMPTY, "|"))
				.stream()
				.map(ex -> new ExpressionParser(ex).parse())
				.collect(toList());
	}

	private ExpressionParser(@Nonnull String expression) {
		this.expression = expression != null ? expression : EMPTY;
		this.expressionPathSegments = new ArrayList<>();
	}

	private ExpressionParser parse() {
		if(!expressionPathSegments.isEmpty()) {
			throw new IllegalArgumentException("Seems to be parse was already invoked.");
		}
		expressionPathSegments.addAll(Arrays.asList(split(expression, "/")));
		return this;
	}

	/**
	 * The number of available segments in the expression for this {@link ExpressionParser} instance.
	 * 
	 * @return The number of available segments.
	 */
	public int segmentCount() {
		return expressionPathSegments.size();
	}

	/**
	 * The number at the end of a segment. If no number is specified, the default value 1 will be
	 * returned.
	 * 
	 * <pre>
	 * "tag=div 2" // 2 will be returned
	 * </pre>
	 * 
	 * @param index The index of the segment. 0 is the first segment.
	 * @return The number at the end of a segment.
	 */
	public int getSegmentNumber(int index) {
		String expressionPathSegment = getExpressionPathSegment(index);
		return toInt(substringAfter(substringAfter(expressionPathSegment, "="), " "), 1);
	}
	
	/**
	 * Tells what kind of segment is located at the given segment index.
	 * 
	 * <pre>
	 * "tag=div 2" // {@link SEGMENT_TYPE#TAG} will be returned
	 * "id=test" // {@link SEGMENT_TYPE#ID} will be returned
	 * </pre>
	 * 
	 * @param index The index of the segment. 0 is the first segment.
	 * @return The {@link SEGMENT_TYPE} at the given <code>index</code>.
	 * @throws IllegalArgumentException if the segment type specified with the expression did not
	 *         exists.
	 */
	public @NotNull SEGMENT_TYPE getSegmentType(int index) {
		return SEGMENT_TYPE.valueOf(getSegmentName(index).toUpperCase());
	}

	/**
	 * Get the name of the segment at the specified segment index.
	 * 
	 * <pre>
	 * "tag=div 2" // "tag" will be returned.
	 * "id=test" // "id" will be returned
	 * </pre>
	 * 
	 * @param index The index of the expression segment.
	 * @return The name part of the segment. The name can possibly be empty but never
	 *         <code>null</code>.
	 */
	public @NotNull String getSegmentName(int index) {
		String expressionPathSegment = getExpressionPathSegment(index);
		return substringBefore(expressionPathSegment, "=");
	}

	/**
	 * Get the value of the segment at the specified segment index. The number which is optionally
	 * behind the value is not be part of the value.
	 * 
	 * <pre>
	 * "tag=div 2" // "div" will be returned.
	 * "id=test" // "test" will be returned
	 * </pre>
	 * 
	 * @param index The index of the expression segment.
	 * @return The value part of the segment. The value can possibly be empty but never
	 *         <code>null</code>.
	 */
	public @NotNull String getSegmentValue(int index) {
		String expressionPathSegment = getExpressionPathSegment(index);
		return substringAfter(substringBefore(expressionPathSegment, " "), "=");
	}

	private @NotNull String getExpressionPathSegment(int index) {
		String expressionPathSegemnt = expressionPathSegments.get(index);
		if (isBlank(expressionPathSegemnt)) {
			logger.info(String.format("The segment %s of the expression '%s' is empty.", index, expressionPathSegemnt));
			return EMPTY;
		}
		return expressionPathSegemnt;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("expression", "'" + expression + "'").build();
	}
}
