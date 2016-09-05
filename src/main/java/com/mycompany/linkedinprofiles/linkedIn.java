/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.linkedinprofiles;

/**
 *
 * @author tarik
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class linkedIn {

    static BufferedWriter writer;
    static int found = 0;

    static String userAgent[];
    static int numberOfAgents = 1;
    static int agentCounter = 0;
    static int agentCounter2 = 0;
    static int startingRow = 0;

    public static void main(String[] args) throws Exception {

        userAgent = new String[3];
        
        //     userAgent[0] = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36";
        userAgent[0] = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0";
        //    userAgent[2] = "Mozilla/5.0 (X11; Linux i686; rv:10.0.5) Gecko/20100101 Firefox/10.0.5 Iceweasel/10.0.5";

        System.out.println("Please enter the starting row and the program will read next 50 rows: ");
        startingRow = (new Scanner(System.in)).nextInt();
        // D:/FinancialFraudResearch/nameList.csv
        FileReader fileReader = new FileReader("nameList.csv");
        BufferedReader reader = new BufferedReader(fileReader);
        //D:/FinancialFraudResearch/firstVerion.csv
        FileWriter filewriter = new FileWriter("linkedInProfilesOutput.csv");
        writer = new BufferedWriter(filewriter);

        writer.write("counter, manually confirmed?, company, last name, initial letter, search term, potential url, potential name, company worked for at 2006, job duration, job title, job description ");
        writer.newLine();
        
        int c2 = 1;
        String line = reader.readLine();
        
        for (int c = 0; c < 50 + startingRow; c++) {
            
            line = reader.readLine();
            
            if( c2++ < startingRow)
                continue;
            
            String parameters[] = line.split(",");
            Thread.sleep(700);
            boolean found = false;
            
            if(parameters.length > 3)
            {
                parameters[1] = parameters[1] + parameters[2];
                parameters[2] = parameters[3];
            }
            
            if(parameters.length < 3 || parameters[2].length() > 1)
            {
                String tempParameters[] = new String[3];
                tempParameters[0] = parameters[0];
                tempParameters[1] = parameters[1];
                tempParameters[2] = "?";
                parameters = tempParameters;
            }
            
            System.out.print("Current search: ");
            System.out.println("company: " + parameters[0] + " lastname: " + parameters[1] + " initial: " + parameters[2].charAt(0));
            found = writeToFile(parameters[0], parameters[1], parameters[2].charAt(0), c, 1);
            
            if(!found)
            {
                writeToFile(parameters[0], parameters[1], parameters[2].charAt(0), c, 2);
            }
            
            System.out.println();
            System.out.println("***");
            System.out.println();
        }

    }

    public static boolean writeToFile(String bank, String lName, char initialChar, int count, int mode) throws Exception {

        String names[] = null;
        String lastName = lName;
        String company = bank;
        char initial = initialChar;
        String title;
        String searchTerm = company + " " + lastName + " " + initial;
        
        if(mode == 2)
            searchTerm = lastName + " " + initial;
        
        String url = "http://www.google.com/search?q=" + searchTerm;

        String fullName = "";
        String targetUrl = "not found";
        System.out.println("Searching google for the following term: " + searchTerm);
        Document doc = Jsoup.connect(url).userAgent(userAgent[(agentCounter++) % numberOfAgents]).get();

        boolean targetFound = false;

        // get the page title
        title = doc.title();
  //      System.out.println("google search page title: " + title);

        boolean initialMatches = false;

        // get all links in page
        Elements links = doc.select("a[href]");
        for (Element link : links) {

            if (targetFound) {
                break;
            }

            targetUrl = link.attr("href");
    //        System.out.println("analysing this url: " + targetUrl);

            if (containsIgnoreCase(targetUrl, "linkedin.com") && containsIgnoreCase(targetUrl, lastName) && link.text().contains("|")) {

                System.out.println("Suspected url: " + targetUrl);

                names = link.text().substring(0, link.text().indexOf('|')).split(" ");

                if (("" + names[0].charAt(0)).equalsIgnoreCase(initial + "")) //|| ("" + names[1].charAt(0)).equalsIgnoreCase(initial + "")) 
                {
                    initialMatches = true;
                    targetFound = true;

                    for (int c2 = 0; c2 < names.length; c2++) {
                        String str = names[c2];
                        fullName += str + " ";
                    }
                }

            }
        }

        if (!targetFound) {
            fullName = "not found";
        }

        if (targetFound == true) {
            System.out.print("Name:");
            System.out.println(fullName);
            System.out.println("Url: " + targetUrl);
            fullName = fullName.replaceAll(",", "");
            targetFound = true;

//            background.
        } else {
            System.out.println("not found");
            fullName = "not found";
            targetUrl = "not found";
            targetFound = false;
        }

        //     writer.write("counter, manually confirmed?, company, last name, initial letter, search term, potential url, potential name, company worked for at 2006, job duration, job title, job description ");
        if (targetFound) {
            String linkedInString = getLinkedInInformation(targetUrl);
            writer.write(count + ", " + "no, " + company + ", " + lastName + ", " + initial + ", " + searchTerm + ", " + targetUrl + ", " + fullName + ", " + linkedInString);
            found++;
        }
        writer.newLine();
        writer.flush();
        System.out.println("Written: " + found + " " + "/" + (count + 1 - startingRow) );
        return targetFound;
    }

    public static String getLinkedInInformation(String uri) throws Exception {

        String url = uri;
        //   String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.9.2.3) Gecko/20100401 Firefox/3.6.3";
        //  String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36";
        //  String userAgent = "Mozilla";
        Connection.Response response = Jsoup.connect(url)
                .ignoreContentType(true)
                .userAgent(userAgent[(agentCounter2++) % numberOfAgents])
                .referrer("https://www.google.com")
                .timeout(12000)
                .followRedirects(true)
                .execute();
        //     System.out.println(response.statusMessage()); 
        Document linkedInpage = response.parse();
        System.out.println(" " + linkedInpage.title());
        Elements positions = linkedInpage.getElementsByClass("position");
        Element targetPosition = null;
        for (Element position : positions) {
            Element workingDate = position.getElementsByClass("date-range").first();
            if(workingDate == null)
                continue;
            List<Node> times = workingDate.childNodes();
            if(times == null)
                continue;
            // System.out.println("size: " + times.size());
            Elements dates = workingDate.getElementsByTag("time");
            if(dates == null || dates.size() < 2)
                continue;
            String beginning = dates.get(0).ownText().replaceAll("[^\\d.]", "");
            String ending = "2016";
            if (dates.size() > 3) {
                ending = dates.get(2).ownText().replaceAll("[^\\d.]", "");
            }
            //  System.out.println("Begin: " + beginning + " Ending " + ending);
            int startingYear = Integer.parseInt(beginning);
            int endingYear = Integer.parseInt(ending);

            if (startingYear <= 2006 && endingYear >= 2006) {
                targetPosition = position;
                break;
            }
        }

        //     writer.write("counter, manually confirmed?, company, last name, initial letter, search term, potential url, potential name, company worked for at 2006, job duration, job title, job description ");
        String result = "";
        if (targetPosition != null) {

            Element itemSubtitle = targetPosition.getElementsByClass("item-subtitle").first();
            if (itemSubtitle != null) {

                System.out.println(itemSubtitle.text());
                result += itemSubtitle.text().replaceAll(",", "") + ", ";

            }

            Element dateRange = targetPosition.getElementsByClass("date-range").first();
            if (dateRange != null) {
                System.out.println(dateRange.text());
                result += dateRange.text().replaceAll(",", "") + ", ";
            }

            Element itemTitle = targetPosition.getElementsByClass("item-title").first();
            if (itemTitle != null) {
                System.out.println(itemTitle.text());
                result += itemTitle.text().replaceAll(",", "") + ", ";
            }

            Element description = targetPosition.getElementsByClass("description").first();
            if (description != null) {
                System.out.println(description.text());
                result += description.text().replaceAll(",", "") + ", ";
            }

        }
        return result;
    }

}
