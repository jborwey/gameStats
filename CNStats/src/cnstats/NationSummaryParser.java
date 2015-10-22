package cnstats;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.*;
import java.util.regex.*;
import org.jsoup.Jsoup;
/**
 * This class provides the methods needed to parse nation data
 * http://www.cybernations.net/allNations_display.asp?Page=1&Order=DESC&Field=Strength
 */
public class NationSummaryParser 
{
    private CNSession session;
    private int numberOfNations;
    
    private boolean success = false;
    public static final String DISPLAY_ALL_NATIONS_URL = "http://www.cybernations.net/allNations_display.asp";
    private static final String DISPLAY_COUNT_REGEX = "Serving (?<fromindex>([0-9]{1,3}+(,[0-9]{3}+)*)) to (?<toindex>([0-9]{1,3}+(,[0-9]{3}+)*)) of (?<count>([0-9]{1,3}+(,[0-9]{3}+)*)) Nations";
    
    private static DateFormat inputDateFormat = new SimpleDateFormat("M/d/yyyy h:mm:ss a");
    
    public NationSummaryParser(CNSession sesh)
    {
        //Grab the display all nations url
        session = sesh;
    }
    
    private void getNumberOfNations(String body)
    {
        //Parse the body to get the number of total nations
        //eg: Serving 1 to 10 of 8,940 Nations
        //First grab the body of the page as a string
        //Now create our regex Pattern and get the matcher
        Matcher m = Pattern.compile(DISPLAY_COUNT_REGEX).matcher(body);
        
        //Grab the regex results
        if (m.find())
        {
            String from = m.group("fromindex").replaceAll(",", "");
            String to = m.group("toindex").replaceAll(",", "");
            String count = m.group("count").replaceAll(",", "");
            numberOfNations = Integer.parseInt(count);
            success = true;
        }
        else
        {
            //throw new Exception("Stats not found");
            success = false;
        }
    }
    
    public ArrayList<NationSummary> getNationSummaryList(int startRank, int count)  throws ParseException
    {
        //Create our Array to be populated
        ArrayList<NationSummary> nations  = new ArrayList<>();
        int startPage = ((startRank-1)/10)+1;
        int endPage = ((startRank+count-2)/10)+1;
        //query all of the pages
        for (int page = startPage; page <= endPage; page++)
        {
            //Create the query url
            String url = String.format("Page=%d&Order=DESC&Field=Strength", page);
            //Grab the nation summaries on the pages
            ArrayList<NationSummary> sum = getNationSummaryPage(url);
            //only add the nations in between the ranks requested
            for (NationSummary nation : sum)
                if (nation.nationRank >= startRank && nation.nationRank < (startRank+count))
                    nations.add(nation);
        }
        
        return nations;
    }
    
    public static ArrayList<NationSummary> parseNationSummary(Document doc, CNSession session) throws ParseException
    {
        //Create our Array to be populated
        ArrayList<NationSummary> nations  = new ArrayList<>();
        
        //get the body
        Elements elements = doc.select("table[bgcolor=#F7F7F7]");
        
        if (elements.size() != 1)
            return nations;
        
        //get tbody
        Element tb = elements.get(0).children().get(0);
        Elements listNations = tb.children();
        
        int numNations = listNations.size()-1;
        //Loop through each nation
        for (int i = 1; i <= numNations; i++)
        {
            //get the element row
            Element tr = listNations.get(i);
            Elements dataItems = tr.children();
            int dataCount = dataItems.size();
            NationSummary summary = new NationSummary(session);
            
            //Go through each data
            for (int r = 0; r < dataCount; r++)
            {
                Element data = dataItems.get(r);
                switch (r)
                {
                    case 0: //nation Rank
                        summary.nationRank = Integer.parseInt(data.ownText().replaceAll("[)]", ""));
                        break;
                    case 1: //Nation Name
                        Element nameLink = data.select("a").first();
                        summary.nationName = nameLink.ownText();
                        summary.nationURL = nameLink.attr("href");
                        int indexOfEqual = summary.nationURL.lastIndexOf("=");
                        summary.nationID = Integer.parseInt(summary.nationURL.substring(indexOfEqual+1));
                        break;
                    case 2: // message
                        break;
                    case 3: //Date created
                        Element center = data.select("center").first();
                        String dateString = center.ownText();
                        summary.dateCreated = inputDateFormat.parse(dateString);
                        break;
                    case 4: //team
                        Element teamImage = data.select("center").first().child(0);
                        String teamStr = teamImage.attr("title");
                        teamStr = teamStr.substring(teamStr.indexOf(":")+2);
                        summary.nationTeamColor = NationSummary.TeamColor.valueOf(teamStr);
                        break;
                    case 5: //strength
                        summary.nationStrength = Double.parseDouble(data.child(0).ownText().replaceAll(",", ""));
                        break;
                    case 6: //infra
                        summary.nationInfrastructure = Double.parseDouble(data.child(0).ownText().replaceAll(",", ""));
                        break;
                    case 7: //tech
                        summary.nationTechnology = Double.parseDouble(data.child(0).ownText().replaceAll(",", ""));
                        break;  
                    case 8: //cruise missile
                    case 9: //nuke
                    case 10: //mode
                        if ((r == 8 && dataCount == 10) || (r == 9 && dataCount == 11) ) //nukes
                        {
                            Element nukeImg = data.select("img").first();
                            String nukeTitle = nukeImg.attr("title");
                            if (nukeTitle.equals("Nation does not support nuclear weapons."))
                            {
                                summary.numberOfNukes = 0;
                            }
                            else
                            {
                                nukeTitle = nukeTitle.replaceAll("This nation owns ", "").replaceAll(" nuclear weapons.", "");
                                summary.numberOfNukes = Integer.parseInt(nukeTitle);
                            }
                        }
                        else if ((r == 8 && dataCount == 11)) //cruise missile
                        {
                            summary.numberOfCruiseMissiles = Integer.parseInt(data.child(0).ownText().replaceAll(",", ""));
                        }
                        else if ((r==9 && dataCount == 10) || (r==10 && dataCount == 11))//mode
                        {
                            summary.isInWarMode = data.child(0).child(0).attr("title").equals("War Mode");
                        }
                        break;  
                }
            }
            nations.add(summary);
        }
        
        return nations;
    }
    
    public ArrayList<NationSummary> getNationSummaryPage(String args)  throws ParseException
    {
        Document doc = session.getRequest(DISPLAY_ALL_NATIONS_URL + "?" + args);
        getNumberOfNations(doc.body().html());
        return parseNationSummary(doc, session);
    }
}
