package common.instruction;

import io.atlassian.fugue.Either;

/**
 * The {@code Instruction} class provides a unified interface for
 * instantiating {@code Instruction} instances through user-supplied
 * and intermediary representations of a Instruction.
 *
 * For example the interface accepts bare Strings, as well as the String
 * wrapper class {@code MnemonicRepresentation} that has verified that the
 * String is well-formed. This rich interface is provided for brevity,
 * especially with respect to testing and ease-of-use.
 *
 * It is the responsibility of the caller to supply valid representations
 * of the primitive representations (String & int) to the factory methods
 * that this interface supplies <i>but</i> all the methods perform a
 * best-effort attempt to instantiate a suitable {@code Instruction}.
 *
 * This means that most of the times the methods prefixed with
 * unsafe should be avoided whenever possible and the functions with
 * the monadic return-value of the
 * {@link io.atlassian.fugue.Either} class.
 */
public class Instruction {
  static Instruction unsafeFrom(String symbolicRepresentation)
        throws NoSuchInstructionException {
    throw new NoSuchInstructionException(
          "Couldn't instantiate Instruction from \"%s\"",
          symbolicRepresentation);
  }

  static Either<Instruction, PartiallyValidInstruction>
  from(String symbolicRepresentation) throws NoSuchInstructionException {
    throw new NoSuchInstructionException(
          "Couldn't instantiate Instruction from \"%s\"",
          symbolicRepresentation);
  }

  static Instruction unsafeFrom(int i) throws NoSuchInstructionException {
    throw new NoSuchInstructionException(
          "Couldn't instantiate Instruction from \"%d\"", i);
  }

  static Either<Instruction, PartiallyValidInstruction> from(int i)
        throws NoSuchInstructionException {
    throw new NoSuchInstructionException(
          "Couldn't instantiate Instruction from \"%d\"", i);
  }
}
