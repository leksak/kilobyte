package common.instruction

/**
 * An Algebraic Data Type (ADT) enforcing language-level control flow
 * of the different results.
 *
 * In effect, this is akin to the Either type, {@code atlassian.io.fugue.Either},
 * (https://hackage.haskell.org/package/base-4.8.2.0/docs/Data-Either.html)
 * just imbued with additional semantics.
 *
 * Using the sealed keyword we get that ConditionResult has a closed
 * set of possible outcomes. The sealed keyword ensures that there
 * cannot be any additional outcomes for the ConditionResult type.
 *
 * Then, we are forced to (on the language level) evaluate both outcomes,
 * not at all unlike a try-catch block but semantically distinct from an
 * exception.
 *
 * In Kotlin we are forced to use algebraic pattern matching to define
 * what to do depending on the type. It is impossible to omit any
 * of the outcomes.
 *
 * when (result) {
 *   is ConditionResult.Success ->
 *     // Do stuff
 *   is ConditionResult.Failure ->
 *     // Do other stuff
 * }
 *
 * See https://kotlinlang.org/docs/reference/classes.html#sealed-classes
 * for more information.
 *
 * Inspired by: http://engineering.pivotal.io/post/algebraic-data-types-in-kotlin/
 */
sealed class ConditionResult {
  class Success(): ConditionResult()
  class Failure(val error: String): ConditionResult()
}

/**
 * One could argue that this class is superfluous and it is, the conditions
 * can be derived from the parametrized mnemonic representation pattern
 * at the expense of type-checking and the quality of the error messages.
 *
 * For an example, R-format instructions expressed on the form
 *
 * iname rd, rs, rt (such as add)
 *
 * require that the shamt field be zero.
 *
 * Instructions on the form,
 *
 * iname rd, rs (such as clo)
 *
 * require that rt and shamt is zero
 *
 * Instructions on the form
 *
 * iname rs, rt (such as msub)
 *
 * require that rd and shamt is zero
 *
 * Instructions on the form
 *
 * iname rt, imm (such as lui)
 *
 * require that rd is zero
 *
 * Instructions on the form
 *
 * iname rd (such as mfhi)
 *
 * demands that rs, rt, and shamt are all zero.
 *
 */
class Condition(val f: (n: Int) -> ConditionResult, val mask: Int = 0) {
  /**
   * Accept n and evaluate the condition yielding a result.
   */
  fun eval(n: Int): ConditionResult = f(n)
}
