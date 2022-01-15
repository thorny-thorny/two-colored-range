import me.thorny.twoColoredRange.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

enum class RedBlackYellowColor {
  RED,
  BLACK,
  YELLOW,
}

object BrokenIntBoundMath1: BoundMath<Int, Int> {
  override fun add(bound: Int, length: Int) = bound
  override fun subtract(bound: Int, length: Int) = IntBoundMath.subtract(bound, length)
  override fun getLength(start: Int, endExclusive: Int) = IntBoundMath.getLength(start, endExclusive)
}

object BrokenIntBoundMath2: BoundMath<Int, Int> {
  override fun add(bound: Int, length: Int) = IntBoundMath.add(bound, length)
  override fun subtract(bound: Int, length: Int) = bound
  override fun getLength(start: Int, endExclusive: Int) = IntBoundMath.getLength(start, endExclusive)
}

object BrokenIntBoundMath3: BoundMath<Int, Int> {
  override fun add(bound: Int, length: Int) = IntBoundMath.add(bound, length)
  override fun subtract(bound: Int, length: Int) = IntBoundMath.subtract(bound, length)
  override fun getLength(start: Int, endExclusive: Int) = 0
}

class WeirdBound(val value: Int): Comparable<WeirdBound> {
  override fun compareTo(other: WeirdBound): Int {
    return value.compareTo(other.value)
  }

  override fun equals(other: Any?): Boolean {
    if (other is WeirdBound) {
      return value == other.value
    }

    return false
  }

  override fun hashCode(): Int {
    return value
  }
}

object WeirdMath: BoundMath<WeirdBound, Int> {
  override fun add(bound: WeirdBound, length: Int) = WeirdBound(bound.value + length)
  override fun subtract(bound: WeirdBound, length: Int) = WeirdBound(bound.value - length)
  override fun getLength(start: WeirdBound, endExclusive: WeirdBound) = endExclusive.value - start.value
}

class ExtraTwoColoredLinkedRangeTests {
  private fun rangeWithMath(math: BoundMath<Int, Int>): TwoColoredLinkedRange<Int, Int, RedBlackYellowColor> {
    return TwoColoredLinkedRange(1..2, 1, math, RedBlackYellowColor.RED, RedBlackYellowColor.BLACK)
  }

  @Test
  fun testExtraExceptions() {
    val range = rangeWithMath(IntBoundMath)
    assertFailsWith<Exception> { range.getSubrangesOfColor(RedBlackYellowColor.YELLOW) }
    assertFailsWith<Exception> { range.setSubrangeColor(1..2, RedBlackYellowColor.YELLOW) }
    assertFailsWith<Exception> { range.getSubrangeOfColor(RedBlackYellowColor.YELLOW) }
    assertFailsWith<Exception> { rangeWithMath(BrokenIntBoundMath1) }
    assertFailsWith<Exception> { rangeWithMath(BrokenIntBoundMath2) }
    assertFailsWith<Exception> { rangeWithMath(BrokenIntBoundMath3) }
    assertFailsWith<Exception> {
      TwoColoredLinkedRange(1..2, 1, IntBoundMath, RedBlackYellowColor.RED, RedBlackYellowColor.RED)
    }
  }

  @Test
  fun testDerivatives() {
    val intRange = TwoColoredIntLinkedRange(1..2, RedBlackYellowColor.RED, RedBlackYellowColor.BLACK)
    intRange.setSubrangeOtherColor(2..2)
    assertContentEquals(listOf(1..1), intRange.getSubrangesOfDefaultColor())
    assertContentEquals(listOf(2..2), intRange.getSubrangesOfOtherColor())

    val longRange = TwoColoredLongLinkedRange(1L..2L, RedBlackYellowColor.RED, RedBlackYellowColor.BLACK)
    longRange.setSubrangeOtherColor(2L..2L)
    assertContentEquals(listOf(1L..1L), longRange.getSubrangesOfDefaultColor())
    assertContentEquals(listOf(2L..2L), longRange.getSubrangesOfOtherColor(), )
  }

  @Test
  fun testWeirdMath() {
    val range = TwoColoredLinkedRange(
      WeirdBound(1)..WeirdBound(2),
      1,
      WeirdMath,
      RedBlackYellowColor.RED,
      RedBlackYellowColor.BLACK,
    )

    // Just to cover non-standard ranges related code
    range.setSubrangeOtherColor(WeirdBound(1)..WeirdBound(1))
    range.setSubrangeOtherColor(WeirdBound(2)..WeirdBound(2))
    assertContentEquals(listOf(WeirdBound(1)..WeirdBound(2)), range.getSubrangesOfOtherColor())
  }
}