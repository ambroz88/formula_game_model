package com.ambi.formula.gamemodel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import com.ambi.formula.gamemodel.datamodel.Paper;
import com.ambi.formula.gamemodel.datamodel.Point;
import com.ambi.formula.gamemodel.datamodel.Polyline;
import com.ambi.formula.gamemodel.datamodel.Track;
import com.ambi.formula.gamemodel.datamodel.Turns;
import com.ambi.formula.gamemodel.labels.HintLabels;
import com.ambi.formula.gamemodel.utils.Calc;

/**
 * This class serves as model for whole game. It communicates with other models
 * e.g. for building the track or making next turn. It also fires property
 * changes for GUI.
 *
 * @author Jiri Ambroz
 */
public class GameModel {

    public final static int BUILD_LEFT = 1;
    public final static int BUILD_RIGHT = 2;
    public final static int EDIT_PRESS = 3;
    public final static int EDIT_RELEASE = 4;
    public final static int FIRST_TURN = 5;
    public final static int NORMAL_TURN = 6;
    public final static int AUTO_CRASH = 7;
    public final static int AUTO_FINISH = 8;

    private final CompSimul computer;
    private final TrackBuilder buildTrack;
    private final MakeTurn turn;
    private final PropertyChangeSupport prop;
    private final Paper paper;

    private List<Polyline> checkLines;
    private Polyline points;
    private Polyline badPoints;
    private Turns turns;
    private String language;
    private HintLabels hintLabels;

    private int stage;
    private int player;
    private int indexLong;
    private int indexShort;
    private int gridSize;

    public GameModel() {
        stage = 1;
        gridSize = 15;
        paper = new Paper();
        points = new Polyline(Polyline.GOOD_SET);
        badPoints = new Polyline(Polyline.CROSS_SET);
        turns = new Turns();

        prop = new PropertyChangeSupport(this);

        buildTrack = new TrackBuilder(this);
        turn = new MakeTurn(this);
        computer = new CompSimul(this);
    }

    /**
     * Metoda zaokrouhli souradnice bodu podle nastaveneho rozmeru mrizky, takze
     * se bod na mrizku "prichyti".
     *
     * @param input bod, kteremu se zaokrouhli souradnice
     * @return point - bod se zaokrouhlenymi souradnicemi
     */
    public Point snapping(Point input) {
        //gridSize je velikost mrizky
        double roundX = Math.round(input.x / gridSize) * gridSize;
        double roundY = Math.round(input.y / gridSize) * gridSize;
        double squareX = Math.round(roundX / gridSize);
        double squareY = Math.round(roundY / gridSize);
        return new Point(squareX, squareY);
    }

    //============================ FIRE CHANGES TO GUI =========================
    public void fireCrash(int count) {
        //nastaveni informace o narazu do mantinelu
        String text = hintLabels.getValue(HintLabels.OUCH) + " " + turn.getFormula(turn.getActID()).getName() + " "
                + hintLabels.getValue(HintLabels.CRASH) + " " + count + "!!!";
        firePropertyChange("crash", "", text); //cought by StatBar
    }

    public void repaintScene() {
        firePropertyChange("repaint", false, true); //cought by Draw
    }

    public void fireTrackReady(boolean ready) {
        firePropertyChange("startVisible", !ready, ready); // cought by StartMenu, TrackMenu
        if (ready) {
            fireHint(HintLabels.TRACK_READY);
            repaintScene();
        } else {
            fireHint(HintLabels.EMPTY);
        }
    }

    public void fireHint(String hintLabelProperty) {
        firePropertyChange("hint", "-1", hintLabels.getValue(hintLabelProperty)); //cought by StatBar
    }

    public void endGame() {
        setStage(BUILD_LEFT);
        firePropertyChange("buildTrack", false, true);
        firePropertyChange("startDraw", false, true); // cought by TrackMenu and Draw
        reset("player");
    }

    //============================ SETTERS AND GETTERS =========================
    public int gridSize() {
        return gridSize;
    }

