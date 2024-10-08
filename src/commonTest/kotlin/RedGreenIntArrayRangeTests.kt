import me.thorny.twoColoredRange.math.IntBoundMath
import me.thorny.twoColoredRange.RedGreenIntArrayRange
import me.thorny.twoColoredRange.RedGreenColor
import kotlin.test.*

internal class RedGreenIntArrayRangeTests {
  private fun eachColor(action: (color: RedGreenColor, otherColor: RedGreenColor) -> Unit) {
    action(RedGreenColor.Red, RedGreenColor.Green)
    action(RedGreenColor.Green, RedGreenColor.Red)
  }

  private fun rangeOfColor(range: IntRange, color: RedGreenColor): RedGreenIntArrayRange {
    val redGreenRange = RedGreenIntArrayRange(range)
    redGreenRange.setSubrangeColor(range, color)
    return redGreenRange
  }

  @Test
  fun testConstructorExceptions() {
    expect<Unit>(Unit) { RedGreenIntArrayRange(1..6) }
    expect<Unit>(Unit) { RedGreenIntArrayRange(1..1) }
    assertFailsWith<IllegalArgumentException> { RedGreenIntArrayRange(IntRange(1, 0)) }
  }

  @Test
  fun testBasicProperties() {
    val range = RedGreenIntArrayRange(1..2)
    assertEquals(1..2, range.range)
    assertEquals(1, range.step)
    assertEquals(IntBoundMath, range.math)
    assertEquals(RedGreenColor.Red, range.defaultColor)
    assertEquals(RedGreenColor.Green, range.otherColor)
    assertEquals(2, range.length)
  }

  @Test
  fun testBasicGetters() {
    val range = RedGreenIntArrayRange(1..2)
    assertContentEquals(listOf(1..2), range.getRedSubranges())
    assertContentEquals(emptyList(), range.getGreenSubranges())
    range.setSubrangeGreen(1..2)
    assertContentEquals(emptyList(), range.getRedSubranges())
    assertContentEquals(listOf(1..2), range.getGreenSubranges())
  }

  @Test
  fun testSuperclassGettersSetters() {
    val range = RedGreenIntArrayRange(1..2)

    range.setSubrangeDefaultColor(1..2)
    assertContentEquals(listOf(1..2), range.getSubrangesOfColor(RedGreenColor.Red))
    assertContentEquals(emptyList(), range.getSubrangesOfColor(RedGreenColor.Green))
    assertContentEquals(listOf(1..2), range.getSubrangesOfDefaultColor())
    assertContentEquals(emptyList(), range.getSubrangesOfOtherColor())
    assertEquals(RedGreenColor.Red, range.getColor(1))
    assertEquals(RedGreenColor.Red, range.getColor(2))

    range.setSubrangeOtherColor(1..2)
    assertContentEquals(emptyList(), range.getSubrangesOfColor(RedGreenColor.Red))
    assertContentEquals(listOf(1..2), range.getSubrangesOfColor(RedGreenColor.Green))
    assertContentEquals(emptyList(), range.getSubrangesOfDefaultColor())
    assertContentEquals(listOf(1..2), range.getSubrangesOfOtherColor())
    assertEquals(RedGreenColor.Green, range.getColor(1))
    assertEquals(RedGreenColor.Green, range.getColor(2))

    assertFailsWith<IllegalArgumentException> { range.getColor(0) }
    assertFailsWith<IllegalArgumentException> { range.getColor(3) }

    eachColor { color, otherColor ->
      val anotherRange = RedGreenIntArrayRange(1..3)
      anotherRange.setSubrangeColor(1..3, color)
      anotherRange.setSubrangeColor(1..1, otherColor)
      anotherRange.setSubrangeColor(3..3, otherColor)

      assertEquals(color, anotherRange.getColor(2))
      assertEquals(otherColor, anotherRange.getColor(3))
    }
  }

  @Test
  fun testSubrangesExceptions() {
    eachColor { color, _ ->
      expect(Unit) { RedGreenIntArrayRange(1..3).setSubrangeColor(1..3, color) }
      assertFailsWith<IllegalArgumentException> { RedGreenIntArrayRange(1..3).setSubrangeColor(0..3, color) }
      assertFailsWith<IllegalArgumentException> { RedGreenIntArrayRange(1..3).setSubrangeColor(1..4, color) }
      assertFailsWith<IllegalArgumentException> { RedGreenIntArrayRange(1..3).setSubrangeColor(0..4, color) }
      assertFailsWith<IllegalArgumentException> { RedGreenIntArrayRange(1..3).setSubrangeColor(IntRange(3, 1), color) }
    }
  }

