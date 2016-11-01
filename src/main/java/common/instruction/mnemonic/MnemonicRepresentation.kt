package common.instruction.mnemonic

import common.instruction.Instruction

class MnemonicRepresentation {
  val standardizedMnemonic: String
  val iname: String

  internal constructor(standardizedMnemonic: String) {
    this.standardizedMnemonic = standardizedMnemonic
    iname = standardizedMnemonic.split(" ")[0]
  }
  
  internal constructor(iname: String,
                       vararg args: String) {
    // TODO: Will also probably have to capture the numeric rep here
    val sj = java.util.StringJoiner(", ")
    args.map { sj.add(it) }
    this.standardizedMnemonic = "%s %s".format(iname, sj.toString())
    this.iname = iname
  }

  companion object Factory {
    @JvmStatic fun fromString(mnemonic: String): MnemonicRepresentation {
      val standardizedMnemonic = standardizeMnemonic(mnemonic)
      val iname = standardizedMnemonic.split(" ")[0]
      return Instruction.getPattern(iname).invoke(standardizedMnemonic)
    }
  }
}
