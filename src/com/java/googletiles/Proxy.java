package com.java.googletiles;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import com.java.googletiles.BoundingBox;
@javax.servlet.annotation.WebServlet(name = "Proxy")


public class Proxy extends javax.servlet.http.HttpServlet {
    private static final int BUFFER_SIZE = 4096;
    Properties prop;
    String path, center2, zoom, x, y;
    double latTileHalf, lngTileHalf, center_lat, center_lng;
    static boolean propLoadOnes = true;


    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(propLoadOnes) {
            InputStream inputStream = getServletContext().getResourceAsStream("/WEB-INF/resources/proxy.properties");

            prop = new Properties();
            prop.load(inputStream);
            System.out.println("Loading file path....");

            System.out.println("path: " + prop.getProperty("path"));
            propLoadOnes = false;
            inputStream.close();
            System.out.println("Path loading completed.");

            int days = Integer.parseInt(prop.getProperty("days"));
            int hours = Integer.parseInt(prop.getProperty("hours"));
            path = prop.getProperty("path");
            SchedulerDeleteFile(days,hours,path);
        }
        System.out.println("File path is already loaded ");

//        String center = request.getParameter("center");
//        zoom = String.valueOf(Integer.parseInt(request.getParameter("zoom"))+1);

        System.out.println("zoom : "+zoom);
        x = request.getParameter("x");
        y = request.getParameter("y");

//        String [] coordinations = center.split(",");

//        double latitude = Double.parseDouble(coordinations[0]);
//        double longitude = Double.parseDouble(coordinations[1]);

//        String directoryName = path+zoom+File.separator;
//        String fileName = center+".png";

        BoundingBox bb = tile2boundingBox(Integer.parseInt(zoom),Integer.parseInt(x),Integer.parseInt(y));
        latTileHalf = (bb.north - bb.south)/2;
        lngTileHalf = (bb.east - bb.west)/2;

        center_lat = bb.north - latTileHalf;
        center_lng = bb.east - lngTileHalf;

        center2 = center_lat+","+ center_lng;

//        String directoryName2 = path+getTileId(latitude,longitude,Integer.parseInt(zoom));
//        String fileName2 = getTileFileId(latitude,longitude,Integer.parseInt(zoom));

        String directoryName2 = path+zoom+File.separator+x+File.separator;
        String fileName2 = y+".png";

        System.out.println("Dir name: "+directoryName2);
        System.out.println("File name: "+fileName2);
        System.out.println("Path of the image: "+directoryName2+fileName2);
        File mapTile = fileCreation(directoryName2, fileName2);

        if(mapTile.exists()){
            System.out.println("File Exists");
        }else {
            System.out.println("File needs to Download");
            downloadTile(center2, zoom, mapTile);
        }

        response.setContentType("image/png");
        ServletOutputStream out= response.getOutputStream();
        FileInputStream fin = new FileInputStream(mapTile);
        BufferedInputStream bin = new BufferedInputStream(fin);
        BufferedOutputStream bout = new BufferedOutputStream(out);
        int ch =0;
        while((ch=bin.read())!=-1)
        {
            bout.write(ch);
        }
        bin.close();
        fin.close();
        bout.close();
        out.close();
    }

    public static BoundingBox tile2boundingBox(final int zoom, final int x, final int y) {
        com.java.googletiles.BoundingBox bb = new com.java.googletiles.BoundingBox();
        bb.north = tile2lat(y, zoom);
        bb.south = tile2lat(y + 1, zoom);
        bb.west = tile2lon(x, zoom);
        bb.east = tile2lon(x + 1, zoom);
        return bb;
    }

    public static double tile2lon(int x, int zoom) {
        return x / Math.pow(2.0, zoom) * 360.0 - 180;
    }

    public static double tile2lat(int y, int zoom) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, zoom);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    public static String getTileId(final double lng, final double lat, int zoom) {
        int xtile = (int)Math.floor( (lng + 180) / 360 * (1<<zoom) ) ;
        int ytile = (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom) ) ;
        if (xtile < 0)
            xtile=0;
        if (xtile >= (1<<zoom))
            xtile=((1<<zoom)-1);
        if (ytile < 0)
            ytile=0;
        if (ytile >= (1<<zoom))
            ytile=((1<<zoom)-1);
        return("" + zoom + "/" + xtile + "/");
    }

    public static String getTileFileId(final double lng, final double lat, int zoom) {
        int xtile = (int)Math.floor( (lng + 180) / 360 * (1<<zoom) ) ;
        int ytile = (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom) ) ;
        if (xtile < 0)
            xtile=0;
        if (xtile >= (1<<zoom))
            xtile=((1<<zoom)-1);
        if (ytile < 0)
            ytile=0;
        if (ytile >= (1<<zoom))
            ytile=((1<<zoom)-1);
        return(ytile+".png");
    }

    public void SchedulerDeleteFile(long days, int scheduledHours, String path) {
        System.out.println("Number of Days older : " + days);
        System.out.println("Check Every : "+ scheduledHours + " Hours");
        long scheduledMs = (3600*1000)/(scheduledHours); // coverts hours to ms
//        long scheduledMs = Long.parseLong(scheduledHours);
        Date currentDate = new Date();
        Timer timer = new Timer();
//        timer.scheduleAtFixedRate(new DeleteFilesNdaysOlder(currentDate, (int) days, path),currentDate,scheduledMs);
        timer.scheduleAtFixedRate(new DeleteFilesNdaysOlder2((int) days, path),currentDate,scheduledMs);
    }


    private void downloadTile(String center, String zoom, File fileName) {
        try {
            URL url = new URL("https://maps.googleapis.com/maps/api/staticmap?maptype=satellite&center=" + center + "&zoom=" + zoom + "&size=512x512&key=");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            // always check HTTP response code first
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // opens input stream from the HTTP connection
                InputStream inputStream = conn.getInputStream();
                writeFile(inputStream,fileName);
                System.out.println("File downloaded");
            } else {
                System.out.println("No file to download. Server replied HTTP code: " + responseCode);
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File fileCreation(String directoryName, String fileName) {
        File directory = new File(directoryName);
        if (!directory.exists()){
            directory.mkdirs();
        }
        File imageDest = new File(directory.getAbsoluteFile() + "/" + fileName);
        System.out.println("Image Destination : "+imageDest.getAbsolutePath());
        return imageDest;
    }

    public void writeFile(InputStream inputStream, File fileName){
        try{
            FileOutputStream outputStream = new FileOutputStream(fileName);
            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}