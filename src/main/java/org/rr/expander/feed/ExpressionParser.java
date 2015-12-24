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
 * Parses expressions which can be used to select a part of a html document. A
 * valid expression can be separated in segments with a slash. A segment always
 * have a type and a value separated by an equality character. The type can be
 * {@link #TYPE_TAG} or {@link #TYPE_ID}. The value is a free word having
 * optional a numeric value as index separated with a whitespace.
 * 
 * The following example results in two segments.
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
		return Arrays.asList(StringUtils.split(expression != null ? expression : EMPTY, "|")).stream().map(ex -> new ExpressionParser(ex).parse())
				.collect(Collectors.toList());
	}

	private ExpressionParser(@Nonnull String expression) {
		this.expression = expression != null ? expression : EMPTY;
		this.expressionPathSegments = new ArrayList<>();
	}

	private ExpressionParser parse() {
		if(!expressionPathSegments.isEmpty()) {
			throw new IllegalArgumentException("Seems to be parse was already invoked.");
		}
		expressionPathSegments.addAll(Arrays.asList(StringUtils.split(expression, "/")));
		return this;
	}

	/**
	 * The number of available segments in the expression for this
	 * {@link ExpressionParser} instance.
	 * 
	 * @return The number of available segments.
	 */
	public int segmentCount() {
		return expressionPathSegments.size();
	}

	/**
	 * The number at the end of a segment. If no number is specified, the default
	 * value 1 will be returned.
	 * 
	 * <pre>
	 * "tag=div 2" // this segment will return 2
	 * </pre>
	 * 
	 * @param index
	 *          The index of the segment. 0 is the first segment.
	 * @return The number at the end of a segment.
	 */
	public int getSegmentNumber(int index) {
		String expressionPathSegment = getExpressionPathSegment(index);
		return NumberUtils.toInt(StringUtils.substringAfter(StringUtils.substringAfter(expressionPathSegment, "="), " "), 1);
	}
	
	/**
	 * Tells what kind of segment is located at the given segment index.
	 * 
	 * @param index
	 *          The index of the segment. 0 is the first segment.
	 * @return The {@link SEGMENT_TYPE} at the given <code>index</code>.
	 * @throws IllegalArgumentException
	 *           if the segment type specified with the expression did not exists.
	 */
	public @NotNull SEGMENT_TYPE getSegmentType(int index) {
		return SEGMENT_TYPE.valueOf(getSegmentName(index).toUpperCase());
	}

	public @NotNull String getSegmentName(int index) {
		String expressionPathSegment = getExpressionPathSegment(index);
		return StringUtils.substringBefore(expressionPathSegment, "=");
	}

	public @NotNull String getSegmentValue(int index) {
		String expressionPathSegment = getExpressionPathSegment(index);
		return StringUtils.substringAfter(StringUtils.substringBefore(expressionPathSegment, " "), "=");
	}

	private @NotNull String getExpressionPathSegment(int index) {
		String expressionPathSegemnt = expressionPathSegments.get(index);
		if (StringUtils.isBlank(expressionPathSegemnt)) {
			logger.info(String.format("The segment %s of the expression '%s' is empty.", index, expressionPathSegemnt));
			return EMPTY;
		}
		return expressionPathSegemnt;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("expression", expression).build();
	}
}
