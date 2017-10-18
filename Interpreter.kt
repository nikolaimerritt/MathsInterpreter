import MathsLibrary.abs
import MathsLibrary.cos
import MathsLibrary.cosec
import MathsLibrary.cot
import MathsLibrary.csc
import MathsLibrary.e
import MathsLibrary.exp
import MathsLibrary.ln
import MathsLibrary.pi
import MathsLibrary.pow
import MathsLibrary.root
import MathsLibrary.sec
import MathsLibrary.sin
import MathsLibrary.tan
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object Interpreter
{
    private val operators = arrayOf("^", "+", "-", "*", "/", "(", ")")
    private val regularOperators = arrayOf("^", "/", "*", "+", "-")
    private val numerals = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    private val operatorFunctions = hashMapOf<String, (a: Double, b: Double) -> String>(
            Pair("^", { a, b -> pow(a, b).toString() }),
            Pair("%", {a, b -> root(a, b).toString() }),
            Pair("/", { a, b -> (a / b).toString() }),
            Pair("*", { a, b -> (a * b).toString() }),
            Pair("+", { a, b -> (a + b).toString() }),
            Pair("-", { a, b -> (a - b).toString() })
    )
    private val mathsFunctions = hashMapOf<String, (x: Double) -> String>(
            Pair("sin", {x -> sin(x).toString()}),
            Pair("cos", {x -> cos(x).toString()}),
            Pair("tan", {x -> tan(x).toString()}),
            Pair("sec", {x -> sec(x).toString()}),
            Pair("csc", {x -> csc(x).toString()}),
            Pair("cosec", {x -> cosec(x).toString()}),
            Pair("cot", {x -> cot(x).toString()}),
            Pair("abs", {x -> abs(x).toString()}),
            Pair("ln", {x -> ln(x).toString()}),
            Pair("exp", {x -> exp(x).toString()})
    )
    val predefFunctNames = mathsFunctions.keys.toList()

    private val globalVariables = hashMapOf<String, Double>(
            Pair("e", e),
            Pair("pi", pi)
    )
    val predefVarNames = globalVariables.keys.toList()

    fun eval(equationString: String, variables: HashMap<String, Double> = globalVariables): Double
    {
        val parsedEquation = explicitMultiplication(equationToList(equationString, variables))
        return evalEquationList(parsedEquation, variables).toDouble()
    }

    fun derivAt(equation: String, mainVarName: String, mainVal: Double, delta: Double = 0.00001, variables: HashMap<String, Double> = globalVariables): Double
    {
        // managing whether the main variable aready exists. if so, temporarily overwriting value. else, declaring and then removing
        val addedVariable = mainVarName !in variables.keys
        var oldMainVarValue = 0.0
        if (addedVariable) { variables[mainVarName] = mainVal }
        else { oldMainVarValue = variables[mainVarName]!! }

        val y1 = eval(equation, variables)
        val y2 = evalAt(equation, variables, variables[mainVarName]!! + delta)

        if (addedVariable) { variables.remove(mainVarName) }
        else { variables[mainVarName] = oldMainVarValue }
        return (y2 - y1) / delta
    }

    fun areaBetween(equation: String, mainVarName: String, range: Pair<Double, Double>, n: Int = 8, variables: HashMap<String, Double> = globalVariables): Double
    {
        if (n < 2 || n % 2 != 0) { throw IllegalArgumentException("Received $n intervals. Intervals must be positive and even.") }

        val a = range.first
        val b = range.second
        val h = (b-a)/ n

        var approx = evalAt(equation, variables, a) + evalAt(equation, variables, b)
        for (i in 1 until n step 2) { approx += 4 * evalAt(equation, variables, a + i*h, mainVarName) }
        for (i in 2 until n-1 step 2) { approx += 2 * evalAt(equation, variables,a + i*h, mainVarName) }

        return approx * h / 3
    }

    fun addFunction(name: String, body: String, mainVarName: String)
    {
        if (name in mathsFunctions.keys) { throw IllegalArgumentException("$name is already defined.") }
        mathsFunctions.put(name, {x: Double -> evalAt(body, globalVariables, x, mainVarName).toString()})
    }

    fun writeToGlobalVars(varName: String, value: Double) { globalVariables[varName] = value }

    private fun equationToList(equation: String, variables: HashMap<String, Double>): ArrayList<String>
    {
        val equationList = ArrayList<String>()
        val varAndFunctNames = ArrayList<String>()
        varAndFunctNames.addAll(variables.keys.sortedByDescending { it.length })
        varAndFunctNames.addAll(mathsFunctions.keys)

        var i = 0
        whileLoop@ while (i < equation.length)
        {
            if (equation[i] == ' ')
            {
                i++
                continue@whileLoop
            }

            // checking if we've found a variable or function name
            for (varName in varAndFunctNames)
            {
                val varEndIndex = endOfSubstring(equation, i, varName)
                if (varEndIndex != -1)
                {
                    equationList.add(equation.substring(i, varEndIndex))
                    i = varEndIndex
                    continue@whileLoop
                }
            }

            // otherwise, checking if we've found a number
            var digits = 0
            while (i + digits < equation.length && equation[i + digits].toString() in numerals)
            {
                digits++
            }
            if (digits != 0) // if we found a number
            {
                equationList.add(equation.substring(i, i + digits))
                i += digits
                continue@whileLoop
            }

            // otherwise, checking if we've found an operator
            if (equation[i].toString() in operators)
            {
                equationList.add(equation[i].toString())
            }
            i++
        }

        return equationList
    }

    private fun explicitMultiplication(equation: ArrayList<String>): ArrayList<String>
    {
        var i = 1
        while (i < equation.size)
        {
            if ((isNumeric(equation[i - 1]) || isVariable(equation[i - 1]) || equation[i - 1] == ")") && (isVariable(equation[i]) || equation[i] == "("))
            {
                equation.add(i, "*")
            }
            i++
        }
        return equation
    }

    private fun evalEquationList(equationList: ArrayList<String>, variables: HashMap<String, Double>): String
    {
        var equation = equationList
        variables.forEach { name, value -> Collections.replaceAll(equation, name, value.toString()) }

        // going to innermost brackets
        while ("(" in equation && ")" in equation)
        {
            val openBracketI = centralBracketIndex(equation)
            val closeBracketI = equation.subList(openBracketI, equation.size).indexOf(")") + openBracketI
            val insideBrackets = listToArrayList(equation.subList(openBracketI + 1, closeBracketI))
            equation = removeBetween(equation, openBracketI + 1, closeBracketI)
            val valueInBrackets = evalEquationList(insideBrackets, variables)
            equation[openBracketI] = valueInBrackets
        }

        // working out functions
        for (f in mathsFunctions)
        {
            if (f.key in equation)
            {
                var i = equation.indexOf(f.key)
                while (i >= 0 && i < equation.size - 1)
                {
                    val x = equation[i + 1].toDouble()
                    equation[i + 1] = "#"
                    equation[i] = f.value(x)
                    equation.removeIf { it == "#" }
                    i = equation.indexOf(f.key)
                }
            }
        }

        // working out operators
        for (opName in regularOperators)
        {
            if (opName in equation)
            {
                var i = equation.indexOf(opName)
                while (i > 0 && i < equation.size)
                {
                    val a = equation[i - 1]
                    equation[i - 1] = "#"
                    val b = equation[i + 1]
                    equation[i + 1] = "#"
                    equation[i] = operatorFunctions[opName]!!.invoke(a.toDouble(), b.toDouble())
                    equation.removeIf { it == "#" }
                    i = equation.indexOf(opName) // resetting because list has been fucked around with
                }
            }
        }
        return equation[0]
    }

    private fun evalAt(equation: String, variables: HashMap<String, Double>, mainVal: Double, mainVarName: String = "x"): Double
    {
        val mainVariableExists = mainVarName in variables.keys
        var oldMainVarValue = 0.0
        if (!mainVariableExists) { variables[mainVarName] = mainVal }
        else { oldMainVarValue = variables[mainVarName]!! }
        variables[mainVarName] = mainVal

        val retval = eval(equation, variables)

        if (!mainVariableExists) { variables.remove(mainVarName) }
        else { variables[mainVarName] = oldMainVarValue }
        return retval
    }

    private fun isNumeric(stringy: String): Boolean = stringy.replace(".", "")matches("[0-9]+".toRegex())
    private fun isVariable(stringy: String): Boolean = stringy !in operators && stringy !in mathsFunctions.keys && !isNumeric(stringy)

    private fun centralBracketIndex(equationList: ArrayList<String>): Int
    {
        if ("(" !in equationList || ")" !in equationList) return -1

        for (i in 0 until equationList.size)
        {
            val openBracketsBeforeI = equationList.subList(0, i).count { it == "(" }
            val closeBracketsAfterI = equationList.subList(i + 1, equationList.size).count { it == ")" }
            if ((openBracketsBeforeI + 1 == closeBracketsAfterI || openBracketsBeforeI == closeBracketsAfterI) && equationList[i] == "(") return i
        }
        return -1
    }

    private fun <T> removeBetween(arrayList: ArrayList<T>, start: Int, end: Int): ArrayList<T>
    {
        if (start < 0 || end >= arrayList.size) throw IllegalArgumentException("start is $start, and end is $end, but the array list goes up to ${arrayList.size - 1}")
        val residualElements = ArrayList<T>()
        (0 until arrayList.size)
                .filter { it < start || it > end }
                .mapTo(residualElements) { arrayList[it] }
        return residualElements
    }

    private fun <T> listToArrayList(list: List<T>): ArrayList<T>
    {
        val arrayList = ArrayList<T>()
        list.forEach { arrayList.add(it) }
        return arrayList
    }

    private fun endOfSubstring(parentString: String, start: Int, substring: String): Int
    {
        if (parentString.length < start + substring.length || parentString.substring(start, start + substring.length) != substring) return -1 // substring was not immediately after start
        return start + substring.length // substring was immediately after start
    }

    fun <T> printList(array: List<T>)
    {
        for (i in 0 until array.size) { print("${array[i]}${if (i != array.size - 1) ", " else ""}") }
        println()
    }
}