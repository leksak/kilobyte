package common.instruction

sealed class ConditionResult {
  class Success(): ConditionResult()
  class Failure(val error: String): ConditionResult()
}

class Condition(val f: (n: Int) -> ConditionResult) {
  fun eval(n: Int): ConditionResult = f(n)
}
