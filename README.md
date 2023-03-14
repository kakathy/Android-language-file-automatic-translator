# Translator
 This project is for independent developers who don't have extra budget for APP localization.
 
 This project should be used to translate android app language files.
 
 After specifying the original language file the Translator will automatically generate language files for all the specified countries.
 
 How to use >>
 
 1.find Android-language-file-automatic-translator/languagetranslator/src/main/java/Translator.java 
 
 2.static final String FILE_ROOT_RES_CONFIG  = new File("").getAbsolutePath() + "/config/";
   this is the specified countries config path
   
   static final String FILE_ROOT_RES  = new File("").getAbsolutePath() + "/res/"; 
   this is original language file path
 

 3.TRANSLATE_BASE_URL = "https://translation.googleapis.com/language/translate/v2?key=";
 key is Google translate API key, Any Google account can apply for a key for free and use it for one year (although restricted but sufficient for personal use)
 API here https://developers.google.com/apis-explorer

 4. Run code and get the language xml in FILE_ROOT_RES
 
 
 
 
 
