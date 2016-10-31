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
      val noOfMnemonics = mnemonicExamples.size
      val noOfNumerics = numericExamples.size
      checkArgument(noOfMnemonics == noOfNumerics,
            "Expected the set of mnemonics examples" +
                  " and numeric examples to be of the same size." +
                  "Got $noOfMnemonics and $noOfNumerics, respectively")
      val arr = mutableListOf<InstructionExample>()
      for (i in 0..(mnemonicExamples.size - 1)) {
        arr.add(InstructionExample(mnemonicExamples[i], numericExamples[i]))
      }
      return arr.toTypedArray()
    }
  }
}

