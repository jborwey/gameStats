/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cnstats;

import java.util.HashMap;
import java.util.Map;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class PurchaseSoldier {
    CNSession session;
    
    
    public PurchaseSoldier(CNSession sesh)
    {
        session = sesh;
    }
    
    public void buysellSoldiers(Integer amount)
    {
        int maxBuy = getMaximumSoldierPurchase();
        if (amount > maxBuy)
            amount = maxBuy;
        
        Map<String,String> postData = new HashMap<>();
        postData.put("Transaction", amount.toString());
        postData.put("VALUE", session.SpecialValue);
        String url = "http://www.cybernations.net/militarybuysell.asp?Nation_ID=" + session.MyNationID.toString();
        session.postRequest(url, postData);
        
        url = "http://www.cybernations.net/militarybuysell.asp?p=1";
        session.getRequest(url);
    }
    
    public int getMaximumSoldierPurchase()
    {
        String url = "http://www.cybernations.net/militarybuysell.asp?Nation_ID=" + session.MyNationID.toString();
        Document doc = session.getRequest(url);
        
        return getMaxSoldierImpl(doc);
    }
    
    private int getMaxSoldierImpl(Document doc) {
        int maxSoldier = 0;

        Element count = doc.select("i:containsOwn(Maximum Soldier Purchase)").first().parent().parent().child(1);
        maxSoldier = Integer.parseInt(count.select("i").first().ownText().replaceAll(",", ""));
        return maxSoldier;
    }
    
    
    
}
