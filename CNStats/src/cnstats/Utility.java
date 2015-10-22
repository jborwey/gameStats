package cnstats;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
/**
 *
 * @author Jeff
 */
public class Utility {
    public static String readToEnd(String filename)
    {
        try
        { 
             byte[] encoded = Files.readAllBytes(Paths.get(filename));
             return new String(encoded, Charset.defaultCharset());
        } catch (Exception ex)
        {
            return "";
        }
    }
    
    public static void writeAll(String filename, String data)
    {
        try
        {
            PrintWriter pw = new PrintWriter(new File(filename));
            pw.append(data);
            pw.close();
        }
        catch (Exception ex)
        {
            System.err.printf("Could not write to file %s\n", filename);
            ex.printStackTrace();
        }
    }
}
