package com.ambi.formula.gamemodel.track;

import java.util.ArrayList;
import java.util.List;

import com.ambi.formula.gamemodel.datamodel.Point;
import com.ambi.formula.gamemodel.datamodel.Polyline;
import com.ambi.formula.gamemodel.datamodel.Track;
import com.ambi.formula.gamemodel.utils.Calc;

/**
 *
 * @author Jiri Ambroz
 */
public class TrackAnalyzer {

    private List<Polyline> checkLines;

    public TrackAnalyzer() {
        checkLines = new ArrayList<>();
    }

    /**
     * Metoda vytvori pole "prujezdovych" usecek, ktere pocitac projizdi pri prujezdu trati. Prvni
     * useckou je start a posledni je cil. Ke kazdemu bodu z delsi krajnice je prirazen bod z
     * protejsi strany. Delka pole se rovna delce delsi krajnice.
     *
     * @param track is track that will be analyzed
     */
    public void analyzeTrack(Track track) {
        int lowIndex = 0;
        int maxLength = track.getLong().getLength();
        Polyline longSide = track.getLong();
        Polyline shortSide = track.getShort();
        checkLines = new ArrayList<>();//seznam prujezdovych usecek
        getCheckLines().add(track.getStart()); // prvni checkLine je start

        //prochazeni delsi krajnice a hledani od kazdeho bodu vhodny protejsi bod
        for (int k = 1; k < maxLength - 1; k++) {
            int actIndex = lowIndex;
            boolean intersect = false;
            Point start = longSide.getPoint(k);//z tohoto bodu bude spustena kolmice
            Polyline segment = longSide.getSegment(k - 1);
            Point end = Calc.rightAngle(segment, track.getLongStr());

            // ------------------ PROCHAZENI KRATSI STRANY -------------------
            for (int i = lowIndex; i < shortSide.getLength() - 1; i++) {
                Point opPoint1 = shortSide.getPoint(i);
                Point opPoint2 = shortSide.getPoint(i + 1);
                Object[] cross = Calc.crossing(start, end, opPoint1, opPoint2);

                if ((int) cross[0] == Calc.INSIDE) {
                    if (Calc.distance(opPoint1, (Point) cross[1]) <= Calc.distance(opPoint2, (Point) cross[1])) {
                        actIndex = i;
                    } else {
                        actIndex = i + 1;
                    }
                    intersect = true;
                    break;
                } else if ((int) cross[0] == Calc.EDGE) {
                    actIndex = i + 1;
                    intersect = true;
                    break;
                }
            }//-------------------------------------------------------------------
            if (intersect == false && actIndex < shortSide.getLength() - 1) {
                /* kolmice neprotnula zadnou protejsi stranu, a vybere se nejblizsi mezi
                 * aktualnim poslednim a poslednim v linii */
                actIndex = shortSide.getLength() - 1;
            }
            actIndex = track.findNearest(segment, lowIndex, actIndex);
            if (lowIndex != actIndex) {
                //pokud se na kratsi strane vynechaji body, uz se nepocita posledni bod na kratke strane
                lowIndex++;
            }

            Point opPoint = shortSide.getPoint(actIndex);
            if (track.getLongStr() == Track.LEFT) {
                getCheckLines().add(new Polyline(start, opPoint));
            } else {
                getCheckLines().add(new Polyline(opPoint, start));
            }

            //prirazeni bodu z delsi strany i pro vynechane body na kratsi strane
            while (lowIndex < actIndex) {
                opPoint = shortSide.getPoint(lowIndex);
                start = Calc.findNearestPoint(opPoint, segment.getPoints());
                //je zachovano poradi: prvni bod je na leve strane a druhy na prave:
                if (track.getLongStr() == Track.LEFT) {
                    getCheckLines().add(getCheckLines().size() - 1, new Polyline(start, opPoint));
                } else {
                    getCheckLines().add(getCheckLines().size() - 1, new Polyline(opPoint, start));
                }
                lowIndex++;
            }
            lowIndex = actIndex;
        }

        //k poslednim bodum kratsi strany nejsou prirazeny zadne body z delsi strany:
        lowIndex++;
        Point start = track.getLong().getPreLast();
        while (lowIndex < track.getShort().getLength() - 1) {
            Point opPoint = shortSide.getPoint(lowIndex);
            //je zachovano poradi: prvni bod je na leve strane a druhy na prave:
            if (track.getLongStr() == Track.LEFT) {
                getCheckLines().add(new Polyline(start, opPoint));
            } else {
                getCheckLines().add(new Polyline(opPoint, start));
            }
            lowIndex++;
        }
        getCheckLines().add(track.getFinish());
    }

    public void clearLines() {
        getCheckLines().clear();
    }

    public List<Polyline> getCheckLines() {
        return checkLines;
    }
}
