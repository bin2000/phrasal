package mt.visualize.phrase;

import joptsimple.*;
import java.util.List;

public final class PhraseViewer {

  private PhraseViewer() {}

  private static String usage() {
    String cmdLineUsage = String.format("Usage: java PhraseViewer [OPTS]\n");
    StringBuilder sb = new StringBuilder(cmdLineUsage);

    sb.append(" -v    : Verbose output to console\n");
    sb.append(" -s    : Source file\n");
    sb.append(" -o    : Options file\n");
    sb.append(" -x    : Path to translation path schema file (required for loading and saving)\n");

    return sb.toString();
  }

  private final static OptionParser op = new OptionParser("vs:o:x:p:");
  private final static int MIN_ARGS = 0;

  private static boolean VERBOSE = false;
  private static String SRC_FILE = null;
  private static String OPTS_FILE = null;
  private static String XSD_FILE = null;

  private static boolean validateCommandLine(String[] args) {
    //Command line parsing
    OptionSet opts = null;
    List<String> parsedArgs = null;
    try {
      opts = op.parse(args);

      parsedArgs = opts.nonOptionArguments();

      if(parsedArgs.size() < MIN_ARGS)
        return false;

    } catch (OptionException e) {
      System.err.println(e.toString());
      return false;
    }

    VERBOSE = opts.has("v");
    if(opts.has("s"))
      SRC_FILE = (String) opts.valueOf("s");
    if(opts.has("o"))
      OPTS_FILE = (String) opts.valueOf("o");
    if(opts.has("x"))
      XSD_FILE = (String) opts.valueOf("x");

    return true;
  }


  public static void main(String[] args) {
    if(!validateCommandLine(args)) {
      System.err.println(usage());
      System.exit(-1);
    }

    PhraseController pc = PhraseController.getInstance();

    pc.setVerbose(VERBOSE);

    if(SRC_FILE != null && !pc.setSourceFile(SRC_FILE)) {
      System.err.printf("ERROR: %s does not exist!\n", SRC_FILE);
      System.exit(-1);
    }
    if(OPTS_FILE != null && !pc.setOptsFile(OPTS_FILE)) {
      System.err.printf("ERROR: %s does not exist!\n", OPTS_FILE);
      System.exit(-1);
    }
    if(XSD_FILE != null && !pc.setSchemaFile(XSD_FILE)) {
      System.err.printf("ERROR: %s does not exist!\n", XSD_FILE);
      System.exit(-1);
    }

    pc.run();

  } 
}