
package cnstats;

import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import java.util.*;
/**
 *This class holds specific information on a nation
 * It will do an additional query on their nation ID
 */
public class NationDetails {
    public String allianceAffiliation;
    
    //Parse the nation details
    public NationDetails(CNSession session, String url)
    {
        //error checking
        if (session == null || !session.isSuccess())
            return;
        //Grab the page
        Document doc = session.getRequest(url);
        //Grab the table with all of the data on it
        Elements infoTable = doc.select("table[id=table18]");
        Elements rows = infoTable.get(2).child(0).children();
        
        //Put all of the rows in a map;
        Map<String,Element> dataMap = new HashMap<>();
        for (Element e : rows)
        {
            if (e.children().size() >= 2)
            {
                //if it is a link we must extract the link
                String key = e.child(0).ownText();
                Elements link = e.child(0).select("a");
                if (link.size() > 0)
                    key = link.first().ownText() + ":";
                dataMap.put(key, e.child(1));
            }
        }
        
        //now parse all of the data needed
        if (dataMap.containsKey("Alliance Affiliation:"))
        {
            Element e = dataMap.get("Alliance Affiliation:");
            String all = e.ownText();
            System.out.println(all);
            allianceAffiliation = all;
        }
        
    }
}
