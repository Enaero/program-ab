package org.alicebot.ab;
/* Program AB Reference AIML 2.1 implementation
        Copyright (C) 2013 ALICE A.I. Foundation
        Contact: info@alicebot.org

        This library is free software; you can redistribute it and/or
        modify it under the terms of the GNU Library General Public
        License as published by the Free Software Foundation; either
        version 2 of the License, or (at your option) any later version.

        This library is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
        Library General Public License for more details.

        You should have received a copy of the GNU Library General Public
        License along with this library; if not, write to the
        Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
        Boston, MA  02110-1301, USA.
*/

import java.io.*;
import java.util.HashMap;


public class Main {
    public void init() {
        MagicStrings.setRootPath();

        AIMLProcessor.extension =  new PCAIMLProcessorExtension();

        MagicBooleans.jp_tokenize = false;
        MagicBooleans.trace_mode = true;
        Graphmaster.enableShortCuts = true;

//        if (MagicBooleans.make_verbs_sets_maps) Verbs.makeVerbSetsMaps(bot);
//
//        if (bot.brain.getCategories().size() < MagicNumbers.brain_print_size) bot.brain.printgraph();
    }

    public void testAiml2csv() {
        Bot bot = new Bot("TEST BOT", MagicStrings.root_path, "csv2aiml");
        bot.writeAIMLIFFiles();
    }

    public void testABwq() {
        Bot bot = new Bot("TEST BOT", MagicStrings.root_path, "abwq");
        AB ab = new AB(bot, TestAB.sample_file);
        ab.abwq();
    }

    public void testShadowChecker() {
        Bot bot = new Bot("TEST BOT", MagicStrings.root_path, "shadowchecker");
        MagicBooleans.trace_mode = false;
        bot.shadowChecker();
    }

    public static void getGlossFromInputStream (Bot bot, InputStream in)  {
        System.out.println("getGlossFromInputStream");
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        int cnt = 0;
        int filecnt = 0;
        HashMap<String, String> def = new HashMap<String, String>();
        try {
            //Read File Line By Line
            String word; String gloss;
            word = null;
            gloss = null;
            while ((strLine = br.readLine()) != null)   {

                if (strLine.contains("<entry word")) {
                    int start = strLine.indexOf("<entry word=\"")+"<entry word=\"".length();
                    //int end = strLine.indexOf(" status=");
                    int end = strLine.indexOf("#");

                    word = strLine.substring(start, end);
                    word = word.replaceAll("_"," ");
                    System.out.println(word);

                }
                else  if (strLine.contains("<gloss>")) {
                    gloss = strLine.replaceAll("<gloss>","");
                    gloss = gloss.replaceAll("</gloss>","");
                    gloss = gloss.trim();
                    System.out.println(gloss);

                }


                if (word != null && gloss != null) {
                    word = word.toLowerCase().trim();
                    if (gloss.length() > 2) gloss = gloss.substring(0, 1).toUpperCase()+gloss.substring(1, gloss.length());
                    String definition;
                    if (def.keySet().contains(word))  {
                        definition = def.get(word);
                        definition = definition+"; "+gloss;
                    }
                    else definition = gloss;
                    def.put(word, definition);
                    word = null;
                    gloss = null;
                }
            }
            Category d = new Category(0,"WNDEF *","*","*","unknown","wndefs"+filecnt+".aiml");
            bot.brain.addCategory(d);
            for (String x : def.keySet()) {
                word = x;
                gloss = def.get(word)+".";
                cnt++;
                if (cnt%5000==0) filecnt++;

                Category c = new Category(0,"WNDEF "+word,"*","*",gloss,"wndefs"+filecnt+".aiml");
                System.out.println(cnt+" "+filecnt+" "+c.inputThatTopic()+":"+c.getTemplate()+":"+c.getFilename());
                Nodemapper node;
                if ((node = bot.brain.findNode(c)) != null) node.category.setTemplate(node.category.getTemplate()+","+gloss);
                bot.brain.addCategory(c);


            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
