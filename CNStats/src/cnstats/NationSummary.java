package cnstats;

import java.util.Date;

/**
 * http://www.cybernations.net/allNations_display.asp?Page=1&Order=DESC&Field=Strength
 * This class holds information grabbed from "Display All Nations" list
 */
public class NationSummary 
{
    public int nationRank;
    public String nationName;
    public String nationURL;
    public int nationID;
    public Date dateCreated;
    public TeamColor nationTeamColor;
    public double nationStrength;
    public double nationInfrastructure;
    public double nationTechnology;
    public int numberOfCruiseMissiles;
    public int numberOfNukes;
    public boolean isInWarMode; 
    
    private NationDetails details = null;
    private CNSession session;
    
    public NationSummary(CNSession sesh)
    {
        session = sesh;
    }
    
    public NationDetails getDetails()
    {
        if (details == null)
            details = new NationDetails(session, "http://www.cybernations.net/" + nationURL);
            
        return details;
    }
    public enum TeamColor
    {
        Pink,
        Purple,
        Black,
        White,
        Green,
        Blue,
        Red,
        Yellow,
        Brown,
        Orange,
        Maroon,
        Aqua,
        None
    }
}