    /**
     * Setter for size of square edge on the "paper".
     *
     * @param size is new grid size
     */
    public void setGridSize(int size) {
        int old = gridSize();
        gridSize = size;
        firePropertyChange("grid", old, gridSize()); //cought by Draw
    }

    public Polyline getPoints() {
        return points;
    }

    public void setPoints(Polyline points) {
        this.points = points;
    }

    public Polyline getBadPoints() {
        return badPoints;
    }

    public void setBadPoints(Polyline badPoints) {
        this.badPoints = badPoints;
    }

    public Turns getTurns() {
        return turns;
    }

    public void setTurns(Turns turns) {
        this.turns = turns;
    }

    public int getStage() {
        return stage;
    }

    /**
     * Metoda nastavi fazi hry, ktera je udana celym cislem.
     *
     * @param stage 1-2 = staveni leve, resp. prave krajnice; 3-4 = editace bodu
     * trate; 5 = zahajovaci tah; 6 = normalni tah hrace; 7-8 = automaticke tahy
     * (naraz resp. projeti cilem)
     */
    public void setStage(int stage) {
        this.stage = stage;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
        hintLabels = new HintLabels(language);
        prop.firePropertyChange("language", null, language);
    }

    public MakeTurn getTurn() {
        return turn;
    }

    public TrackBuilder getBuilder() {
        return buildTrack;
    }

    public Paper getPaper() {
        return paper;
    }

    public int getPaperWidth() {
        return paper.getWidth();
    }

    public void setPaperWidth(int width) {
        int old = getPaper().getWidth();
        getPaper().setWidth(width);
        firePropertyChange("paperWidth", old, width); //cought by Draw and Options
    }

    public int getPaperHeight() {
        return paper.getHeight();
    }

    public void setPaperHeight(int height) {
        int old = paper.getHeight();
        getPaper().setHeight(height);
        firePropertyChange("paperHeight", old, height); //cought by Draw and Options
    }

    public List<Polyline> getCheckLines() {
        return checkLines;
    }

    /**
     * Method for clearing scene: always it deletes all points. It could be
     * delete more things - it depends on parameter task.
     *
     * @param task if it is "player" - formulas are deleted; "game" - all is
     * deleted (track, formulas, statistics)
     */
    public void reset(String task) {
        points.clear();
        badPoints.clear();
        if (task.equals("")) {
            fireHint(HintLabels.EMPTY);
        } else {
            turn.getFormula(1).reset();
            turn.getFormula(2).reset();
            if (task.equals("game")) {
                setStage(BUILD_LEFT);
                buildTrack.getTrack().reset();
            }
        }
    }

    /**
     * Zobrazeni zpravy o vitezi na informacni panel. Ukonceni hry.
     */
    public void decision() {
        points.clear();
        badPoints.clear();
        setStage(9); //konec hry
        String finalMessage;
        if (turn.getFormula(1).getWin() && turn.getFormula(2).getWin() == false) {
            finalMessage = turn.getFormula(1).getName() + " " + hintLabels.getValue(HintLabels.WINNER);
        } else if (turn.getFormula(2).getWin() && turn.getFormula(1).getWin() == false) {
            finalMessage = turn.getFormula(2).getName() + " " + hintLabels.getValue(HintLabels.WINNER);
        } //oba souperi projeli cilem a rozhoduje mensi ujeta vzdalenost
        else if (turn.getFormula(1).getDist() < turn.getFormula(2).getDist()) {
            finalMessage = turn.getFormula(1).getName() + " " + hintLabels.getValue(HintLabels.WINNER);
        } else if (turn.getFormula(1).getDist() > turn.getFormula(2).getDist()) {
            finalMessage = turn.getFormula(2).getName() + " " + hintLabels.getValue(HintLabels.WINNER);
        } else {
            finalMessage = hintLabels.getValue(HintLabels.BOTH_WIN);
        }
        firePropertyChange("winner", "", finalMessage); // cought by StatBar
    }

