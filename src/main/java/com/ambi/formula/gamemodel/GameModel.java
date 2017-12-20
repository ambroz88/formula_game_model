package com.ambi.formula.gamemodel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.List;

import com.ambi.formula.gamemodel.datamodel.Paper;
import com.ambi.formula.gamemodel.datamodel.Point;
import com.ambi.formula.gamemodel.datamodel.Polyline;
import com.ambi.formula.gamemodel.datamodel.Track;
import com.ambi.formula.gamemodel.datamodel.Turns;
import com.ambi.formula.gamemodel.labels.HintLabels;
import com.ambi.formula.gamemodel.utils.TrackIO;

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
    public final static int GAME_OVER = 9;

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

    // ========================== METHODS FROM GUI ===========================
    /**
     * This is main method that decides what happens when user clicked in the
     * main pnnel. According the game stage there is launch suitable action.
     *
     * @param click is point in main panel where user clicked
     */
    public void windowMouseClicked(Point click) {
        click.toGridUnits(gridSize);
        if (!getPaper().isOutside(click)) {//TODO: if the turns is through finishline, than it should be valid

            if (getStage() != FIRST_TURN || getStage() == FIRST_TURN && turn.getActID() == 2) {
                fireHint(HintLabels.EMPTY);
            }

            if (getStage() == BUILD_LEFT) {//left side is build
                buildTrack.buildTrack(click, Track.LEFT);
            } else if (getStage() == BUILD_RIGHT) {//right side is build
                buildTrack.buildTrack(click, Track.RIGHT);
            } else if (getStage() > EDIT_RELEASE && getStage() <= AUTO_CRASH) {
                processPlayerTurn(click);
            }

        } else {
            fireHint(HintLabels.OUTSIDE);
        }
    }

    public void keyPressed(int position) {
        //phase of the race
        if (getStage() > FIRST_TURN && getStage() <= AUTO_CRASH && turns.getTurn(position).isExist()) {
            Point click = turns.getTurn(position).getPosition();
            processPlayerTurn(click);
            repaintScene();
        }
    }

    private void processPlayerTurn(Point click) {
        turn.turn(click);
        checkWinner();
        //SINGLE mode
        if (player == 1 && turn.getActID() == 2 && getStage() != FIRST_TURN) {
            //computer turn
            click = computer.compTurn();
            turn.turn(click);
            if (turn.getActID() == 2) {
                fireHint(HintLabels.NEXT_COMP_TURN);
            }
            checkWinner();
        }
    }

    /**
     * EDITACE BODU - stisknuti mysi
     *
     * @param click je bod na hlavnim panelu, kam uzivatel klikl
     * @return
     */
    public boolean isTrackEdit(Point click) {
        boolean onTrack = false;
        if (getStage() == EDIT_PRESS) {

            click.toGridUnits(gridSize);
            onTrack = buildTrack.clickOnTrack(click);
            if (!onTrack) {
                fireHint(HintLabels.NO_POINT);
            } else {
                fireHint(HintLabels.EMPTY);
            }
            setStage(EDIT_RELEASE);
        }
        return onTrack;
    }

    /**
     * EDITACE BODU - uvolneni mysi
     *
     * @param click je bod na hlavnim panelu, kde uzivatel uvolnil mys (coz
     * znamena misto presunuti bodu trati)
     */
    public void windowMouseReleased(Point click) {
        if (getStage() == EDIT_RELEASE) {//urci se nove souradnice premistovaneho bodu. Kontrola kolize s ostatni trati
            click.toGridUnits(gridSize);

            if (!buildTrack.isNewPointValid(click)) {
                fireHint(HintLabels.CROSSING);
            }
            setStage(EDIT_PRESS);
        }
    }

    /**
     * It prepares game for start - track is correctly drawn. When it is single
     * mode, track is analysed for computer turns.
     *
     * @param playerCount is number of players
     */
    public void prepareGame(int playerCount) {
        if (playerCount == 1) { //single mode
            computer.setTrackIndex(0);
            checkLines = buildTrack.analyzeTrack();
        }
        resetPlayers();
        setPoints(turn.startPosition(buildTrack.getStart()));
        buildTrack.setLeftWidth(3);
        buildTrack.setRightWidth(3);

        setStage(FIRST_TURN);
        player = playerCount;
        turn.setID(1, 2);   //it says which formula is on turn and which is second
        fireHint(HintLabels.START_POSITION);
        firePropertyChange("startGame", 0, turn.getActID()); // cought by Draw and TrackMenu
    }

    /**
     * Method for switching start and finish. It doesn't draw new start
     * positions. It deletes and restarts variables so game will be ready to
     * start.
     */
    public void switchStart() {
        setStage(BUILD_LEFT);
        getBuilder().switchStart();
        resetPlayers();
    }

    public boolean saveTrack(String trackName) {
        boolean saved;
        try {
            TrackIO.trackToJSON(getBuilder().getTrack(), trackName);
            // cought by TrackTopComponent:
            firePropertyChange("newTrack", false, true);
            fireHint(HintLabels.HINT_SAVED);
            saved = true;
        } catch (IOException ex) {
            fireHint(HintLabels.HINT_FAILED);
            saved = false;
        }
        return saved;
    }

    /**
     * Method for clearing whole scene: track, formulas and points.
     */
    public void resetGame() {
        setStage(BUILD_LEFT);
        buildTrack.reset();
        resetPlayers();
    }

    /**
     * Method for clearing formulas and points.
     */
    public void resetPlayers() {
        turn.getFormula(1).reset();
        turn.getFormula(2).reset();
        resetPoints();
    }

    /**
     * Method for clearing points.
     */
    public void resetPoints() {
        points.clear();
        badPoints.clear();
        repaintScene();
    }

    public void checkWinner() {

        if (turn.getFormula(2).getWin() == true) {
            winnerAnnoucment();
        } else if (turn.getFormula(1).getWin() == true && turn.getFinishType() != MakeTurn.SECOND_CHANCE) {
            winnerAnnoucment();
        }

    }

    /**
     * It generates the message about winner and game is finished.
     */
    public void winnerAnnoucment() {
        points.clear();
        badPoints.clear();
        setStage(GAME_OVER);
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
        repaintScene();
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
        resetPlayers();
    }

    public void loadSelectedTrack() {
        //cought by TracksComponent
        firePropertyChange("loadTrack", false, true);
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
