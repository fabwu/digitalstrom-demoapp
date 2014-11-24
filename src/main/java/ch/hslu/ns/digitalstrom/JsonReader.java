package ch.hslu.ns.digitalstrom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Set;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonReader {

    /**
     * default IP Adress of digitalSTROM Testinstallation or developer kit.Port
     * 8080 or xxxxx when portforwarding.
     */
    private String testServer = "https://testrack2.aizo.com:58080";
    /**
     * username for the digitalSTROM Server.
     */
    private String user = "dssadmin";
    /**
     * password for the digitalSTROM Server.
     */
    private String password = "dssadmin";

    private static HashMap<String, String> eventlist, deviceMap;

    private static String applicationToken;
    private static String temporaryToken;

    public HashMap<String, String> getDeviceMap() {
        return deviceMap;
    }

    public String setLoginData(String testServer, String user, String pw) {
        this.testServer = testServer;
        this.user = user;
        this.password = pw;
        System.out.println(testServer);
        return "\nConnect to " + testServer + "\nWith user " + user + " and password " + pw;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        }
    }

    public void doTrustToCertificates() throws Exception {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                    return;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                    return;
                }
            }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                    System.out.println("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
                }
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }

    /**
     * connecting to URL with trusting all certificates.
     *
     * @throws java.net.MalformedURLException
     */
    public void connectToUrl() throws MalformedURLException, Exception {
        doTrustToCertificates();//  
        URL url = new URL(testServer);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        System.out.println("Connection no Trust" + conn.getResponseCode());
    }

    public String initDigitialServerLogin() throws IOException {
        /**
         * the application token is a string that must be persisted in the
         * application. Connecting for the first time, a new application token
         * is requested. This function won't work if requested from a logged in
         * session.
         */
        JSONObject json = readJsonFromUrl(testServer + "/json/system/requestApplicationToken?applicationName=hsluApp");
        applicationToken = (String) json.getJSONObject("result").get("applicationToken");
        String apptoken = "applicationToken=" + applicationToken;

        /**
         * Get temporary token for authentification.
         */
        json = readJsonFromUrl(testServer + "/json/system/login?user=" + user + "&password=" + password);
        temporaryToken = (String) json.getJSONObject("result").get("token");
        String tempToken = "temporaryToken=" + temporaryToken;

        /**
         * The token needs to be enabled before it is active.
         */
        json = readJsonFromUrl(testServer + "/json/system/enableToken?applicationToken="
                + applicationToken + "&token=" + temporaryToken);
        String enableToken = "Enable token..." + json.toString();

        return "\n" + apptoken + "\n" + tempToken + "\n" + enableToken + "\n";

    }

    /**
     * Commando.
     *
     * @param dsid
     * @param commando for example blink
     * @return
     * @throws IOException
     */
    public String digitalServerDeviceCommando(String dsid, String commando) throws IOException {
        JSONObject json = readJsonFromUrl(testServer + "/json/device/" + commando + "?dsid=" + dsid
                + "&applicationToken=" + applicationToken + "&token=" + temporaryToken);
        return json.toString();
    }

    public String digitalServerDeviceCommando(String dsid, String commando, int offset) throws IOException {
        JSONObject json = readJsonFromUrl(testServer + "/json/device/" + commando + "?dsid=" + dsid
                + "&offset=" + offset + "&applicationToken=" + applicationToken + "&token=" + temporaryToken);

        return json.toString();
    }

    public HashMap<String, String> digitalServerDeviceMap() throws IOException {

        deviceMap = new HashMap();

        try {
            JSONObject json = readJsonFromUrl(testServer + "/json/property/query?query=/apartment/zones/zone0/devices/*(name,dSID)"
                    + "&applicationToken=" + applicationToken + "&token=" + temporaryToken);
            System.out.print(json.toString());
            JSONArray array = json.getJSONObject("result").getJSONArray("devices");
            for (int i = 0; i < array.length(); i++) {
                deviceMap.put((String) array.getJSONObject(i).get("name"), (String) array.getJSONObject(i).get("dSID"));
            }
        } catch (IOException | JSONException ex) {
            System.err.println(ex.getMessage());
            return null;
        }
        return deviceMap;
    }

    public HashMap<String, String> getEvents() {
        eventlist = new HashMap<>();
        try {
            JSONObject json = readJsonFromUrl(testServer + "/json/property/query?query=/usr/events/*(name,id)"
                    + "&applicationToken=" + applicationToken + "&token=" + temporaryToken);
            JSONArray array = json.getJSONObject("result").getJSONArray("events");
            for (int i = 0; i < array.length(); i++) {
                eventlist.put((String) array.getJSONObject(i).get("name"), (String) array.getJSONObject(i).get("id"));
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            return null;
        } catch (JSONException ex) {
            System.err.println(ex.getMessage());
            return null;
        }
        System.out.println(eventlist.size());

        return eventlist;
    }

    public String[] getEventKeysHashmap() {
        eventlist = getEvents();
        Set<String> set = eventlist.keySet();
        String[] array = new String[set.size()];
        int i = 0;
        for (String s : set) {
            array[i] = s;
            i++;
        }
        return array;
    }

    public String[] getDevicesKeysHashmap() {
        try {
            eventlist = digitalServerDeviceMap();
            Set<String> set = eventlist.keySet();
            String[] array = new String[set.size()];
            int i = 0;
            for (String s : set) {
                array[i] = s;
                i++;
            }
            return array;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

}
