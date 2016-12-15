package common.hardware


/**
 * Within the MIPS32 architecture there are 32 general-purpose registers,
 * all of which are defined in this enum. The registers when written out,
 * are preceded by a {@code $} (dollar-sign).  We use
 * two formats for addressing a particular register, either
 * we use the raw-numeric indices i.e. {@code $0}
 * through {@code $31}. Or, using their equivalent mnemonic
 * representations, for instance {@code $t1}.
 */
class Register(val index: Int, val name: String, val description: String) {
  var value: Int = 0

  override fun toString(): String {
    return name
  }

  companion object {
    @JvmStatic fun equals(s1: String, s2: String): Boolean {
      return RegisterFile[s1] == RegisterFile[s2]
    }
  }
}
