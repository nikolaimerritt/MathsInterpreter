object MathsLibrary
{
    val pi: Double = 3.141592658579323846264338327950288419716936638
    val e: Double = 2.7182818284590452353602874713526624977572470937

    fun sin(theta: Double): Double
    {
        if (theta in 0.0 .. pi/2) // this can be defined by Taylor polynomials
        {
            var approximation = theta
            for (i in 1 until 5)
            {
                val d = i.toDouble()
                val k = 2*d + 1
                approximation += pow(-1.0, d) * pow(theta, k) / fact(k.toInt())
            }
            return approximation
        }
        if (theta in -pi/2 .. 0.0) { return -sin(-theta) }
        if (theta in -pi .. pi) // using sin(a+b) = sin(a) cos(b) + cos(a) sin(b)
        {
            val a = theta - pi/2
            val b = theta - a
            return sin(a) * cos(b) + cos(a) * sin(b)
        }
        if (theta in -2*pi .. 2*pi) { return -sin(theta - pi) } // using sin(pi to 2 pi) = -sin(0 to pi)
        return sin(theta - 2*pi * (theta/(2*pi)).toInt() ) // because sin(x) repeats itself
    }

    fun cos(theta: Double) = sin(pi/2 - theta)

    fun tan(theta: Double): Double
    {
        if (cos(theta) == 0.0) { throw IllegalArgumentException("tan(x) is undefined for x = $theta") }
        return sin(theta) / cos(theta)
    }

    fun sec(theta: Double): Double
    {
        if (cos(theta) == 0.0) { throw IllegalArgumentException("sec(x) is undefined for x = $theta") }
        return 1.0/ cos(theta)
    }

    fun csc(theta: Double): Double
    {
        if (sin(theta) == 0.0) { throw IllegalArgumentException("cosec(x) is undefined for x = $theta") }
        return 1.0/ sin(theta)
    }

    fun cosec(theta: Double) = csc(theta)

    fun cot(theta: Double): Double
    {
        if (tan(theta) == 0.0) { throw IllegalArgumentException("cot(x) is undefined for x = $theta") }
        println("x = $theta")
        return 1.0/tan(theta)
    }

    fun abs(x: Double): Double
    {
        if (x >= 0) return x
        return -x
    }

    fun fact(n: Int): Int
    {
        if (n < 0) throw IllegalArgumentException("n is $n, which is less than 0.")

        if (n == 0 || n == 1) return 1

        var value = 1
        for (i in 1 until n + 1) { value *= i }
        return value
    }

    fun pow(x: Double, p: Double): Double
    {
        if (canBeInt(p))
        {
            if (p >= 0)
            {
                val pInt = p.toInt()

                var answer = 1.0
                for (i in 0 until pInt) { answer *= x
                }
                return answer
            }
            else { return 1.0 / pow(x, abs(p)) }
        }
        // using the fact that x^(a/b) = root(x^a, b)
        val fracPower = Fraction(p)
        return root(pow(x, fracPower.a.toDouble()), fracPower.b.toDouble())
    }

    fun root(x: Double, root: Double, iterations: Int = 30): Double
    {
        checkForIllegalIterations(iterations)
        if (root == 0.0) { throw IllegalArgumentException("root is 0, which is undefined") }
        if (root == 1.0 || x == 1.0) return x

        // using the fact that x^(1/r) = e^(ln (x)/r)
        return exp(ln(x, iterations)/root)
    }

    fun ln(x: Double, iterations: Int = 30): Double
    {
        checkForIllegalIterations(iterations)
        if (x <= 0) { throw IllegalArgumentException("x is $x, which is less than or equal to 0") }
        if (x == 1.0) { return e }
        if (x == 10.0) { return 2.30258509299 }

        if (x < 2) // using the taylor expansion ln(x) = y  - y^2 / 2     + y^3 / 3  - y^4 / 4 + ...
        {
            val y = x - 1
            return y + (2 until iterations)
                    .map { it.toDouble() }
                    .sumByDouble { pow(-1.0, it + 1) * pow(y, it) / it }
        }

        // using the fact that ln(3456.789) = ln(0.3456789 * 10^4) = ln(0.3456789) + 4*ln(10)
        val powerOf10 = x.toString().substringBefore(".").length.toDouble()
        val xToDecimalLessThan1 = x / pow(10.0, powerOf10)
        return ln(xToDecimalLessThan1) + powerOf10 * ln(10.0)
    }

    fun exp(x: Double, iterations: Int = 15): Double
    {
        checkForIllegalIterations(iterations)
        if (canBeInt(x)) return pow(e, x)

        return (0 until iterations)
                .map { it.toDouble() }
                .sumByDouble { pow(x, it) / fact(it.toInt()) }
    }

    fun canBeInt(n: Double) = abs(n - n.toInt()) <= Double.MIN_VALUE
    fun checkForIllegalIterations(iterations: Int)
    {
        if (iterations < 1) { throw IllegalArgumentException("You asked for $iterations iterations, which is less than 1.") }
    }
}