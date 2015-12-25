package org.rr.expander.feed;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.rr.expander.feed.ExpressionParser;

public class ExpressionParserTest {

	@Test
	public void testZeroExpressionCount() {
		List<ExpressionParser> parsers = ExpressionParser.createExpressionParser("");
		assertEquals(0, parsers.size());
	}
	
	@Test
	public void testNullExpression() {
		List<ExpressionParser> parsers = ExpressionParser.createExpressionParser(null);
		assertEquals(0, parsers.size());
	}
	
	@Test
	public void testOneExpressionCount() {
		List<ExpressionParser> parsers = ExpressionParser.createExpressionParser("tag=div");
		assertEquals(1, parsers.size());
		ExpressionParser parser = parsers.get(0);
		
		assertEquals(1, parser.segmentCount());
	}
	
	@Test
	public void testMultipleExpressionCount() {
		List<ExpressionParser> parsers = ExpressionParser.createExpressionParser("tag=div/tag=div/tag=div");
		assertEquals(1, parsers.size());
		ExpressionParser parser = parsers.get(0);
		
		assertEquals(3, parser.segmentCount());
	}
	
	@Test
	public void testExpressionGetTag() {
		List<ExpressionParser> parsers = ExpressionParser.createExpressionParser("tag=div/tag=p");
		assertEquals(1, parsers.size());
		ExpressionParser parser = parsers.get(0);
		
		assertEquals("div", parser.getSegmentValue(0));
		assertEquals("p", parser.getSegmentValue(1));
	}
	
	@Test
	public void testExpressionGetId() {
		List<ExpressionParser> parsers = ExpressionParser.createExpressionParser("id=test1/id=test2");
		assertEquals(1, parsers.size());
		ExpressionParser parser = parsers.get(0);
		
		assertEquals("test1", parser.getSegmentValue(0));
		assertEquals("test2", parser.getSegmentValue(1));
	}
	
}
