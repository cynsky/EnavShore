/*
 * Copyright 2011 Danish Maritime Safety Administration. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY Danish Maritime Safety Administration ``AS IS'' 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of Danish Maritime Safety Administration.
 * 
 */
package dk.frv.enav.shore.core.services.nogo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import dk.frv.ais.geo.GeoLocation;
import dk.frv.enav.common.xml.nogo.request.NogoRequest;
import dk.frv.enav.common.xml.nogo.response.NogoResponse;
import dk.frv.enav.common.xml.nogo.types.BoundingBoxPoint;
import dk.frv.enav.common.xml.nogo.types.NogoPoint;
import dk.frv.enav.common.xml.nogo.types.NogoPolygon;
import dk.frv.enav.shore.core.domain.DepthDenmark;
import dk.frv.enav.shore.core.domain.TideDenmark;
import dk.frv.enav.shore.core.services.Errorcodes;
import dk.frv.enav.shore.core.services.ServiceException;

@Stateless
public class NogoServiceBean implements NogoService {

    @PersistenceContext(unitName = "enav")
    private EntityManager entityManager;

    public enum WorkerType {
        DEPTHPOINT, TIDEPOINT, DEPTHDATA, TIDEDATA, MAXTIDE;
    }

    public enum DataType {
        SYDKATTEGAT, NORDKATTEGAT, SF_BAY, HUMBER;
    }

    int errorCode = 0;
    DataType type;

    double latOffset;
    double lonOffset;

