package com.ambi.formula.gamemodel;

import com.ambi.formula.gamemodel.datamodel.Point;
import com.ambi.formula.gamemodel.datamodel.Polyline;
import com.ambi.formula.gamemodel.datamodel.Track;
import com.ambi.formula.gamemodel.utils.Calc;

/**
 *
 * @author Jiri Ambroz
 */
public class TrackEditor extends Track {

    private int indexLong;
    private int indexShort;

    public TrackEditor() {
    }

    public boolean clickOnTrack(Point click) {
        //pri editaci bodu se urci, ktery bod se bude hybat
        //kontrola, zda se kliklo na bod z leve krajnice
        for (int i = 1; i < getLeft().getLength() - 1; i++) {
            if (click.isEqual(getLeft().getPoint(i))) {
                indexLong = i;//zapamatovani si indexu meniciho se bodu na leve strane
                break;
            }
        }
        //kontrola, zda se kliklo na bod z prave krajnice
        if (indexLong == 0) {
            for (int i = 1; i < getRight().getLength() - 1; i++) {
                if (click.isEqual(getRight().getPoint(i))) {
                    indexShort = i;//zapamatovani si indexu meniciho se bodu na prave strane
                    break;
                }
            }
        }

        return (indexLong > 0 || indexShort > 0);
    }

    public boolean isNewPointValid(Point click) {
        //hledani mozneho pruseciku se zbytkem krajnice
        boolean interRight = false; //premistovany bod z prave krajnice se nekrizi
        boolean interLeft = false; //premistovany bod z leve krajnice se nekrizi

        //-------- cyklus projde vsechny USECKY LEVE KRAJNICE: -------------
        for (int i = 0; i < getLeft().getLength() - 1; i++) {
            Polyline actLeft = getLeft().getSegment(i);
            // ______ premistovany bod je z prave krajnice: ______
            if (indexShort > 0) {
                Point newEdge1 = getRight().getPoint(indexShort - 1);
                Point newEdge2 = getRight().getPoint(indexShort + 1);
                if ((int) Calc.crossing(click, newEdge1, actLeft)[0] != Calc.OUTSIDE
                        || (int) Calc.crossing(click, newEdge2, actLeft)[0] != Calc.OUTSIDE) {
                    interRight = true;
                    break;
                }
            }
            //______ premistovany bod je z leve krajnice: _______
            // nesmi se krizit ani s ostatnimi body leve krajnice
            if (indexLong > 0 && (i < indexLong - 1 || i > indexLong)) {
                Point newEdge1 = getLeft().getPoint(indexLong - 1);
                Point newEdge2 = getLeft().getPoint(indexLong + 1);
                if ((int) Calc.crossing(click, newEdge1, actLeft)[0] == Calc.INSIDE
                        || (int) Calc.crossing(click, newEdge2, actLeft)[0] == Calc.INSIDE) {
                    interLeft = true;
                }
            }
        }
        //-------- cyklus projde vsechny USECKY PRAVE KRAJNICE: ------------
        for (int i = 0; i < getRight().getLength() - 1; i++) {
            Polyline actRight = getRight().getSegment(i);
            //______ premistovany bod je z leve krajnice: _______
            if (indexLong > 0) {
                Point newEdge1 = getLeft().getPoint(indexLong - 1);
                Point newEdge2 = getLeft().getPoint(indexLong + 1);
                if ((int) Calc.crossing(click, newEdge1, actRight)[0] != Calc.OUTSIDE
                        || (int) Calc.crossing(click, newEdge2, actRight)[0] != Calc.OUTSIDE) {
                    interLeft = true;
                    break;
                }
            }
            // ______ premistovany bod je z prave krajnice: ______
            // nesmi se krizit ani s ostatnimi body prave krajnice
            if (indexShort > 0 && (i < (indexShort - 1) || i > (indexShort))) {
                Point newEdge1 = getRight().getPoint(indexShort - 1);
                Point newEdge2 = getRight().getPoint(indexShort + 1);
                if ((int) Calc.crossing(click, newEdge1, actRight)[0] == Calc.INSIDE
                        || (int) Calc.crossing(click, newEdge2, actRight)[0] == Calc.INSIDE) {
                    interRight = true;
                }
            }
        }

        boolean validPoint = true;
        //vyhodnoceni pruseciku
        if (indexShort > 0 && interRight == false) { //zadny prusecik praveho bodu se nenasel
            getLine(RIGHT).changePoint(click, indexShort);
        } else if (indexLong > 0 && interLeft == false) { //zadny prusecik leveho bodu se nenasel
            getLine(LEFT).changePoint(click, indexLong);
        } else if (interRight || interLeft) { //existuje prusecik
            validPoint = false;
        }

        indexLong = 0;
        indexShort = 0;
        return validPoint;
    }

}