  @Test
  fun testBasicSubranges() {
    eachColor { color, otherColor ->
      val range = rangeOfColor(1..11, otherColor)
      // Add subrange
      range.setSubrangeColor(6..6, color)
      assertContentEquals(listOf(6..6), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(1..5, 7..11), range.getSubrangesOfColor(otherColor))
      // Add range on far left - should be added before existing subrange
      range.setSubrangeColor(1..1, color)
      assertContentEquals(listOf(1..1, 6..6), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(2..5, 7..11), range.getSubrangesOfColor(otherColor))
      // Add range on far right - should be added after existing subrange
      range.setSubrangeColor(11..11, color)
      assertContentEquals(listOf(1..1, 6..6, 11..11), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(2..5, 7..10), range.getSubrangesOfColor(otherColor))
      // Add range in middle between other ranges
      range.setSubrangeColor(3..3, color)
      range.setSubrangeColor(9..9, color)
      assertContentEquals(listOf(1..1, 3..3, 6..6, 9..9, 11..11), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(2..2, 4..5, 7..8, 10..10), range.getSubrangesOfColor(otherColor))
    }
  }

  @Test
  fun testSingleIntersectingSubranges() {
    eachColor { color, otherColor ->
      val range = rangeOfColor(1..11, otherColor)
      // Add same ranges - subranges should have one copy
      range.setSubrangeColor(6..6, color)
      range.setSubrangeColor(6..6, color)
      assertContentEquals(listOf(6..6), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(1..5, 7..11), range.getSubrangesOfColor(otherColor))
      // Add greater-right range - subranges should have union
      range.setSubrangeColor(6..7, color)
      assertContentEquals(listOf(6..7), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(1..5, 8..11), range.getSubrangesOfColor(otherColor))
      // Add greater-left range - subranges should have union
      range.setSubrangeColor(5..7, color)
      assertContentEquals(listOf(5..7), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(1..4, 8..11), range.getSubrangesOfColor(otherColor))
      // Add range intersecting on left
      range.setSubrangeColor(4..6, color)
      assertContentEquals(listOf(4..7), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(1..3, 8..11), range.getSubrangesOfColor(otherColor))
      // Add range intersecting on right
      range.setSubrangeColor(6..8, color)
      assertContentEquals(listOf(4..8), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(1..3, 9..11), range.getSubrangesOfColor(otherColor))
      // Add greater-both range
      range.setSubrangeColor(3..9, color)
      assertContentEquals(listOf(3..9), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(1..2, 10..11), range.getSubrangesOfColor(otherColor))
    }
  }

  @Test
  fun testMultipleIntersectingSubranges() {
    eachColor { color, otherColor ->
      var range = rangeOfColor(1..11, otherColor)
      range.setSubrangeColor(2..3, color)
      range.setSubrangeColor(5..6, color)
      // Add two-intersection subrange
      range.setSubrangeColor(3..5, color)
      assertContentEquals(listOf(2..6), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(1..1, 7..11), range.getSubrangesOfColor(otherColor))

      range = rangeOfColor(1..11, otherColor)
      range.setSubrangeColor(2..3, color)
      range.setSubrangeColor(5..6, color)
      // Add two-intersection subrange touching ends
      range.setSubrangeColor(2..6, color)
      assertContentEquals(listOf(2..6), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(1..1, 7..11), range.getSubrangesOfColor(otherColor))

      range = rangeOfColor(1..11, otherColor)
      range.setSubrangeColor(2..3, color)
      range.setSubrangeColor(5..6, color)
      // Add two-intersection greater subrange
      range.setSubrangeColor(1..7, color)
      assertContentEquals(listOf(1..7), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(8..11), range.getSubrangesOfColor(otherColor))

      range = rangeOfColor(1..11, otherColor)
      range.setSubrangeColor(2..3, color)
      range.setSubrangeColor(5..6, color)
      range.setSubrangeColor(8..9, color)
      // Add three-intersection subrange
      range.setSubrangeColor(3..8, color)
      assertContentEquals(listOf(2..9), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(1..1, 10..11), range.getSubrangesOfColor(otherColor))
    }
  }

  @Test
  fun testTouchingSubranges() {
    eachColor { color, otherColor ->
      val range = rangeOfColor(1..11, otherColor)
      range.setSubrangeColor(6..6, color)
      // Add close range on left - should expand subrange
      range.setSubrangeColor(4..5, color)
      assertContentEquals(listOf(4..6), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(1..3, 7..11), range.getSubrangesOfColor(otherColor))
      // Add close range on right - should expand subrange
      range.setSubrangeColor(7..8, color)
      assertContentEquals(listOf(4..8), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(1..3, 9..11), range.getSubrangesOfColor(otherColor))
      // Add close range between ranges
      range.setSubrangeColor(1..2, color)
      range.setSubrangeColor(3..3, color)
      assertContentEquals(listOf(1..8), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(9..11), range.getSubrangesOfColor(otherColor))
    }
  }