    @SuppressWarnings("deprecation")
    @Override
    public NogoResponse nogoPoll(NogoRequest nogoRequest) throws ServiceException {

        // System.out.println("NoGo request recieved");

        // First identify which area we are searching in

        GeoLocation northWest = new GeoLocation(nogoRequest.getNorthWestPointLat(), nogoRequest.getNorthWestPointLon());
        GeoLocation SouthEast = new GeoLocation(nogoRequest.getSouthEastPointLat(), nogoRequest.getSouthEastPointLon());

        // System.out.println("northWest " + northWest);
        // System.out.println("southEAst " + SouthEast);

        NogoWorker nogoWorkerFirstPointDepth = null;

        NogoWorker nogoWorkerSecondPointDepth = null;

        NogoWorker nogoWorkerFirstPointTide = null;

        NogoWorker nogoWorkerSecondPointTide = null;

        NogoWorker nogoWorkerDepthData = null;

        NogoWorker nogoWorkerTideData = null;

        // Sydkattegat data
        if (northWest.getLatitude() > 54.36294 && northWest.getLatitude() < 56.36316 && northWest.getLongitude() > 9.419409
                && northWest.getLongitude() < 13.149009 && SouthEast.getLatitude() > 54.36294 && SouthEast.getLatitude() < 56.36316
                && SouthEast.getLongitude() > 9.419409 && SouthEast.getLongitude() < 13.149009) {

            nogoWorkerFirstPointDepth = new NogoWorker(entityManager, WorkerType.DEPTHPOINT, DataType.SYDKATTEGAT);
            nogoWorkerSecondPointDepth = new NogoWorker(entityManager, WorkerType.DEPTHPOINT, DataType.SYDKATTEGAT);
            nogoWorkerFirstPointTide = new NogoWorker(entityManager, WorkerType.TIDEPOINT, DataType.SYDKATTEGAT);
            nogoWorkerSecondPointTide = new NogoWorker(entityManager, WorkerType.TIDEPOINT, DataType.SYDKATTEGAT);

            nogoWorkerDepthData = new NogoWorker(entityManager, WorkerType.DEPTHDATA, DataType.SYDKATTEGAT);
            nogoWorkerTideData = new NogoWorker(entityManager, WorkerType.TIDEDATA, DataType.SYDKATTEGAT);

            latOffset = 0.00055500;
            lonOffset = 0.00055504;

            type = DataType.SYDKATTEGAT;

        } else {
            // Nordkattegat data
            if (northWest.getLatitude() > 56.34096 && northWest.getLatitude() < 58.26237 && northWest.getLongitude() > 9.403869
                    && northWest.getLongitude() < 12.148899 && SouthEast.getLatitude() > 56.34096
                    && SouthEast.getLatitude() < 58.26237 && SouthEast.getLongitude() > 9.403869
                    && SouthEast.getLongitude() < 12.148899) {
                // System.out.println("Valid nordkattegat point");

                nogoWorkerFirstPointDepth = new NogoWorker(entityManager, WorkerType.DEPTHPOINT, DataType.NORDKATTEGAT);
                nogoWorkerSecondPointDepth = new NogoWorker(entityManager, WorkerType.DEPTHPOINT, DataType.NORDKATTEGAT);
                nogoWorkerFirstPointTide = new NogoWorker(entityManager, WorkerType.TIDEPOINT, DataType.NORDKATTEGAT);
                nogoWorkerSecondPointTide = new NogoWorker(entityManager, WorkerType.TIDEPOINT, DataType.NORDKATTEGAT);

                nogoWorkerDepthData = new NogoWorker(entityManager, WorkerType.DEPTHDATA, DataType.NORDKATTEGAT);
                nogoWorkerTideData = new NogoWorker(entityManager, WorkerType.TIDEDATA, DataType.NORDKATTEGAT);

            }

            latOffset = 0.00055504;
            lonOffset = 0.00055504;

            type = DataType.NORDKATTEGAT;

        }

        // SF Bay data
        if (northWest.getLatitude() > 37.17 && northWest.getLatitude() < 38.35 && northWest.getLongitude() > -123.21
                && northWest.getLongitude() < -121.32 && SouthEast.getLatitude() > 37.17 && SouthEast.getLatitude() < 38.35
                && SouthEast.getLongitude() > -123.21 && SouthEast.getLongitude() < -121.32) {
            // System.out.println("Valid nordkattegat point");

            nogoWorkerFirstPointDepth = new NogoWorker(entityManager, WorkerType.DEPTHPOINT, DataType.SF_BAY);
            nogoWorkerSecondPointDepth = new NogoWorker(entityManager, WorkerType.DEPTHPOINT, DataType.SF_BAY);
            nogoWorkerFirstPointTide = new NogoWorker(entityManager, WorkerType.TIDEPOINT, DataType.SF_BAY);
            nogoWorkerSecondPointTide = new NogoWorker(entityManager, WorkerType.TIDEPOINT, DataType.SF_BAY);

            nogoWorkerDepthData = new NogoWorker(entityManager, WorkerType.DEPTHDATA, DataType.SF_BAY);
            nogoWorkerTideData = new NogoWorker(entityManager, WorkerType.TIDEDATA, DataType.SF_BAY);

            latOffset = -0.00008;
            lonOffset = 0.000151883;

            type = DataType.SF_BAY;
        }

        // Humber Data
        if (northWest.getLatitude() > 53.53 && northWest.getLatitude() < 53.742 && northWest.getLongitude() > -0.87
                && northWest.getLongitude() < 0.25 && SouthEast.getLatitude() > 53.53 && SouthEast.getLatitude() < 53.742
                && SouthEast.getLongitude() > -0.87 && SouthEast.getLongitude() < 0.25) {
            System.out.println("Valid Humber point");

            nogoWorkerFirstPointDepth = new NogoWorker(entityManager, WorkerType.DEPTHPOINT, DataType.HUMBER);
            nogoWorkerSecondPointDepth = new NogoWorker(entityManager, WorkerType.DEPTHPOINT, DataType.HUMBER);
            nogoWorkerFirstPointTide = new NogoWorker(entityManager, WorkerType.TIDEPOINT, DataType.HUMBER);
            nogoWorkerSecondPointTide = new NogoWorker(entityManager, WorkerType.TIDEPOINT, DataType.HUMBER);

            nogoWorkerDepthData = new NogoWorker(entityManager, WorkerType.DEPTHDATA, DataType.HUMBER);
            nogoWorkerTideData = new NogoWorker(entityManager, WorkerType.TIDEDATA, DataType.HUMBER);

            // latOffset = 0.0000418;

            // latOffset = 0.0000868125;
            // lonOffset = 0.000151883;
            latOffset = 0.0000434;
            lonOffset = 0;

            type = DataType.HUMBER;
        }

        // Is the points outside our area?

        if ((northWest.getLatitude() > 58.26237 || northWest.getLatitude() < 54.36294 || northWest.getLongitude() > 13.149009
                || northWest.getLongitude() < 9.403869

                || SouthEast.getLatitude() > 58.26237 || SouthEast.getLatitude() < 54.36294 || SouthEast.getLongitude() > 13.149009 || SouthEast
                .getLongitude() < 9.403869)

                && (northWest.getLatitude() > 38.35 || northWest.getLatitude() < 37.16 || northWest.getLongitude() > -121.32
                        || northWest.getLongitude() < -123.21

                        || SouthEast.getLatitude() > 38.35 || SouthEast.getLatitude() < 37.16 || SouthEast.getLongitude() > -121.32 || SouthEast
                        .getLongitude() < -123.21)

                && (northWest.getLatitude() < 53.53 || northWest.getLatitude() > 53.742 || northWest.getLongitude() < -0.87
                        || northWest.getLongitude() > 0.25 || SouthEast.getLatitude() < 53.53 || SouthEast.getLatitude() > 53.742
                        || SouthEast.getLongitude() < -0.87 || SouthEast.getLongitude() > 0.25)

        ) {
            System.out.println("No data available");

            NogoResponse res = new NogoResponse();

            res.setNoGoErrorCode(17);
            res.setNoGoMessage(Errorcodes.getErrorMessage(17));

            // System.out.println("Returning empty res");

            return res;
        }

        // NogoWorker nogoWorkerThirdMaxTide = new NogoWorker(entityManager,
        // WorkerType.MAXTIDE);

        nogoWorkerFirstPointDepth.setPos(new GeoLocation(nogoRequest.getNorthWestPointLat(), nogoRequest.getNorthWestPointLon()));

        nogoWorkerSecondPointDepth.setPos(new GeoLocation(nogoRequest.getSouthEastPointLat(), nogoRequest.getSouthEastPointLon()));

        nogoWorkerFirstPointTide.setPos(new GeoLocation(nogoRequest.getNorthWestPointLat(), nogoRequest.getNorthWestPointLon()));

        nogoWorkerSecondPointTide.setPos(new GeoLocation(nogoRequest.getSouthEastPointLat(), nogoRequest.getSouthEastPointLon()));

        // firstPos = getArea(55.070, 11.668);
        // secondPos = getArea(55.170, 11.868)

        // Testing stuff
        // nogoWorkerFirstPointDepth.setPos(new GeoLocation(55.070, 11.668));
        //
        // nogoWorkerSecondPointDepth.setPos(new GeoLocation(55.170, 11.868));
        //
        // nogoWorkerFirstPointTide.setPos(new GeoLocation(55.070, 11.668));
        //
        // nogoWorkerSecondPointTide.setPos(new GeoLocation(55.170, 11.868));

        // Get the grid position of the data in the depth database
        nogoWorkerFirstPointDepth.start();
        nogoWorkerSecondPointDepth.start();

        // Get the grid position of the data in the tide database
        nogoWorkerFirstPointTide.start();
        nogoWorkerSecondPointTide.start();

        // Find max change in depth database - not needed anymore
        // nogoWorkerThirdMaxTide.start();

        // nogoRequest.getStartDate();

        try {
            nogoWorkerFirstPointDepth.join();
            System.out.println("First depth point found");
            nogoWorkerSecondPointDepth.join();
            System.out.println("Second depth point found");
            // nogoWorkerThirdMaxTide.join();
            // System.out.println("MaxTide found");

            nogoWorkerFirstPointTide.join();
            // System.out.println("First tide point found");
            nogoWorkerSecondPointTide.join();
            // System.out.println("Second tide point found");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        BoundingBoxPoint firstPosDepth = nogoWorkerFirstPointDepth.getPoint();
        BoundingBoxPoint secondPosDepth = nogoWorkerSecondPointDepth.getPoint();

        // System.out.println("depth points are " +
        // nogoWorkerFirstPointDepth.getPoint() + ", "
        // + nogoWorkerSecondPointDepth.getPoint());

        BoundingBoxPoint firstPosTide = nogoWorkerFirstPointTide.getPoint();
        BoundingBoxPoint secondPosTide = nogoWorkerSecondPointTide.getPoint();

        // System.out.println("tide points are " +
        // nogoWorkerFirstPointTide.getPoint() + ", "
        // + nogoWorkerSecondPointTide.getPoint());

        List<NogoPolygon> polyArea = new ArrayList<NogoPolygon>();

        if (firstPosDepth != null && secondPosDepth != null) {
            // System.out.println("Bounding Box found - requesting data");

            nogoWorkerDepthData.setFirstPos(firstPosDepth);
            nogoWorkerDepthData.setSecondPos(secondPosDepth);

            nogoWorkerDepthData.setDraught(nogoRequest.getDraught());

            // Testing
            // nogoWorkerDepthData.setDraught(-7);

            nogoWorkerTideData.setFirstPos(firstPosTide);
            nogoWorkerTideData.setSecondPos(secondPosTide);

            // Use 01-05 until we get better database setup
            // 2012-01-05 22:00:00
            java.sql.Timestamp timeStart = new Timestamp(112, 0, 5, 0, 0, 0, 0);
            java.sql.Timestamp timeEnd = new Timestamp(112, 0, 5, 0, 0, 0, 0);

            timeStart.setHours(nogoRequest.getStartDate().getHours());
            timeEnd.setHours(nogoRequest.getEndDate().getHours());

            nogoWorkerTideData.setTimeStart(timeStart);
            nogoWorkerTideData.setTimeEnd(timeEnd);

            // System.out.println("StartTime is: " + timeStart);
            //
            // System.out.println("EndTime is: " + timeEnd);

            nogoWorkerDepthData.start();

            nogoWorkerTideData.start();

            try {
                nogoWorkerDepthData.join();
                System.out.println("Depth data thread joined");
                nogoWorkerTideData.join();
                System.out.println("Tide data thread joined");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Depth database size: " + nogoWorkerDepthData.getDepthDatabaseResult().size());

            if (nogoWorkerDepthData.getDepthDatabaseResult().size() != 0) {

                System.out.println("Begin parsing");
                polyArea = parseResult(nogoWorkerDepthData.getDepthDatabaseResult(), nogoWorkerTideData.getTideDatabaseResult(),
                        nogoRequest.getDraught());
            }
            // polyArea = getNogoArea(firstPos, secondPos, -7);
            System.out.println("Data recieved and parsed");
        }

        NogoResponse res = new NogoResponse();

        for (int i = 0; i < polyArea.size(); i++) {
            res.addPolygon(polyArea.get(i));
        }

        Date requestStart = nogoRequest.getStartDate();
        requestStart.setMinutes(0);
        requestStart.setSeconds(0);

        Date requestEnd = nogoRequest.getEndDate();
        requestEnd.setMinutes(0);
        requestEnd.setSeconds(0);

        // Date currentDate = new Date();
        // long futureDate = currentDate.getTime() + 7200000;

        res.setValidFrom(requestStart);
        res.setValidTo(requestEnd);

        res.setNoGoErrorCode(errorCode);
        res.setNoGoMessage(Errorcodes.getErrorMessage(errorCode));

        errorCode = 0;

        System.out.println("Sending data");
        return res;
    }

    @SuppressWarnings("unused")
    @Override
    public List<NogoPolygon> parseResult(List<DepthDenmark> result, List<TideDenmark> resultTide, double depth) {

        // System.out.println("Query executed! - parsing");

        // This is where we store our result
        List<NogoPolygon> res = new ArrayList<NogoPolygon>();

        // Seperate it into lines - depth
        List<List<DepthDenmark>> lines = new ArrayList<List<DepthDenmark>>();
        int m = -1;
        List<DepthDenmark> line = null;
        for (DepthDenmark depthDenmark : result) {
            // What is the index, n
            if (depthDenmark.getM() > m) {
                line = new ArrayList<DepthDenmark>();
                lines.add(line);
                m = depthDenmark.getM();
            }
            line.add(depthDenmark);
        }

        if (resultTide == null) {
            errorCode = 18;
        }

        // Seperate it into lines - tide - if we got em
        if (resultTide != null) {

            // Disable tide for now
            if (true) {
                // if (resultTide.size() == 0){
                // System.out.println("No tide");
                // errorCode = 18;
            } else {

                List<List<TideDenmark>> linesTide = new ArrayList<List<TideDenmark>>();
                int mT = -1;
                List<TideDenmark> lineTide = null;
                for (TideDenmark tideDenmark : resultTide) {
                    // What is the index, n
                    if (tideDenmark.getM() > mT) {
                        lineTide = new ArrayList<TideDenmark>();
                        linesTide.add(lineTide);
                        mT = tideDenmark.getM();
                    }
                    lineTide.add(tideDenmark);

                }

                // Identify how many similar we have
                int n = linesTide.get(0).get(0).getN();
                int nCount = 0;
                for (int j = 0; j < linesTide.get(0).size(); j++) {

                    if (n != -1 && linesTide.get(0).get(j).getN() != n) {
                        break;
                    }
                    nCount++;

                }

                // System.out.println("We have: " + nCount + " that are equal");
                // System.out.println("The size of linesTide first line is: " +
                // linesTide.get(0).size());
                // We have a broad time spand
                if (nCount != 1) {
                    List<List<TideDenmark>> linesTideParsed = new ArrayList<List<TideDenmark>>();
                    // We need to take nCount out and compare, and return the
                    // highest
                    for (int i = 0; i < linesTide.size(); i++) {
                        List<TideDenmark> parsedLine = compareTideLines(linesTide.get(i), nCount);
                        linesTideParsed.add(parsedLine);
                    }
                    // Overwrite the old one
                    linesTide = linesTideParsed;
                }

                // System.out.println("The size of linesTideParsed first line is: "
                // + linesTide.get(0).size());

                // Combine the two into one result
                int j = 0;
                for (int i = 0; i < linesTide.size(); i++) {
                    List<TideDenmark> currentTideLine = linesTide.get(i);
                    combineVertical(currentTideLine, lines, j);
                    j = j + 5;
                }

            }
        }

        List<List<DepthDenmark>> parsedLines = new ArrayList<List<DepthDenmark>>();

        // Remove invalid positions
        for (int i = 0; i < lines.size(); i++) {
            parsedLines.add(new ArrayList<DepthDenmark>());
            for (int k = 0; k < lines.get(i).size(); k++) {
                if (lines.get(i).get(k).getDepth() == null || lines.get(i).get(k).getDepth() > depth) {
                    // System.out.println("Current line depth is: " + lines.get(i).get(k).getDepth());
          
                    parsedLines.get(i).add(lines.get(i).get(k));
                }

            }

        }

        lines = parsedLines;

        // System.out.println("Parsing Query");

        ParseData parseData = new ParseData();

        // System.out.println("Lines is: " + lines.size());

        List<List<DepthDenmark>> parsed = parseData.getParsed(lines);

        // System.out.println("Parsed is: " + parsed.size());

        // parsed = lines;

        // System.out.println(lines.size());
        // System.out.println(parsed.size());
        //
        // for (int j = 0; j < parsed.size(); j++) {
        // System.out.println(parsed.get(j).size());
        // }

        // All the line component are split into sections, ie. all on same index m are put in a list together
        List<List<List<DepthDenmark>>> lineSection = new ArrayList<List<List<DepthDenmark>>>();
        List<List<DepthDenmark>> tempLine = new ArrayList<List<DepthDenmark>>();

        m = parsed.get(0).get(0).getM();

        // Split the list based on the m index - note the index is opposite of the longitude coordinates
        for (List<DepthDenmark> splittedLines : parsed) {
            if ((splittedLines.get(0).getM()) > m) {
                // System.out.println("New line detected");
                lineSection.add(tempLine);
                tempLine = new ArrayList<List<DepthDenmark>>();
                tempLine.add(splittedLines);
                m = splittedLines.get(0).getM();
            } else {
                tempLine.add(splittedLines);
            }
        }
        lineSection.add(tempLine);

        // Reverse the list
        Collections.reverse(lineSection);

        // Seperate? Find all the required connection things

        List<NogoPolygon> allNeighboursLine = new ArrayList<NogoPolygon>();

        for (int i = 0; i < lineSection.size(); i++) {

            for (int j = 0; j < lineSection.get(i).size(); j++) {

                List<NogoPolygon> neighbours = new ArrayList<NogoPolygon>();

                // It has a next line
                if (i != lineSection.size() - 1) {
                    neighbours = connectNeighbourLines
                            .connectFindValidNeighbours(lineSection.get(i).get(j), lineSection.get(i + 1));

                    allNeighboursLine.addAll(neighbours);

                    //
                    // if (neighbours.size() != 0){
                    // for (int k = 0; k < neighbours.size(); k++) {
                    //
                    // //Check for overlap, first between line + 1 and the triangles
                    // if (!connectNeighbourLines.doesOverlap(neighbours.get(k), lineSection.get(i+1))){
                    // res.add(neighbours.get(k));
                    // }
                    //
                    // //Then for each triangle with the other triangles
                    //
                    // //If no overlap, add it
                    //
                    //
                    // }
                    // }
                }

            }

            // System.out.println(allNeighboursLine.size());

            // List<NogoPolygon> neighbours = connectNeighbourLines.connectFindValidNeighbours(lineSection.get(0).get(i),
            // lineSection.get(1));

        }

        // We found our neighbours, make sure they don't clash together

        if (type != DataType.SF_BAY || type != DataType.HUMBER) {

            List<NogoPolygon> finalNeighbours = connectNeighbourLines.triangleOverlap(allNeighboursLine);

            for (int k = 0; k < finalNeighbours.size(); k++) {
                res.add(finalNeighbours.get(k));
            }

        }

        // List<List<DepthDenmark>> neighbours = connectNeighbourLines.connectFindValidNeighbours(lineSection.get(0).get(0),
        // lineSection.get(1));
        // System.out.println(neighbours.size() + " neighbours found");
        //
        // neighbours = connectNeighbourLines.connectFindValidNeighbours(lineSection.get(1).get(0), lineSection.get(2));
        // System.out.println(neighbours.size() + " neighbours found");

        NogoPolygon polygon;
        NogoPolygon temp;

//        System.out.println("splitted lines is: " + parsed.size());

        // double lonOffset = 0.0007854;
        // The difference between each point / 2. This is used in calculating
        // the polygons surrounding the lines

        // 100m spacing
        // double latOffset = 0.00055504;
        // // double latOffset = 0.0;
        //
        // double lonOffset = 0.00055504;
        // double lonOffset = 0.0;

        // 50m spacing
        // double latOffset = 0.000290;

        for (List<DepthDenmark> splittedLines : parsed) {

            // Singleton
            if (splittedLines.size() == 1) {

                NogoPoint point = new NogoPoint(splittedLines.get(0).getLat(), splittedLines.get(0).getLon());
                temp = new NogoPolygon();
                temp.getPolygon().add(point);
                temp.getPolygon().add(point);

            } else {
                temp = new NogoPolygon();
                for (DepthDenmark dataEntries : splittedLines) {
                    NogoPoint point = new NogoPoint(dataEntries.getLat(), dataEntries.getLon());
                    temp.getPolygon().add(point);
                }

                /** Add to draw singletons **/
                // }

                NogoPoint westPoint = new NogoPoint(temp.getPolygon().get(0).getLat(), temp.getPolygon().get(0).getLon()
                        - lonOffset);
                NogoPoint eastPoint = new NogoPoint(temp.getPolygon().get(1).getLat(), temp.getPolygon().get(1).getLon()
                        + lonOffset);

                NogoPoint northWest = new NogoPoint(westPoint.getLat() + latOffset, westPoint.getLon());

                NogoPoint northEast = new NogoPoint(eastPoint.getLat() + latOffset, eastPoint.getLon());

                NogoPoint southWest = new NogoPoint(westPoint.getLat() - latOffset, westPoint.getLon());

                NogoPoint southEast = new NogoPoint(eastPoint.getLat() - latOffset, eastPoint.getLon());

                polygon = new NogoPolygon();

                polygon.getPolygon().add(northWest);
                polygon.getPolygon().add(southWest);
                polygon.getPolygon().add(southEast);
                polygon.getPolygon().add(northEast);

                res.add(polygon);

                /** Remove to draw singletons **/
                // }

            }
        }
        // System.out.println(res.size());

        return res;
    }

    private List<TideDenmark> compareTideLines(List<TideDenmark> list, int nCount) {

        List<TideDenmark> parsedList = new ArrayList<TideDenmark>();
        // Take nCount out
        // Compare them
        for (int i = 0; i < list.size(); i = i + nCount) {

            // take all the elements
            List<TideDenmark> tempList = new ArrayList<TideDenmark>();
            for (int j = 0; j < nCount; j++) {
                tempList.add(list.get(j + i));
            }

            // find lowest in tempList
            TideDenmark lowestTide = getLowestTide(tempList);
            // add it to parsedList
            parsedList.add(lowestTide);
        }

        return parsedList;
    }

    private TideDenmark getLowestTide(List<TideDenmark> tempList) {
        TideDenmark current = tempList.get(0);

        for (int i = 0; i < tempList.size(); i++) {
            if (current.getDepth() != null && tempList.get(i).getDepth() != null) {
                // Take the lowest
                if (current.getDepth() > tempList.get(i).getDepth()) {
                    current = tempList.get(i);
                }
            }
            // if current is null and the other isn't, take the none null one.
            // Is this the correct approach?
            if (current.getDepth() == null && tempList.get(i).getDepth() != null) {
                // System.out.println("Strangeness");
                current = tempList.get(i);
            }

        }

        return current;
    }

    private void combineVertical(List<TideDenmark> currentTideLine, List<List<DepthDenmark>> lines, int k) {

        // How many entries does lines has, is k + 5 > than lines.size then
        // treat then special - end of shit

        if (k + 5 > lines.size() - 1) {

            for (int i = k + 1; i < lines.size(); i++) {
                // System.out.println("We must work on " + k);
                combineHorizontal(currentTideLine, lines.get(k));
            }
            // System.out.println("Do something else");

        } else {

            // We have the line, work on the depth database part
            for (int j = 0; j < 4; j++) {
                // Five lines has to use the currentTideLine
                // Each line now has to iterate through the tideline
                combineHorizontal(currentTideLine, lines.get(k + j));
                // System.out.println("Currently working on: " + (k+j));
            }

        }

    }

    private void combineHorizontal(List<TideDenmark> currentTideLine, List<DepthDenmark> currentDepthList) {

        // Gets two lines - depth and tide
        int j = 0;

        // For each tidePoint, apply it's depth to all element in the depth
        for (int i = 0; i < currentTideLine.size(); i++) {
            // System.out.println("Current tide size is: "
            // + currentTideLine.size());
            // Apply this depth to some elements - 8 f them
            double currentDepth = 0;

            if (currentTideLine.get(i).getDepth() != null) {
                currentDepth = currentTideLine.get(i).getDepth();
            }
            // System.out.println("currentDepth is: " + currentDepth);

            // System.out.println("Depth something is: " +
            // currentTideLine.get(0).getId());

            combineDepth(currentDepth, currentDepthList, j);

            // Take element from

            j += 8;
        }

    }

    private void combineDepth(double currentDepth, List<DepthDenmark> currentDepthList, int j) {

        if (j + 8 > currentDepthList.size() - 1) {

            for (int i = j + 1; i < currentDepthList.size(); i++) {

                // It's null, screw it
                if (currentDepthList.get(i).getDepth() != null) {
                    double newDepth = currentDepthList.get(i).getDepth() - currentDepth;
                    currentDepthList.get(i).setDepth(newDepth);
                    // System.out.println("Depth is: " +
                    // currentDepthList.get(i+j).getDepth());
                }

                // System.out.println("We must work on " + i);

            }
            // System.out.println("Do something else");

        }

        if (j + 8 > currentDepthList.size() - 1) {
            // System.out.println("Do something else - depth line version");
        } else {

            // j is the current position, so we need to take that + 7
            for (int i = 0; i < 7; i++) {

                // It's null, screw it
                if (currentDepthList.get(i + j).getDepth() != null) {
                    double newDepth = currentDepthList.get(i + j).getDepth() - currentDepth;
                    currentDepthList.get(i + j).setDepth(newDepth);
                    // System.out.println("Depth is: " +
                    // currentDepthList.get(i+j).getDepth());
                }

            }

        }

    }

}