    // ========================== METHODS FROM GUI ===========================
    /**
     * This is main method that decides what happens when user clicked in the
     * main pnnel. According the game stage there is launch suitable action.
     *
     * @param panelClick is point in main panel where user clicked
     */
    public void windowMouseClicked(Point panelClick) {
        Point click = snapping(new Point(panelClick.x, panelClick.y)); //prichyceni kliknuti na mrizku
        if (!getPaper().isOutside(click)) {
            fireHint(HintLabels.EMPTY);
            if (getStage() == BUILD_LEFT) {//left side is build
                buildTrack.buildTrack(click, Track.LEFT);
            } else if (getStage() == BUILD_RIGHT) {//right side is build
                buildTrack.buildTrack(click, Track.RIGHT);
            } else if (getStage() > EDIT_RELEASE && getStage() <= AUTO_CRASH) {//faze hry behem zavodu
                if (player == 1) { //herni mod SINGLE
                    turn.turn(click);
                    if (turn.getActID() == 1) {
                        if (getStage() == FIRST_TURN) {
                            fireHint(HintLabels.START_POSITION);
                        }
                    }
                    //kontrola konce hry:
                    if (turn.getFinishType() == 1 && turn.getFormula(1).getWin() == true || turn.getFormula(2).getWin() == true) {
                        decision();
                    } else if (turn.getActID() == 2 && getStage() != FIRST_TURN) {
                        //pokracuje tahem pocitac
                        click = computer.compTurn();
                        turn.turn(click);
                        if (turn.getActID() == 2) {
                            fireHint(HintLabels.NEXT_COMP_TURN);
                        }
                    }
                    //kontrola konce hry:
                    if (turn.getFormula(1).getWin() == true || turn.getFormula(2).getWin() == true) {
                        decision();
                    }
                } else// herni mod DUEL
                {
                    if (turn.getActID() == 1) {//hraje hrac 1
                        turn.turn(click);
                        //kontrola konce hry:
                        if (turn.getFinishType() == 1 && (turn.getFormula(1).getWin() == true || turn.getFormula(2).getWin() == true)) {
                            decision();
                        }
                    } else { //hraje hrac 2
                        turn.turn(click);
                        //kontrola konce hry:
                        if (turn.getFormula(1).getWin() == true || turn.getFormula(2).getWin() == true) {
                            decision();
                        }
                    }
                }
            }
        } else {
            fireHint(HintLabels.OUTSIDE);
        }
    }

    public void keyPressed(int position) {
        if (getStage() > FIRST_TURN && getStage() <= AUTO_CRASH && turns.getTurn(position).isExist()) {//faze hry behem zavodu
            Point click = turns.getTurn(position).getPosition();
            if (player == 1) { //herni mod SINGLE
                turn.turn(click);
                if (turn.getActID() == 1) {
                    if (getStage() == FIRST_TURN) {
                        fireHint(HintLabels.START_POSITION);
                    }
                }
                //kontrola konce hry:
                if (turn.getFinishType() == 1 && turn.getFormula(1).getWin() == true || turn.getFormula(2).getWin() == true) {
                    decision();
                } else if (turn.getActID() == 2 && getStage() != FIRST_TURN) {
                    //pokracuje tahem pocitac
                    click = computer.compTurn();
                    turn.turn(click);
                    if (turn.getActID() == 2) {
                        fireHint(HintLabels.NEXT_COMP_TURN);
                    }
                }
                //kontrola konce hry:
                if (turn.getFormula(1).getWin() == true || turn.getFormula(2).getWin() == true) {
                    decision();
                }
            } else// herni mod DUEL
            {
                if (turn.getActID() == 1) {//hraje hrac 1
                    turn.turn(click);
                    //kontrola konce hry:
                    if (turn.getFinishType() == 1 && (turn.getFormula(1).getWin() == true || turn.getFormula(2).getWin() == true)) {
                        decision();
                    }
                } else { //hraje hrac 2
                    turn.turn(click);
                    //kontrola konce hry:
                    if (turn.getFormula(1).getWin() == true || turn.getFormula(2).getWin() == true) {
                        decision();
                    }
                }
            }
            firePropertyChange("repaint", false, true);
        }
    }

