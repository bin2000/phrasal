package mt.train;

import java.util.*;
import java.io.*;

import mt.base.IOTools;
import mt.base.Sequence;
import mt.base.SimpleSequence;
import mt.base.IString;
import mt.base.IStrings;
import edu.stanford.nlp.util.BitSetUtils;

/**
 * Toolkit for extracting source-language n-grams that must be considered
 * for feature extraction.
 *
 * @author Michel Galley
 */
 
public class SourceFilter {

  public static final String SHOW_PHRASE_RESTRICTION_PROPERTY = "ShowPhraseRestriction";
  public static final boolean SHOW_PHRASE_RESTRICTION = 
    Boolean.parseBoolean(System.getProperty(SHOW_PHRASE_RESTRICTION_PROPERTY, "false"));

  /**
   * Restrict feature extraction to source-language phrases that appear in 
   * a given test/dev corpus.
   *
   * @param fFilterCorpus
   */
  @SuppressWarnings("unchecked")
  public static List<int[]> getPhrasesFromFilterCorpus(String fFilterCorpus, int maxPhraseLenF, int maxSpanF, boolean addBoundaryMarkers) {
    AlignmentTemplates tmpSet = new AlignmentTemplates();
    System.err.println("Filtering against corpus: "+fFilterCorpus);
    System.err.println("MaxSpanF: "+maxSpanF);
    //filterFromDev = true;
    try {
      LineNumberReader fReader = IOTools.getReaderFromFile(fFilterCorpus);
      int lineNb = 0;
      for (String fLine; (fLine = fReader.readLine()) != null; ) {
        if(maxSpanF < Integer.MAX_VALUE) {
          assert(!addBoundaryMarkers);
          if(lineNb % 10 == 0)
            System.err.printf("line %d...\n", lineNb);
          extractDTUPhrasesFromLine(tmpSet, fLine, maxPhraseLenF, maxSpanF);
        }
        extractPhrasesFromLine(tmpSet, fLine, maxPhraseLenF, addBoundaryMarkers);
        ++lineNb;
      }
      fReader.close();
    } catch(IOException e) {
      e.printStackTrace();
    }
    System.err.printf("Filtering against %d phrases.\n", tmpSet.sizeF());
    System.gc(); System.gc(); System.gc();
    long totalMemory = Runtime.getRuntime().totalMemory()/(1<<20);
    long freeMemory = Runtime.getRuntime().freeMemory()/(1<<20);
    System.err.printf("totalmem = %dm, freemem = %dm\n", totalMemory, freeMemory);
    List<int[]> phrases = new ArrayList<int[]>(tmpSet.sizeF());
    for(int i=0; i<tmpSet.sizeF(); ++i)
      phrases.add(tmpSet.getF(i));
    Collections.shuffle(phrases);
    return phrases;
  }

  private static void extractPhrasesFromLine(AlignmentTemplates set, String fLine, int maxPhraseLenF, boolean addBoundaryMarkers) {
    fLine = fLine.trim();
    //System.err.printf("line: %s\n", fLine);
    if(addBoundaryMarkers)
      fLine = new StringBuffer("<s> ").append(fLine).append(" </s>").toString();
    Sequence<IString> f = new SimpleSequence<IString>(true, IStrings.toIStringArray(fLine.split("\\s+")));
    for(int i=0; i<f.size(); ++i) {
      for(int j=i; j<f.size() && j-i<maxPhraseLenF; ++j) {
        Sequence<IString> fPhrase = f.subsequence(i,j+1);
        if(SHOW_PHRASE_RESTRICTION)
          System.err.printf("restrict to phrase (i=%d,j=%d,M=%d): %s\n",i,j,maxPhraseLenF,fPhrase.toString());
        set.addForeignPhraseToIndex(fPhrase);
      }
    }
  }

  private static void extractDTUPhrasesFromLine(AlignmentTemplates set, String fLine, int maxPhraseLenF, int maxSpanF) {
    fLine = fLine.trim();
    Sequence<IString> f = new SimpleSequence<IString>(true, IStrings.toIStringArray(fLine.split("\\s+")));
    for(int i=0; i<f.size(); ++i) {
      for(int j=i; j<f.size() && j-i<maxSpanF; ++j) {
        // TODO: make this polynomial instead of exponential!! (e.g., use suffix arrays for phrase extraction)
        if(j-i <= 1) {
          Sequence<IString> fPhrase = f.subsequence(i,j+1);
          if(SHOW_PHRASE_RESTRICTION)
            System.err.printf("restrict to phrase (i=%d,j=%d,M=%d): %s\n",i,j,maxPhraseLenF,fPhrase.toString());
          set.addForeignPhraseToIndex(fPhrase);
        } else {
          int bits = (j-i)-1;
          int combinations = 1 << bits;
          int firstBit = 1 << (bits+1);
          for(int k = 0; k < combinations; ++k) {
            int mask = firstBit + (k<<1) + 1;
            BitSet bs = BitSetUtils.toBitSet(mask, i);
            if(bs.cardinality() <= maxPhraseLenF) {
              Sequence<IString> fPhrase = DiscontinuousSubSequences.subsequence(f, bs, null);
              if(SHOW_PHRASE_RESTRICTION)
                System.err.printf("restrict to dtu (i=%d,j=%d,M=%d): %s\n",i,j,maxPhraseLenF,fPhrase.toString());
              set.addForeignPhraseToIndex(fPhrase);
            }
          }
        }
      }
    }
  }


  /**
   * Restrict feature extraction to a pre-defined list of source-language phrases.
   */
  @SuppressWarnings("unchecked")
  public static List<int[]> getPhrasesFromList(String fileName) {
    List<int[]> list = new ArrayList<int[]>();
    System.err.println("Filtering against list: "+fileName);
    //filterFromDev = true;
    try {
      LineNumberReader fReader = IOTools.getReaderFromFile(fileName);
      for (String fLine; (fLine = fReader.readLine()) != null; ) {
        int[] f = IStrings.toIntArray(IStrings.toIStringArray(fLine.split("\\s+")));
        if(SHOW_PHRASE_RESTRICTION)
          System.err.printf("restrict to phrase: %s\n",f.toString());
        list.add(f);
      }
      fReader.close();
    } catch(IOException e) {
      e.printStackTrace();
    }
    return list;
  }
}