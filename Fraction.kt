import MathsLibrary.pow

class Fraction(userA: Int, userB: Int)
{
    var a = userA; private set
    var b = userB; private set
    init { simplify() }
    val isUndefined = b == 0
    val decimal = if (!isUndefined) a / b else null

    constructor(n: Double): this(1, 1)
    {
        this.b = pow(10.0, n.toString().substringAfter(".").length.toDouble()).toInt()
        this.a = (n * this.b).toInt()
        simplify()
    }

    companion object
    {
        val UNDEFINED = Fraction(1, 0)
        val MAX_FRACTION = Fraction(Int.MAX_VALUE, 1)
        val MIN_FRACTION = MAX_FRACTION.getReciprocal()
    }

    override fun equals(other: Any?): Boolean
    {
        if (other is Fraction) { return this.a == other.a && this.b == other.b }
        return super.equals(other)
    }

    override fun toString(): String = "($a / $b)"

    override fun hashCode(): Int
    {
        var result = a
        result = 31 * result + b
        return result
    }

    operator fun unaryMinus() = Fraction(-a, b)

    operator fun plus(other: Fraction) = Fraction(this.a * other.b + this.b * other.a, this.b * other.b)
    operator fun plus(other: Double) = this + Fraction(other)

    operator fun minus(other: Fraction) = Fraction(this.a * other.b - other.a * this.b, this.b * other.b)
    operator fun minus(other: Double) = this - Fraction(other)

    operator fun times(other: Fraction) = Fraction(this.a * other.a, this.b * other.b)
    operator fun times(other: Double) = this * Fraction(other)

    operator fun div(other: Fraction) = if (!other.isUndefined) this * other.getReciprocal() else Fraction.UNDEFINED
    operator fun div(other: Double) = this / Fraction(other)

    operator fun compareTo(other: Fraction): Int
    {
        val compared = this - other
        if (compared.a > 0) { return 1 }
        if (compared.a < 0) { return -1 }
        return 0
    }

    fun getReciprocal() = Fraction(this.b, this.a)

    private fun simplify()
    {
        var i = 2
        while (i < b && i < a && a != 1 && b != 1) // reducing fraction
        {
            if (a % i == 0 && b % i == 0)
            {
                a /= i
                b /= i
                i = 2
            }
            else { i++ }
        }
        if (b < 0) // turning (a/-b) -> (-a/b) and (-a/-b) -> (a/b)
        {
            b = -b
            a = -a
        }
    }
}