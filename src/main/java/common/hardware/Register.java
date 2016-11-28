package common.hardware;

import decompiler.MachineCodeDecoder;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Within the MIPS32 architecture there are 32 general-purpose registers,
 * all of which are defined in this enum. The registers when written out,
 * are preceded by a {@code $} (dollar-sign).  We use
 * two formats for addressing a particular register, either
 * we use the raw-numeric indices i.e. {@code $0}
 * through {@code $31}. Or, using their equivalent mnemonic
 * representations, for instance {@code $t1}.
 */
public enum Register {
  // Registers 0 through 3
  $zero("Constant 0", 0),
  $at("Reserved for assembler", 1),

  $v0("Expression evaluation and results of a function", 2),
  $v1("Expression evaluation and results of a function", 3),

  // Registers 4 through 7
  $a0("Argument 1", 4),
  $a1("Argument 2", 5),
  $a2("Argument 3", 6),
  $a3("Argument 4", 7),

  // Registers 8 through 11
  $t0("Temporary (not preserved across call)", 8),
  $t1("Temporary (not preserved across call)", 9),
  $t2("Temporary (not preserved across call)", 10),
  $t3("Temporary (not preserved across call)", 11),

  // Registers 12 through 15
  $t4("Temporary (not preserved across call)", 12),
  $t5("Temporary (not preserved across call)", 13),
  $t6("Temporary (not preserved across call)", 14),
  $t7("Temporary (not preserved across call)", 15),

  // Registers 16 through 19
  $s0("Saved temporary (preserved across call)", 16),
  $s1("Saved temporary (preserved across call)", 17),
  $s2("Saved temporary (preserved across call)", 18),
  $s3("Saved temporary (preserved across call)", 19),

  // Registers 20 through 23
  $s4("Saved temporary (preserved across call)", 20),
  $s5("Saved temporary (preserved across call)", 21),
  $s6("Saved temporary (preserved across call)", 22),
  $s7("Saved temporary (preserved across call)", 23),

  // Registers 24 through 27
  $t8("Temporary (not preserved across call)", 24),
  $t9("Temporary (not preserved across call)", 25),

  $k0("Reserved for OS kernel", 26),
  $k1("Reserved for OS kernel", 27),

  // Registers 28 through 31 (32 total)
  $gp("Pointer to global area", 28),
  $sp("Stack pointer", 29),
  $fp("Frame pointer", 30),

  $ra("Return offset (used by function call)", 31),
  ;
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

  public static boolean equals(String s1, String s2) {
    return Register.fromString(s1) == Register.fromString(s2);
  }

  public static String toString(int index) {
    return fromIndex(index).toString();
  }

  public static Register fromInt(int machineCode) {
    return fromIndex(machineCode);
  }

  public static Register fromIndex(int index) {
    return Register.values()[index];
  }

  /**
   *
   * @param mnemonic
   * @return
   */
  public static Register fromString(String mnemonic) {
    checkArgument(mnemonic.startsWith("$"), "Registers has to start with a \"$\"");

    String sansDollarSign = mnemonic.replace("$", "");
    if (sansDollarSign.matches("\\d+")) {
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

  /**
   * Of a given address-formatted String ('NUMBER($REG)') $REG will be returned
   * as a Register-class.
   * @param mnemonic  The given address-formatted String
   * @return          Register-class of $REG from the given address-formatted
   *                  String.
   */
  public static Register registerFromOffset(String mnemonic) {
    return fromString(offsetSplitter(mnemonic, false));
  }

  /**
   * Of a given address-formatted String ('NUMBER($REG)') NUMBER will be returned.
   * @param mnemonic  The given address-formatted String
   * @return          NUMBER of the given address-formatted String.
   */
  public static int offsetFromOffset(String mnemonic) {
    return Integer.valueOf(offsetSplitter(mnemonic, true));
  }


  /**
   * Will, if given the address-part('NUMBER($REG)') of a mnemonic-string and
   * will return either the NUMBER representation in base 10 or it will return
   * the register part '$REG' of the string.
   * @param mnemonic    The address-part of a mnemonic-string.
   * @param wantOffset  If true then will return NUMBER, else it will return $REG
   * @return            String representation of requested part of the
   *                    address-string
   * @throws StringIndexOutOfBoundsException  if requesting $REG but the given
   *                                          String was not address-formatted.
   * @throws IllegalArgumentException         If requesting $REG but the given
   *                                          String had not $REG-formatted-
   *                                          address-part.
   */
  private static String offsetSplitter(String mnemonic, boolean wantOffset)
        throws StringIndexOutOfBoundsException, IllegalArgumentException {
    String[] split = mnemonic.replace(")", "").split("[(]");
    //TODO: can there be minus? offset in I-instructions
    if (wantOffset) {
      return String.valueOf(MachineCodeDecoder.decode(split[0], 10));
    }
    /* This will only be called if $REG is wanted from N($REG) */
    if (split.length > 2 || split.length < 2) {
      throw new StringIndexOutOfBoundsException("Invalid offset format for register '"+mnemonic+"'.");
    }
    if (!split[1].matches("\\$[a-z0-9]+")) {
      throw new IllegalArgumentException("Invalid offsetFormat '"+mnemonic+"'");
    }
    return split[1];

  }
}

