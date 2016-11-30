package decompiler;

import com.google.common.collect.Lists;
import common.instruction.DecompiledInstruction;
import common.instruction.Instruction;
import kotlin.collections.EmptyList;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.val;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Value
public class CommandLineDecompiler {
  static Options options = new Options()
        .addOption("h", "help", false, "print this message")
        .addOption("n", "number(s)", true, "disassemble 32-bit word(s) from stdin")
        .addOption("headerless", "suppress table header")
        .addOption("printsupported", "print all supported instructions");

  static CommandLineParser parser = new DefaultParser();
  static HelpFormatter formatter = new HelpFormatter();

  private static CommandLine parse(String... args) throws ParseException {
    return parser.parse(options, args);
  }

  private static void printUsage() {
    formatter.printHelp("MachineCodeDecoder [OPTION] [file|number]...", options);
  }

  private static DecompiledInstruction decompile(Long number) {
    return DecompiledInstruction.from(number);
  }

  private static List<DecompiledInstruction> decompile(File f) {
    return decompile(MachineCodeDecoder.decode(f));
  }

  private static void printSupportedInstructions() {
    Instruction.printInstructionSet(false);
  }

  private static List<DecompiledInstruction> decompile(List<Long> numbers) {
    return Lists.transform(numbers, CommandLineDecompiler::decompile);
  }


  private CommandLineDecompiler() {
    // Intentionally left empty
  }

  public static void main(String[] args) throws IOException {
    CommandLine line;
    try {
      line = parse(args);

      if (line.hasOption("help")) {
        printUsage();
        return;
      }
    } catch (ParseException e) {
      printUsage();
      System.err.println("Parsing the command-line failed. Error: " + e.getMessage());
      return;
    }

    if (line.hasOption("printsupported")) {
      printSupportedInstructions();
      return;
    }

    boolean printTableHeader = true;

    if (line.hasOption("headerless")) {
      printTableHeader = false;
    }

    List<DecompiledInstruction> decompiledInstructions = new ArrayList<>();
    String[] argv = line.getArgs();

    if (line.hasOption("n")) {
      // The user has passed integers on the command line
      // TODO: Test what happens when a number has a bad format
      List<String> inputs = Arrays.asList(line.getOptionValues("n"));
      decompiledInstructions = CommandLineDecompiler.decompile(MachineCodeDecoder.decode(inputs));
    } else if (argv.length > 0) {
      // Passed a list of files.
      for (String arg : argv) {
        // Decode the contents of each file
        decompiledInstructions.addAll(CommandLineDecompiler.decompile(new File(arg)));
      }
    } else if (argv.length == 0) {
      // Decompile from standard in
      List<Long> numbers = MachineCodeDecoder.decode(new InputStreamReader(System.in));
      decompiledInstructions = CommandLineDecompiler.decompile(numbers);
    }

    outputTable(printTableHeader, decompiledInstructions);
  }

  private static void outputTable(boolean printTableHeader, List<DecompiledInstruction> decompiledInstructions) {
    String formatString = "%-12s  |  %-6s  |  %-16s  |  %-22s  |  %-20s   |  %s";
    Object[] header = {"Machine Code", "Format", "Decomposition", "Decomposition hex", "Source", "Errors"};

    if (printTableHeader) {
      System.out.println(String.format(formatString, header));
    }

    for (DecompiledInstruction d : decompiledInstructions) {
      String unpretty = d.toString();
      String[] split = unpretty.split("\\s+");
      String machineCode = split[0];
      String format = split[1];
      int firstOpeningBracket = unpretty.indexOf('[');
      int firstClosingBracket = unpretty.indexOf(']', firstOpeningBracket);
      int secondOpeningBracket = unpretty.indexOf('[', firstClosingBracket);
      int secondClosingBracket = unpretty.indexOf(']', secondOpeningBracket);
      String decimalDecomp = unpretty.substring(firstOpeningBracket, firstClosingBracket + 1);
      String hexDecomp = unpretty.substring(secondOpeningBracket, secondClosingBracket + 1);

      List<String> errors = Collections.emptyList();
      String source;

      if (d.isPartiallyValid()) {
        errors = d.errors();
        source = unpretty.substring(secondClosingBracket + 2, unpretty.indexOf("error"));
      } else {
        source = unpretty.substring(secondClosingBracket + 2);
      }

      if (errors.isEmpty()) {
        System.out.println(String.format(formatString, machineCode, format, decimalDecomp, hexDecomp, source, ""));
      } else {
        StringJoiner sj = new StringJoiner("\", \"", "[\"", "\"]");
        errors.forEach(sj::add);
        System.out.println(String.format(formatString, machineCode, format, decimalDecomp, hexDecomp, source, " error(s)=" + sj.toString()));
      }
    }
  }
}