    /**
     * EDITACE BODU - stisknuti mysi
     *
     * @param panelClick je bod na hlavnim panelu, kam uzivatel klikl
     * @return
     */
    public boolean isTrackEdit(Point panelClick) {
        if (getStage() == EDIT_PRESS) {//faze editace trati
            //urci se premistovany bod
            Point click = snapping(new Point(panelClick.x, panelClick.y)); //prichyceni kliknuti na mrizku
            //pri editaci bodu se urci, ktery bod se bude hybat
            //kontrola, zda se kliklo na bod z leve krajnice
            for (int i = 1; i < buildTrack.getTrack().left().getLength() - 1; i++) {
                if (click.isEqual(buildTrack.getTrack().left().getPoint(i))) {
                    indexLong = i;//zapamatovani si indexu meniciho se bodu na leve strane
                    fireHint(HintLabels.EMPTY);
                    break;
                }
            }
            //kontrola, zda se kliklo na bod z prave krajnice
            if (indexLong == 0) {
                for (int i = 1; i < buildTrack.getTrack().right().getLength() - 1; i++) {
                    if (click.isEqual(buildTrack.getTrack().right().getPoint(i))) {
                        indexShort = i;//zapamatovani si indexu meniciho se bodu na prave strane
                        fireHint(HintLabels.EMPTY);
                        break;
                    }
                }
            }
            setStage(EDIT_RELEASE);
            if (indexLong == 0 && indexShort == 0) {
                //pokud se nekliklo na zadny bod, zobrazi se info
                fireHint(HintLabels.NO_POINT);
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * EDITACE BODU - uvolneni mysi
     *
     * @param panelClick je bod na hlavnim panelu, kde uzivatel uvolnil mys (coz
     * znamena misto presunuti bodu trati)
     */
    public void windowMouseReleased(Point panelClick) {
        if (getStage() == EDIT_RELEASE) {//urci se nove souradnice premistovaneho bodu. Kontrola kolize s ostatni trati
            Point click = snapping(new Point(panelClick.x, panelClick.y)); //prichyceni kliknuti na mrizku
            //hledani mozneho pruseciku se zbytkem krajnice
            boolean interRight = false; //premistovany bod z prave krajnice se nekrizi
            boolean interLeft = false; //premistovany bod z leve krajnice se nekrizi

            //-------- cyklus projde vsechny USECKY LEVE KRAJNICE: -------------
            for (int i = 0; i < buildTrack.getTrack().left().getLength() - 1; i++) {
                Polyline actLeft = new Polyline(buildTrack.getTrack().left().getPoint(i), buildTrack.getTrack().left().getPoint(i + 1));
                // ______ premistovany bod je z prave krajnice: ______
                if (indexShort > 0) {
                    Point newEdge1 = buildTrack.getTrack().right().getPoint(indexShort - 1);
                    Point newEdge2 = buildTrack.getTrack().right().getPoint(indexShort + 1);
                    if ((int) Calc.crossing(click, newEdge1, actLeft)[0] != -1
                            || (int) Calc.crossing(click, newEdge2, actLeft)[0] != -1) {
                        interRight = true;
                        break;
                    }
                }
                //______ premistovany bod je z leve krajnice: _______
                // nesmi se krizit ani s ostatnimi body leve krajnice
                if (indexLong > 0 && (i < (indexLong - 1) || i > (indexLong))) {
                    Point newEdge1 = buildTrack.getTrack().left().getPoint(indexLong - 1);
                    Point newEdge2 = buildTrack.getTrack().left().getPoint(indexLong + 1);
                    if ((int) Calc.crossing(click, newEdge1, actLeft)[0] == 1
                            || (int) Calc.crossing(click, newEdge2, actLeft)[0] == 1) {
                        interLeft = true;
                    }
                }
            }
            //-------- cyklus projde vsechny USECKY PRAVE KRAJNICE: ------------
            for (int i = 0; i < buildTrack.getTrack().right().getLength() - 1; i++) {
                Polyline actRight = new Polyline(buildTrack.getTrack().right().getPoint(i), buildTrack.getTrack().right().getPoint(i + 1));
                //______ premistovany bod je z leve krajnice: _______
                if (indexLong > 0) {
                    Point newEdge1 = buildTrack.getTrack().left().getPoint(indexLong - 1);
                    Point newEdge2 = buildTrack.getTrack().left().getPoint(indexLong + 1);
                    if ((int) Calc.crossing(click, newEdge1, actRight)[0] != -1
                            || (int) Calc.crossing(click, newEdge2, actRight)[0] != -1) {
                        interLeft = true;
                        break;
                    }
                }
                // ______ premistovany bod je z prave krajnice: ______
                // nesmi se krizit ani s ostatnimi body prave krajnice
                if (indexShort > 0 && (i < (indexShort - 1) || i > (indexShort))) {
                    Point newEdge1 = buildTrack.getTrack().right().getPoint(indexShort - 1);
                    Point newEdge2 = buildTrack.getTrack().right().getPoint(indexShort + 1);
                    if ((int) Calc.crossing(click, newEdge1, actRight)[0] == 1
                            || (int) Calc.crossing(click, newEdge2, actRight)[0] == 1) {
                        interRight = true;
                    }
                }
            }
            //vyhodnoceni pruseciku
            if (indexShort > 0 && interRight == false) { //zadny prusecik praveho bodu se nenasel
                buildTrack.changePoint(Track.RIGHT, click, indexShort); //prepsani noveho bodu
            } else if (indexLong > 0 && interLeft == false) { //zadny prusecik leveho bodu se nenasel
                buildTrack.changePoint(Track.LEFT, click, indexLong); //prepsani noveho bodu
            } else if (interRight || interLeft) { //existuje prusecik
                fireHint(HintLabels.CROSSING);
            }
            if (interLeft == false || interRight == false) {
                //pokud se draha zmenila, prekreslim lomove body
                Polyline bad = new Polyline(Polyline.CROSS_SET);
                for (int i = 1; i < buildTrack.getTrack().left().getLength() - 1; i++) {
                    bad.addPoint(buildTrack.getTrack().left().getPoint(i));
                }
                for (int i = 1; i < buildTrack.getTrack().right().getLength() - 1; i++) {
                    bad.addPoint(buildTrack.getTrack().right().getPoint(i));
                }
                setBadPoints(bad);
            }
            indexLong = 0;
            indexShort = 0;
            setStage(EDIT_PRESS);
        }
    }

    /**
     * It prepares game for start - track is correctly drawn. When it is single
     * mode, track is analyzed for computer turns.
     *
     * @param playerCount is number of players
     */
    public void prepareGame(int playerCount) {
        if (playerCount == 1) { //single mode
            computer.setTrackIndex(0);
            checkLines = buildTrack.getTrack().analyzeTrack();
        }
        reset("player");
        setPoints(turn.startPosition(buildTrack.getTrack().getStart()));
        buildTrack.getTrack().setLeftWidth(3);
        buildTrack.getTrack().setRightWidth(3);

        setStage(FIRST_TURN);
        player = playerCount;
        turn.setID(1, 2);   //it says which formula is on turn and which is second
        firePropertyChange("startGame", 0, turn.getActID()); // cought by Draw and TrackMenu
    }

    /**
     * Method for switching start and finish. It doesn't draw new start
     * positions. It deletes and restarts variables so game will be ready to
     * start.
     */
    public void switchStart() {
        reset("player");
        setStage(BUILD_LEFT);
        getBuilder().getTrack().switchStart();
        repaintScene();
    }

    public void loadSelectedTrack() {
        //cought by TracksComponent
        firePropertyChange("loadTrack", false, true);
    }

    public void firePropertyChange(String prop, Object oldValue, Object newValue) {
        this.prop.firePropertyChange(prop, oldValue, newValue);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        prop.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        prop.removePropertyChangeListener(listener);
    }

}