  @Test
  fun testTouchingIntersectingSubranges() {
    eachColor { color, otherColor ->
      var range = rangeOfColor(1..11, otherColor)
      range.setSubrangeColor(2..3, color)
      range.setSubrangeColor(5..6, color)
      // Add intersecting-left touching-right range
      range.setSubrangeColor(3..4, color)
      assertContentEquals(listOf(2..6), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(1..1, 7..11), range.getSubrangesOfColor(otherColor))

      range = rangeOfColor(1..11, otherColor)
      range.setSubrangeColor(2..3, color)
      range.setSubrangeColor(5..6, color)
      // Add touching-left intersecting-right range
      range.setSubrangeColor(4..5, color)
      assertContentEquals(listOf(2..6), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(1..1, 7..11), range.getSubrangesOfColor(otherColor))

      range = rangeOfColor(1..11, otherColor)
      range.setSubrangeColor(2..3, color)
      range.setSubrangeColor(5..6, color)
      // Add containing-left touching-right range
      range.setSubrangeColor(1..4, color)
      assertContentEquals(listOf(1..6), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(7..11), range.getSubrangesOfColor(otherColor))

      range = rangeOfColor(1..11, otherColor)
      range.setSubrangeColor(2..3, color)
      range.setSubrangeColor(5..6, color)
      // Add touching-left containing-right range
      range.setSubrangeColor(3..7, color)
      assertContentEquals(listOf(2..7), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(1..1, 8..11), range.getSubrangesOfColor(otherColor))

      range = rangeOfColor(1..11, otherColor)
      range.setSubrangeColor(2..3, color)
      range.setSubrangeColor(5..6, color)
      range.setSubrangeColor(8..9, color)
      // Add intersecting-left containing-middle touching-right range
      range.setSubrangeColor(3..7, color)
      assertContentEquals(listOf(2..9), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(1..1, 10..11), range.getSubrangesOfColor(otherColor))

      range = rangeOfColor(1..11, otherColor)
      range.setSubrangeColor(2..3, color)
      range.setSubrangeColor(5..6, color)
      range.setSubrangeColor(8..9, color)
      // Add touching-left containing-middle intersecting-right range
      range.setSubrangeColor(4..8, color)
      assertContentEquals(listOf(2..9), range.getSubrangesOfColor(color))
      assertContentEquals(listOf(1..1, 10..11), range.getSubrangesOfColor(otherColor))
    }
  }

  @Test
  fun testSubrangeBasicRequests() {
    eachColor { color, otherColor ->
      val range = rangeOfColor(1..2, color)
      assertEquals(1..1, range.getSubrangeOfColor(color))
      assertEquals(1..2, range.getSubrangeOfColor(color, 2))
      assertEquals(1..2, range.getSubrangeOfColor(color, 3))
      assertEquals(null, range.getSubrangeOfColor(otherColor))

      range.setSubrangeColor(1..2, otherColor)
      assertEquals(1..1, range.getSubrangeOfColor(otherColor))
      assertEquals(1..2, range.getSubrangeOfColor(otherColor, 2))
      assertEquals(1..2, range.getSubrangeOfColor(otherColor, 3))
      assertEquals(null, range.getSubrangeOfColor(color))
    }
  }

  @Test
  fun testSubrangeAdvancedRequests() {
    eachColor { color, otherColor ->
      var range = rangeOfColor(1..7, color)
      range.setSubrangeColor(3..4, otherColor)
      assertEquals(5..6, range.getSubrangeOfColor(color, 2, 2..6))

      range = rangeOfColor(1..5, color)
      range.setSubrangeColor(1..2, otherColor)
      range.setSubrangeColor(4..5, otherColor)
      assertContentEquals(
        listOf(
          2..2 to otherColor,
          3..3 to color,
          4..4 to otherColor,
        ),
        range.subrangesIterator(2..4).asSequence().toList(),
      )
      assertContentEquals(
        listOf(
          4..5 to otherColor,
        ),
        range.subrangesIterator(4..5).asSequence().toList(),
      )

      range = rangeOfColor(1..5, color)
      range.setSubrangeColor(3..3, otherColor)
      assertContentEquals(
        listOf(
          2..2 to color,
          3..3 to otherColor,
          4..4 to color,
        ),
        range.subrangesIterator(2..4).asSequence().toList(),
      )
      assertContentEquals(
        listOf(
          4..4 to color,
        ),
        range.subrangesIterator(4..4).asSequence().toList(),
      )
    }
  }

  @Test
  fun testSubrangeRequestsExceptions() {
    expect<Unit>(Unit) { RedGreenIntArrayRange(1..2).subrangesIterator(IntRange(2, 2)) }
    expect<Unit>(Unit) { RedGreenIntArrayRange(1..2).getSubrangeOfColor(RedGreenColor.Red, limitByRange = IntRange(2, 2)) }
    assertFailsWith<IllegalArgumentException> { RedGreenIntArrayRange(1..2).subrangesIterator(IntRange(2, 1)) }
    assertFailsWith<IllegalArgumentException> { RedGreenIntArrayRange(1..2).getSubrangeOfColor(RedGreenColor.Red, limitByRange = IntRange(2, 1)) }
    assertFailsWith<IllegalArgumentException> { RedGreenIntArrayRange(1..2).subrangesIterator(0..3) }
    assertFailsWith<IllegalArgumentException> { RedGreenIntArrayRange(1..2).getSubrangeOfColor(RedGreenColor.Red, limitByRange = IntRange(0, 3)) }
    assertFailsWith<IllegalArgumentException> { RedGreenIntArrayRange(1..2).getSubrangeOfColor(RedGreenColor.Red, 0) }
    assertFailsWith<IllegalArgumentException> { RedGreenIntArrayRange(1..2).getSubrangeOfColor(RedGreenColor.Red, -1) }
  }
}
