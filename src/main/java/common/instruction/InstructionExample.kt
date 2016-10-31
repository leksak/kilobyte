package common.instruction

import com.google.common.base.Preconditions.checkArgument

/**
 * Container type object
 */
data class
InstructionExample(val mnemonicExample: String, val numericExample: Long) {
  companion object Factory {
    @JvmStatic fun examplesFrom(mnemonicExamples: Array<String>,
                                numericExamples: Array<Long>): Array<InstructionExample> {
      assert(mnemonicExamples.size == numericExamples.size)
      val arr = mutableListOf<InstructionExample>()
      for (i in 0..mnemonicExamples.size) {
        arr.add(InstructionExample(mnemonicExamples[i], numericExamples[i]))
      }
      return arr.toTypedArray()
    }
  }
}

