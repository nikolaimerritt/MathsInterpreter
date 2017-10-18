fun runPrompt()
{
    val prevAnswerKw = "_"
    val endKw = "end"
    val editVarKw = "var"
    val editFuncKw = "func"
    val evalExpressionKw = "eval"
    val numericDiffKw = "deriv"
    val numericIntegKw = "area"
    val showHelpKw = "help"

    var mostRecentAnswer: Double? = null
    while (true)
    {
        print("$\t")
        var input = readLine()?.replace(" ", "")
        if (input != null)
        {
            if (input == "end") return
            if (mostRecentAnswer != null && prevAnswerKw in input) { input = input.replace(prevAnswerKw, mostRecentAnswer.toString()) }
            else if (prevAnswerKw in input) { println("ERROR: You haven't evaluated anything yet, so $prevAnswerKw has no value.") }
            when
            {
                // editing var. in form var varName = value
                editVarKw in input ->
                {
                    val name = input.replace(editVarKw, "").substringBefore("=")
                    val value = input.substringAfter("=")
                    Interpreter.writeToGlobalVars(name, Interpreter.eval(value))
                    println("Sucessfully assigned $value to $name.")
                }
                // editing function. in form func funcName(x) = 3x^2 + 12
                editFuncKw in input ->
                {
                    val name = input.replace(editFuncKw, "").substringBefore("(")
                    val mainVar = input.substringAfter("(").substringBefore(")")
                    val body = input.substringAfter("=")
                    Interpreter.addFunction(name, body, mainVar)
                    println("Successfully added $name as a function of $mainVar.")
                }
                evalExpressionKw in input ->
                {
                    val toEvaluate = input.replace(evalExpressionKw, "")
                    mostRecentAnswer = Interpreter.eval(toEvaluate)
                    println(mostRecentAnswer)
                }
                // computing derivative. in form deriv x^2 + 2x, x = 1
                numericDiffKw in input ->
                {
                    val function = input.replace(numericDiffKw, "").substringBefore(",")
                    val mainVar = input.substringAfter(",").substringBefore("=")
                    val mainVarValue = Interpreter.eval(input.substringAfter("="))
                    mostRecentAnswer = Interpreter.derivAt(function, mainVar, mainVarValue)
                    println(mostRecentAnswer)
                }
                // computing definite integral. in form area x^2 + 2x, x = 1 to 5
                numericIntegKw in input ->
                {
                    val function = input.replace("area", "").substringBefore(",")
                    val mainVar = input.substringAfter(",").substringBefore("=")
                    val a = Interpreter.eval(input.substringAfter("=").substringBefore("to"))
                    val b = Interpreter.eval(input.substringAfter("to"))
                    mostRecentAnswer = Interpreter.areaBetween(function, mainVar, Pair(a, b))
                    println(mostRecentAnswer)
                }
                showHelpKw in input ->
                {
                    println("This maths interpreter aims to replicate standard notation as much as possible. As such, maths constnants are pre-defined, all pre-defined functions are in lower case, spacing doesn't matter, and multiplication symbols are not required.\n\n")

                    println("============= KEYWORDS =============")
                    println("$prevAnswerKw\t Variable that holds the previous answer")
                    println("$editVarKw\t Declare or edit variable. Syntax: $editFuncKw myVar = sin(0)")
                    println("$editFuncKw\t Declare or edit function of one argument. E.g. $editFuncKw myFunct(theta) = sin(theta) + theta^xy")
                    println("$endKw\t Ends the current session")
                    println("$evalExpressionKw\t Evaluates the current expression. E.g. $evalExpressionKw x^2 + 2t^y + sin(theta)")
                    println("$numericDiffKw\t Numerically differentiates the current expression with respect to one variable. E.g. $numericDiffKw sin(theta + a^2) + z theta, theta = 0")
                    println("$numericIntegKw\t Numerically integrates the current expression across a given interval with respect to one variable. E.g. $numericIntegKw theta^2 + 2 cot(theta^b), theta = 0 to pi")
                    println("$showHelpKw\t Shows this help dialog")

                    println("\n\n============= PRE-DEFINED FUNCTIONS =============")
                    Interpreter.printList(Interpreter.predefFunctNames)
                    println("\n\n============= PRE-DEFINED VARIABLES =============")
                    Interpreter.printList(Interpreter.predefVarNames)
                }
            }
        }
    }
}

fun main(args: Array<String>)
{
    println(Fraction(MathsLibrary.pi).decimal)
}
