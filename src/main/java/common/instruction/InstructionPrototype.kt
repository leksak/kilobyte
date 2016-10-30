package common.instruction

/**
 * This is a set of Instructions containing an {@code Instruction}
 * instance of each of the supported operations. These are templates
 * from which we can spawn actual instances of the {@code Instruction}
 * class, i.e. it is from {@code InstructionPrototype.Add} that we
 * create all instances of the "Add" {@code Instruction}.
 *
 * Developers Note:
 *
 * This class was written in Kotlin as it supports named arguments
 * out of the box. This can be mimicked in Java using a Builder
 * pattern but that generates a lot of boilerplate even when
 * using libraries such as Lombok. Also, the Builder pattern is
 * less legible as it is not supported on the language level.
 *
 * By using a language that supports named arguments instead we can
 * alleviate ourselves of a large, verbose and unnecessarily complex
 * code-base. However, we use a single constructor to encompass all
 * different ways of instantiating an instruction, thereby we end
 * up letting a lot of attributes that could effectively be final
 * as being variable. By reducing the visibility of the constructor
 * to the private level we retain security on the API level while
 * striking a good balance in brevity and legibility.
 *
 * Written in Kotlin because of named arguments as there is a
 * cornucopia of ways to instantiate an instruction.
 *
 * The constructor is hidden since only we require it. By limiting
 * its visibility we may define a single constructor with multiple
 * "var" arguments instead of having a cornucopia of constructors.
 *
 * Hence, we trade-off API usage security for brevity, as the usage
 * of this class is very limited.
 *
 * For more information about named arguments refer to the
 * Kotlin documentation available here
 * https://kotlinlang.org/docs/reference/functions.html#named-arguments
 *
 * @property iname The name of the instruction
 * @property opcode The opcode of the instruction
 * @property mnemonicExample A symbolic representation of one instruction instance
 * @property numericExample A numeric representation of <i>the same</i>
 *                       instruction instance as the mnemonicExample property
 * @property description A description of the semantics of the instruction
 * @property format The format of the instruction
 * @property pattern The pattern that the symbolic representation of the
 *                instruction adheres to. For an example, the "add"
 *                instruction is on the form (iname, rd, rs, rt) meaning
 *                that for the instruction instance "add $t1, $t2, $t3"
 *                we have that rd=$t1, rs=$t2, rt=$t3
 * @property type The type of the instruction, if applicable.
 * @property rt If the instruction is identified by the value of its
 *                opcode and the value of the rt field then identRt
 *                should be set to the the same value.
 * @property funct Like the @rt property but for the funct field.
 * @property conditions If applicable: A set of conditions that need to
 *                   apply for a numeric representation of the instruction
 *                   to be valid. The functions shall return "null"
 *                   when the condition is violated.
 */
class InstructionPrototype constructor(
      val iname: String,
      val opcode: Int,
      val mnemonicExample: String,
      val numericExample: Int,
      val description: String,
      val format: Format,
      val pattern: Pattern,
      var type: Type? = null,
      var rt: Int? = null,
      var funct: Int? = null,
      vararg var conditions: Condition = emptyArray()) {
  init {
    // The "init" block is executed each time an InstructionPrototype
    // is initialized.
    println("hej")
  }

  fun from(symbolicRepresentation: String): Instruction {
    throw UnsupportedOperationException()
  }

  fun from(numericRepresentation: Int): Instruction {
    throw UnsupportedOperationException()
  }
}

