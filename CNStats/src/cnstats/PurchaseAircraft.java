/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cnstats;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.*;
import java.util.regex.*;
import java.util.*;


public class PurchaseAircraft {

    private CNSession session;

    public static final String AIRCRAFT_PURCHASE_URL = "http://www.cybernations.net/aircraft_purchase.asp";
    public static final String AIRCRAFT_DESTROY_URL = "http://www.cybernations.net/aircraft_purchase_destroy.asp";
    public PurchaseAircraft(CNSession sesh) {
        session = sesh;
    }

    public void purchaseAircraft(String type, Integer amount) {
        Document doc = session.getRequest(AIRCRAFT_PURCHASE_URL);
        String specialValue = session.getSpecialValue(doc);
        Map<String, Integer> aircraftCount = getAircraftCountImpl(doc);
        int totalCount = sumCountMap(aircraftCount);
        
        if (amount <= 0)
            return;
        if ((totalCount+amount) <= 105)
            amount = 105-totalCount;
        if (!aircraftCount.containsKey(type))
            return;
        
        purchaseAircraftImpl(type, amount, specialValue);
    }
    
    private void purchaseAircraftImpl(String type, Integer amount, String specialValue)
    {
        //first we must 'click' on the aircraft
        Map <String, String> postData = new HashMap<>();
        postData.put("aircraft", type);
        Document firstPost = session.postRequest(AIRCRAFT_PURCHASE_URL + "#Purchase", postData);
        Utility.writeAll("purchaseAircraft1.txt", firstPost.body().html());

        postData.clear();
        postData.put("amount_purchase", amount.toString());
        postData.put("process", "1");
        postData.put("VALUE", specialValue);
        String url = AIRCRAFT_PURCHASE_URL + "?aircraft=" + type.replaceAll(" ", "%20");
        Document secondPost = session.postRequest(url, postData);

        Utility.writeAll("purchaseAircraft2.txt", secondPost.body().html());
    }

    public void decomissionAircraft(String type, Integer amount)
    {
        Document doc = session.getRequest(AIRCRAFT_PURCHASE_URL);
        String specialValue = session.getSpecialValue(doc);
        Map<String, Integer> aircraftCount = getAircraftCountImpl(doc);
        
        if (amount <= 0)
            return;
        if (!aircraftCount.containsKey(type))
            return;
        //If trying to decomission more than you own, only decomission the number you own
        if (amount > aircraftCount.get(type))
            amount = aircraftCount.get(type);
        
        decomissionAircraftImpl(type, amount, specialValue);
    }
    
    private void decomissionAircraftImpl(String type, Integer amount, String specialValue)
    {
        //first we must 'click' on the aircraft
        Map <String, String> postData = new HashMap<>();
        postData.put("Decommission", amount.toString());
        postData.put("Submit", "Decom");
        String url = String.format("%s?Aircraft=%s&VALUE=%s", AIRCRAFT_DESTROY_URL, type.replaceAll(" ", "%20"), specialValue);
        Document firstPost = session.postRequest(url, postData);
        Utility.writeAll("destroyAircraft1.txt", firstPost.body().html());
    }

    public Map<String, Integer> getAircraftCount() {
        Document doc = session.getRequest(AIRCRAFT_PURCHASE_URL);
        return getAircraftCountImpl(doc);
    }

    private Map<String, Integer> getAircraftCountImpl(Document doc) {
        Map<String, Integer> aircraftCount = new HashMap<>();

        Element table = doc.select("table[id=table1]").select("table[id=table17]").first().child(0);
        for (Element e : table.children()) {
            if (e.children().size() == 4) {
                aircraftCount.put(e.child(0).ownText(), Integer.parseInt(e.child(1).ownText()));
                aircraftCount.put(e.child(2).ownText(), Integer.parseInt(e.child(3).ownText()));
            }
        }
        return aircraftCount;
    }

    private int sumCountMap(Map<String, Integer> data) {
        int sum = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            sum += entry.getValue();
        }
        return sum;
    }
}
