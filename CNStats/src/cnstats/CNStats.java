
package cnstats;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class CNStats {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ParseException
    {
        CNSession sesh = new CNSession("user name", "password");
        
        //section for checking aircraft count and repurchasing if under max value
        
        //sesh.AircraftPurchase.decomissionAircraft("Yakovlev Yak-9", 1);
        //int max = sesh.SoldierPurchase.getMaximumSoldierPurchase();
//        int raptorCount = sesh.AircraftPurchase.getAircraftCount().get("F-22 Raptor");
//        System.out.printf("Raptor Count: %d\n", raptorCount);
//        sesh.AircraftPurchase.purchaseAircraft("F-22 Raptor", 1);
//        raptorCount = sesh.AircraftPurchase.getAircraftCount().get("F-22 Raptor");
//        System.out.printf("Raptor Count After Purchase: %d\n", raptorCount);
        
        //section for checking soldier count and repurchasing if under max value
        int soldierCount = sesh.SoldierPurchase.getMaximumSoldierPurchase();
        System.out.printf("Soldiers Available for Purchase: %d\n", soldierCount);
        sesh.SoldierPurchase.buysellSoldiers(soldierCount);
        soldierCount = sesh.SoldierPurchase.getMaximumSoldierPurchase();
        System.out.printf("Soldiers Available for Purchase: %d\n", soldierCount);
        
        sesh.logout();
        /*
        ArrayList<NationSummary> summaries = nsp.getNationSummaryList(1, 20);
        
        ArrayList<NationSummary> sortedList = new ArrayList<>();
        for(int i = 0; i < summaries.size(); i++)
        {
            NationSummary sum = summaries.get(i);
            if (sum.nationStrength > 400000 && sum.nationTeamColor != NationSummary.TeamColor.Pink)
            {
                sortedList.add(sum);
            }
        }
        
        for (NationSummary sum : sortedList)
        {
            System.out.printf("%s Strength:%.2f\n", sum.nationName, sum.nationStrength);
            NationDetails detail = sum.getDetails();
        }*/
    }
    
}
