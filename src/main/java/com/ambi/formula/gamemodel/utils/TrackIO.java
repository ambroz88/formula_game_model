package com.ambi.formula.gamemodel.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ambi.formula.gamemodel.GameModel;
import com.ambi.formula.gamemodel.datamodel.Point;
import com.ambi.formula.gamemodel.datamodel.Polyline;
import com.ambi.formula.gamemodel.datamodel.Track;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Jiri Ambroz
 */
public abstract class TrackIO {

    public static List<String> getAvailableTracks() {
        List<String> tracks = new ArrayList<>();
        File directory = new File("tracks");

        for (File track : directory.listFiles()) {
            tracks.add(track.getName().substring(0, track.getName().lastIndexOf(".")));
        }

        return tracks;
    }

    public static void trackToJSON(Track track, String name) throws IOException {
        JSONObject obj = new JSONObject();

        obj.put("width", track.getModel().getPaperWidth());
        obj.put("height", track.getModel().getPaperHeight());

        // save left barrier
        List<List<Integer>> leftSide = new ArrayList<>();
        Polyline left = track.getLine(Track.LEFT);
        for (int i = 0; i < left.getLength(); i++) {
            leftSide.add(new ArrayList<>(Arrays.asList(left.getPoint(i).getX(), left.getPoint(i).getY())));
        }
        obj.put("left", leftSide);

        // save right barrier
        List<List<Integer>> rightSide = new ArrayList<>();
        Polyline right = track.getLine(Track.RIGHT);
        for (int i = 0; i < right.getLength(); i++) {
            rightSide.add(new ArrayList<>(Arrays.asList(right.getPoint(i).getX(), right.getPoint(i).getY())));
        }
        obj.put("right", rightSide);

        //save to file
        String filePath = getTrackFilePath(name + ".json");
        try {
            try (FileWriter file = new FileWriter(filePath)) {
                file.write(obj.toString(4));
                file.close();
            }
        } catch (IOException e) {
            throw e;
        }
    }

    public static Track trackFromJSON(String name, GameModel model) {
        String filePath = getTrackFilePath(name + ".json");

        try {
            JSONObject jsonObject = new JSONObject(FileIO.readFileToString(filePath));

            // load game properties
            String width = jsonObject.get("width").toString();
            String height = jsonObject.get("height").toString();

            model.setPaperWidth(Integer.valueOf(width));
            model.setPaperHeight(Integer.valueOf(height));

            String X;
            String Y;
            List<Object> coordinatesArray;

            // load left barrier
            JSONArray leftSide = jsonObject.getJSONArray("left");
            Polyline left = new Polyline(Polyline.POLYLINE);

            for (Object pointObject : leftSide.toList()) {
                coordinatesArray = (List) pointObject;
                X = coordinatesArray.get(0).toString();
                Y = coordinatesArray.get(1).toString();
                left.addPoint(new Point(Integer.valueOf(X), Integer.valueOf(Y)));
            }

            // load right barrier
            JSONArray rightSide = jsonObject.getJSONArray("right");
            Polyline right = new Polyline(Polyline.POLYLINE);

            for (Object pointObject : rightSide.toList()) {
                coordinatesArray = (List) pointObject;
                X = coordinatesArray.get(0).toString();
                Y = coordinatesArray.get(1).toString();
                right.addPoint(new Point(Integer.valueOf(X), Integer.valueOf(Y)));
            }

            // load new track
            Track track = new Track(model);
            track.setLeft(left);
            track.setRight(right);
            return track;
        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void deleteTrack(String name) {
        FileUtils.getFile(getTrackFilePath(name + ".json")).delete();
    }

    private static String getTrackFilePath(String name) {
        return "tracks/" + name;
    }

}
