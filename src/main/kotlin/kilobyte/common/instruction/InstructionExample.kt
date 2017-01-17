package kilobyte.common.instruction

import kilobyte.common.instruction.mnemonic.iname
import kilobyte.common.machinecode.MachineCode

/**
 * Container type object
 *
 * Has to be long to deal with overflow
 */
data class
Example(val mnemonicExample: String, val numericExample: MachineCode) {
  constructor(mnemonicExample: String, numericExample: Int) : this(mnemonicExample, numericExample.toLong())

  val iname = mnemonicExample.iname()
}

