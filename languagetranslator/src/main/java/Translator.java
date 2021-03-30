import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translator {

    static final String FILE_ROOT_RES_CONFIG  = new File("").getAbsolutePath() + "/config/";
    static final String FILE_ROOT_RES  = new File("").getAbsolutePath() + "/res/";
    public static final String TRANSLATE_BASE_URL = "https://translation.googleapis.com/language/translate/v2?key=";
    public static void main(String args[]) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doString();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
    public static void doString() throws JSONException, IOException {

        List<String> sourceStrings = getLines(FILE_ROOT_RES + "strings.xml");
        List<String> countryString = getLines(FILE_ROOT_RES_CONFIG + "country.txt");
        for (String county : countryString) {


            if (new File(FILE_ROOT_RES + county.split(":")[1] + "/" +
                    "strings.xml").exists()) {
                continue;

            }
            System.out.print(county.split(":")[1] + " do stringing\n");


            final List<String> sourceStringRelults = new ArrayList<>();
            //解析string类型
            for (String str : sourceStrings) {
                if (isCanTs(str)) {
                    if (str.contains("<string")) {
                        JSONObject sb = new JSONObject();
                        String peiStart = "<string\\s*name=\\\"[\\s\\S]*\\\">";
                        peiStart = findString(peiStart, str);
                        String peiMid = "(?<=\\\">)([\\S\\s]*?)(?=</)";
                        peiMid = findString(peiMid, str);
                        final String end = "</string>";
                        if (peiMid == null || peiMid.length() <= 0) {
                            sourceStringRelults.add(str);
                            continue;
                        }
                        final String finalPeiStart = peiStart;
                        addString(sb, "q", peiMid);
                        String target;
                        if ("rTW".equals(county.split(":")[1].split("-")[2])) {
                            target = "zh-TW";
                        } else if ("rHK".equals(county.split(":")[1].split("-")[2])) {
                            target = "zh-TW";
                        } else {
                            target = county.split(":")[1].split("-")[1];
                        }
                        addString(sb, "target", target);
                        URL mRrl = new URL(TRANSLATE_BASE_URL);
                        HttpURLConnection httpURLConnection = (HttpURLConnection) mRrl.openConnection();
                        httpURLConnection.setConnectTimeout(6 * 10000);
                        httpURLConnection.setReadTimeout(6 * 10000);
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setUseCaches(false);
                        httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                        httpURLConnection.setRequestProperty("Charset", "UTF-8");
                        httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        httpURLConnection.setRequestProperty("accept", "application/json");
                        if (sb != null && !sb.toString().isEmpty()) {
                            byte[] writebytes = sb.toString().getBytes();
                            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                            OutputStream outwritestream = httpURLConnection.getOutputStream();
                            outwritestream.write(sb.toString().getBytes());
                            outwritestream.flush();
                            outwritestream.close();
                        }
                        BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"), 8 * 1024);
                        StringBuilder resultBuffer = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            resultBuffer.append(line);
                        }
                        br.close();
                        System.out.print(resultBuffer.toString());
                        JSONArray jsonArray = new JSONObject(resultBuffer.toString()).optJSONObject("data").optJSONArray("translations");
                        sourceStringRelults.add(finalPeiStart + (new JSONObject(jsonArray.get(0).toString()).get("translatedText")).toString().replace("'", "\'") + end);
                    }
                } else {
                    sourceStringRelults.add(str);
                }

            }
            System.out.print(county.split(":")[1] + " save string\n");
            saveStringsToFile(FILE_ROOT_RES + county.split(":")[1] + "/" + "strings.xml", sourceStringRelults);
            System.out.print("end save string\n");
            sourceStringRelults.clear();
        }
    }

    private static void addString(JSONObject jsonObject, String key, String value) {
        try {
            jsonObject.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String findString(String ppp, String body) {
        Pattern p = Pattern
                .compile(ppp);
        Matcher m = p.matcher(body);
        while (m.find()) {
            String json = m.group();
            if (json != null && json.length() > 0) {
                return json;
            }
        }
        return null;
    }

    public static List<String> getLines(String path) {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        List<String> lines = new ArrayList<String>();
        try {
            String str = "";
            fis = new FileInputStream(path);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);//
            while ((str = br.readLine()) != null) {
                lines.add(str);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Read failed");
        } finally {
            try {
                br.close();
                isr.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return lines;
    }

    public static boolean isCanTs(String str) {
        if (str.contains("resources>") || str.contains("<?xml") || str.contains("string-array")) {
            return false;
        }
        return true;
    }

    public static void saveStringsToFile(final String strFilename, List<String> sourceStringRelults) {

        String strBuffer = "";
        for (String str : sourceStringRelults) {
            strBuffer += str + "\n";
        }

        TextToFile(strFilename, strBuffer);

    }

    public static void TextToFile(final String strFilename, final String strBuffer) {
        try {
            File fileText = new File(strFilename);
            if (!fileText.getParentFile().exists()) {
                fileText.getParentFile().mkdirs();
            }

            if (fileText.exists())
                fileText.delete();
            if (!fileText.exists()) {
                fileText.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(fileText);
            fileWriter.write(strBuffer);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
