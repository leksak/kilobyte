package common.hardware;

public enum Register {
  // Registers 0 through 3
  $zero("Constant 0"),
  $at("Reserved for assembler"),

  $v0("Expression evaluation and results of a function"),
  $v1("Expression evaluation and results of a function"),

  // Registers 4 through 7
  $a0("Argument 1"),
  $a1("Argument 2"),
  $a2("Argument 3"),
  $a3("Argument 4"),

  // Registers 8 through 11
  $t0("Temporary (not preserved across call)"),
  $t1("Temporary (not preserved across call)"),
  $t2("Temporary (not preserved across call)"),
  $t3("Temporary (not preserved across call)"),

  // Registers 12 through 15
  $t4("Temporary (not preserved across call)"),
  $t5("Temporary (not preserved across call)"),
  $t6("Temporary (not preserved across call)"),
  $t7("Temporary (not preserved across call)"),

  // Registers 16 through 19
  $s0("Saved temporary (preserved across call)"),
  $s1("Saved temporary (preserved across call)"),
  $s2("Saved temporary (preserved across call)"),
  $s3("Saved temporary (preserved across call)"),

  // Registers 20 through 23
  $s4("Saved temporary (preserved across call)"),
  $s5("Saved temporary (preserved across call)"),
  $s6("Saved temporary (preserved across call)"),
  $s7("Saved temporary (preserved across call)"),

  // Registers 24 through 27
  $t8("Temporary (not preserved across call)"),
  $t9("Temporary (not preserved across call)"),

  $k0("Reserved for OS kernel"),
  $k1("Reserved for OS kernel"),

  // Registers 28 through 31 (32 total)
  $gp("Pointer to global area"),
  $sp("Stack pointer"),
  $fp("Frame pointer"),

  $ra("Return address (used by function call)"),;
  public int value;
  private final String description;

  Register(String description) {
    this.description = description;
  }

  Register(String description, int initialValue) {
    // TODO: I know gp and sp will need a different value, stub for now
    this.description = description;
    this.value = initialValue;
  }

  public static String toString(int index) {
    return fromIndex(index).toString();
  }

  public static Register fromIndex(int index) {
    return Register.values()[index];
  }

  public static Register fromString(String mnemonic) {
    String sansDollarSign = mnemonic.replace("$", "");
    if (sansDollarSign.matches("[0-9]+")) {
      // A non-symbolic name was passed, such as "$8" as opposed
      // to the symbolic "$t0".
      return fromIndex(Integer.valueOf(sansDollarSign));
    } else {
      // A symbolic name was passed.
      return Register.valueOf(mnemonic);
    }
  }

  public int asInt() {
    return this.ordinal();
  }
}

