import me.thorny.twoColoredRange.RedBlackColor
import me.thorny.twoColoredRange.RedBlackIntLinkedRange
import me.thorny.twoColoredRange.RedBlackLongLinkedRange
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ExtraRedBlackLinkedRangeTests {
  @Test
  fun testDefaultColors() {
    var range = RedBlackIntLinkedRange(1..1)
    assertEquals(RedBlackColor.RED, range.defaultColor)
    assertEquals(RedBlackColor.BLACK, range.otherColor)
    range = RedBlackIntLinkedRange(1..1, RedBlackColor.RED)
    assertEquals(RedBlackColor.RED, range.defaultColor)
    assertEquals(RedBlackColor.BLACK, range.otherColor)
    range = RedBlackIntLinkedRange(1..1, RedBlackColor.BLACK)
    assertEquals(RedBlackColor.BLACK, range.defaultColor)
    assertEquals(RedBlackColor.RED, range.otherColor)
  }

  @Test
  fun testLongRange() {
    val range = RedBlackLongLinkedRange(1L..2L)
    range.setSubrangeBlack(2L..2L)
    assertContentEquals(listOf(1L..1L), range.getRedSubranges())
    assertContentEquals(listOf(2L..2L), range.getBlackSubranges())
  }

  @Test
  fun testExtraMethods() {
    val range = RedBlackIntLinkedRange(1..5)
    range.setSubrangeBlack(1..5)
    assertEquals(1..1, range.getBlackSubrange())
    range.setSubrangeRed(1..5)
    assertEquals(1..1, range.getRedSubrange())
  }
}