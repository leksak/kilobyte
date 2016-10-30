package common.instruction;

import io.atlassian.fugue.Either;
import lombok.Value;

/**
 * The {@code InstructionPrototype} class provides a unified interface for
 * instantiating {@code InstructionPrototype} instances through user-supplied
 * and intermediary representations of a InstructionPrototype.
 *
 * For example the interface accepts bare Strings, as well as the String
 * wrapper class {@code MnemonicRepresentation} that has verified that the
 * String is well-formed. This rich interface is provided for brevity,
 * especially with respect to testing and ease-of-use.
 *
 * It is the responsibility of the caller to supply valid representations
 * of the primitive representations (String & int) to the factory methods
 * that this interface supplies <i>but</i> all the methods perform a
 * best-effort attempt to instantiate a suitable {@code InstructionPrototype}.
 *
 * This means that most of the times the methods prefixed with
 * unsafe should be avoided whenever possible and the functions with
 * the monadic return-value of the
 * {@link io.atlassian.fugue.Either} class.
 */
@Value
public class Instruction {
  String iname;
  int numericRepresentation;
  MnemonicRepresentation mnemonicRepresentation;
  InstructionPrototype prototype;

  static Instruction unsafeFrom(String symbolicRepresentation)
        throws NoSuchInstructionException {
    return unsafeFrom(new MnemonicRepresentation(symbolicRepresentation));
  }

  static Either<InstructionPrototype, PartiallyValidInstruction>
  from(String symbolicRepresentation) throws NoSuchInstructionException {
    return from(new MnemonicRepresentation(symbolicRepresentation));
  }

  static Instruction unsafeFrom(MnemonicRepresentation m)
        throws NoSuchInstructionException {
    throw new NoSuchInstructionException(
          "Couldn't instantiate InstructionPrototype from \"%s\"",
          m);
  }

  static Either<InstructionPrototype, PartiallyValidInstruction>
  from(MnemonicRepresentation m) throws NoSuchInstructionException {
    throw new NoSuchInstructionException(
          "Couldn't instantiate InstructionPrototype from \"%s\"",
          m);
  }

  static Instruction unsafeFrom(int i) throws NoSuchInstructionException {
    throw new NoSuchInstructionException(
          "Couldn't instantiate InstructionPrototype from \"%d\"", i);
  }

  static Either<InstructionPrototype, PartiallyValidInstruction> from(int i)
        throws NoSuchInstructionException {
    throw new NoSuchInstructionException(
          "Couldn't instantiate InstructionPrototype from \"%d\"", i);
  }
}
