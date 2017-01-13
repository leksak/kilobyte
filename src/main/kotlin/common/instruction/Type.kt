package common.instruction

/**
 * Certain types of instructions may be classified beyond their
 * format. For an example, the {@code bne} (branch on not equal)
 * is on the I-type format but we want to be able to refer to it
 * as a <i>branch</i> instruction. Using the Type enum we may
 * "tag" instructions to describe them further without overwriting
 * information regarding their format.
 */
enum class Type(val longName: String) {
  B("branch"),
  T("trap"),
  E("exception"),
  Interrupt("interrupt"),
  J("jump"), // Some jump instructions are on the r format such as jalr
  SHIFT("shift"),
}

