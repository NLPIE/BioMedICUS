package edu.umn.biomedicus.common.simple;

import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.TextSpan;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit test for {@link SimpleTextSpan}.
 */
public class SimpleTextSpanTest {

    @Test
    public void testGetText() throws Exception {
        TextSpan simpleTextSpan = new SimpleTextSpan(new Span(0, 5), "test string");
        assertEquals(simpleTextSpan.getText(), "test ", "Should return the correct substring for the span");
    }

    @Test
    public void testGetBegin() throws Exception {
        TextSpan simpleTextSpan = new SimpleTextSpan(new Span(0, 5), "test string");
        assertEquals(simpleTextSpan.getBegin(), 0, "Should return correct begin");
    }

    @Test
    public void testGetEnd() throws Exception {
        TextSpan textSpan = new SimpleTextSpan(new Span(0, 5), "test string");
        assertEquals(textSpan.getEnd(), 5, "Should return correct end");
    }

    @Test
    public void testEqualsSameObject() throws Exception {
        TextSpan textSpan = new SimpleTextSpan(new Span(0, 5), "test span");
        assertTrue(textSpan.equals(textSpan), "TextSpan should equal itself");
    }

    @Test
    public void testEqualsNull() throws Exception {
        TextSpan textSpan = new SimpleTextSpan(new Span(0, 5), "test span");
        assertFalse(textSpan.equals(null), "TextSpan shouldn't equal null");
    }

    @Test
    public void testEqualsDifferentClass() throws Exception {
        TextSpan textSpan = new SimpleTextSpan(new Span(0, 5), "test span");
        assertFalse(textSpan.equals("String"), "TextSpan shouldn't equal null");
    }

    @Test
    public void testEqualsDifferentValues() throws Exception {
        TextSpan textSpan = new SimpleTextSpan(new Span(0, 5), "test span");
        TextSpan other = new SimpleTextSpan(new Span(0, 10), "other span");
        assertFalse(textSpan.equals(other), "TextSpan shouldn't equal TextSpan with different values");
    }

    @Test
    public void testEquals() throws Exception {
        TextSpan textSpan = new SimpleTextSpan(new Span(0, 5), "text span");
        TextSpan other = new SimpleTextSpan(new Span(0, 5), "text span");
        assertTrue(textSpan.equals(other), "equivalent TextSpans should be equal");
    }

    @Test
    public void testHashCode() throws Exception {
        TextSpan textSpan = new SimpleTextSpan(new Span(0, 5), "text span");
        TextSpan other = new SimpleTextSpan(new Span(0, 5), "text span");
        assertTrue(textSpan.hashCode() == other.hashCode(), "equivalent text spans should have equal hash codes");
    }

    @Test
    public void testHashCodeNeq() throws Exception {
        TextSpan textSpan = new SimpleTextSpan(new Span(0, 5), "test span");
        TextSpan other = new SimpleTextSpan(new Span(0, 10), "other span");
        assertTrue(textSpan.hashCode() != other.hashCode(), "different text spans should have different hash codes");
    }
}