package cnstats;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.auth.login.LoginException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
/**
 * This class provides the methods for logging into cybernations
 * and accessing the credentials later on.
 */
public class CNSession
{
    public static final String CN_LOGIN_URL = "http://www.cybernations.net/login.asp";
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36";
    
    private static final String PURCHASE_SPECIAL_REGEX = "VALUE=(?<special>([0-9A-G]{10}))";
    private boolean loginSuccess = false;
    private final Map<String,String> cookies;
    
    public NationSummaryParser SummaryParser;
    public PurchaseAircraft AircraftPurchase;
    public PurchaseSoldier SoldierPurchase;
    
    public Integer MyNationID;
    public String MyNationURL;
    public String SpecialValue;
    
    String username, password;
    public CNSession(String usr, String pass) throws IOException
    {
        username = usr;
        password = pass;
        
        Connection.Response loginForm = Jsoup.connect(CN_LOGIN_URL)
            .userAgent(USER_AGENT)
            .referrer("http://www.google.com") 
            .method(Connection.Method.GET).execute();
        cookies = loginForm.cookies();
        
        System.out.println("Printing Cookies:");
        for (Map.Entry<String,String> entry : cookies.entrySet())
        {
            System.out.printf("'%s' => '%s'\n", entry.getKey(), entry.getValue());
        }
        
        Map<String,String> postData = new HashMap<>();
        postData.put("Username", username);
        postData.put("Validate_Password", password);
        postData.put("checkbox", "1");
        postData.put("Login", "Login");
        Document document = postRequest(CN_LOGIN_URL, postData);

        //check if we had a success or not
        String documentString = document.html();
        if (documentString.contains("Sorry but you entered an invalid username or password.")) 
        {
            //throw new LoginException("Could not login with those credentials");
            loginSuccess = false;
        } else if (documentString.contains("You are now logged in"))
        {
            loginSuccess = true;
            
            //Get Nation ID
            Element e = document.select("ul[id=popmenu3]").select("a:contains(View My Nation)").first();
            MyNationURL = "http://www.cybernations.net/" + e.attr("href");
            MyNationID = Integer.parseInt(MyNationURL.substring(MyNationURL.lastIndexOf("=")+1));
            
            SpecialValue = getSpecialValue(document);
            
            //Initialize our Handlers
            SummaryParser = new NationSummaryParser(this);
            AircraftPurchase = new PurchaseAircraft(this);
            SoldierPurchase = new PurchaseSoldier(this);
        }
        else
        {
            throw new IOException("Error Logging in");
        }
    }
    
    public void logout()
    {
        Document doc = getRequest("http://www.cybernations.net/default.asp");
        Element e = doc.select("a[title=Logout of the game]").first();
        String href = e.attr("href");
        String logoutURL = "http://www.cybernations.net" + href;
        Document response = getRequest(logoutURL);
    }
    
    public String getSpecialValue(Document doc)
    {
        Matcher m = Pattern.compile(PURCHASE_SPECIAL_REGEX).matcher(doc.body().html());
        
        //Grab the regex results
        if (m.find())
        {
            String special = m.group("special");
            return special;
        }
        return "";
    }
    
    //Returns a boolean of the success
    public boolean isSuccess()
    {
        return loginSuccess;
    }
    
    public Document postRequest(String url, Map<String,String> postData)
    {
        try
        {
            return Jsoup.connect(url)
                .cookies(cookies)
                .userAgent(USER_AGENT)
                .data(postData)
                .post();
        }
        catch (IOException ex)
        {
            return null;
        }
    }
    
    //does a GET request on a url
    public Document getRequest(String url) 
    {
        try
        {
        Document request = Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .cookies(cookies)
            .get();
            return request;
        }
        catch (IOException ex)
        {
            return null;
        }
    }
    
    
    
}
