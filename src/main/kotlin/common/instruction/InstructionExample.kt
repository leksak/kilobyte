package common.instruction

import common.machinecode.MachineCode

/**
 * Container type object
 *
 * Has to be long to deal with overflow
 */
data class
Example(val mnemonicExample: String, val numericExample: MachineCode)

