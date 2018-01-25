package com.ambi.formula.gamemodel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;

import com.ambi.formula.gamemodel.datamodel.Paper;
import com.ambi.formula.gamemodel.datamodel.Point;
import com.ambi.formula.gamemodel.datamodel.Track;
import com.ambi.formula.gamemodel.enums.FormulaType;
import com.ambi.formula.gamemodel.labels.HintLabels;
import com.ambi.formula.gamemodel.track.TrackAnalyzer;
import com.ambi.formula.gamemodel.track.TrackBuilder;
import com.ambi.formula.gamemodel.turns.ComputerEasy;
import com.ambi.formula.gamemodel.turns.ComputerModerate;
import com.ambi.formula.gamemodel.turns.ComputerTurnCore;
import com.ambi.formula.gamemodel.turns.TurnMaker;
import com.ambi.formula.gamemodel.utils.TrackIO;

/**
 * This class serves as model for whole game. It communicates with other models e.g. for building
 * the track or making next turn. It also fires property changes for GUI.
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

    private ComputerTurnCore computer;
    private final TrackBuilder buildTrack;
    private final TrackAnalyzer analyzer;
    private final TurnMaker turnMaker;
    private final Paper paper;
    private final PropertyChangeSupport prop;

    private String language;
    private HintLabels hintLabels;

    private int stage;
    private int player;

    public GameModel() {
        stage = BUILD_LEFT;
        paper = new Paper();
        analyzer = new TrackAnalyzer();

        prop = new PropertyChangeSupport(this);

        buildTrack = new TrackBuilder(this);
        turnMaker = new TurnMaker(this);
    }

    // ========================== METHODS FROM GUI ===========================
    /**
     * This is main method that decides what happens when user clicked in the main pnnel. According
     * the game stage there is launch suitable action.
     *
     * @param click is point in main panel where user clicked
     */
    public void windowMouseClicked(Point click) {
        click.toGridUnits(getPaper().getGridSize());
        if (!getPaper().isOutside(click) || getStage() == AUTO_FINISH) {//TODO: if the turns is through finishline, than it should be valid

            if (getStage() != FIRST_TURN || getStage() == FIRST_TURN && turnMaker.getActID() == 2) {
                fireHint(HintLabels.EMPTY);
            }

            if (getStage() == BUILD_LEFT) {//left side is build
                buildTrack.buildTrack(click, Track.LEFT);
                fireHint(getBuilder().getMessage());
            } else if (getStage() == BUILD_RIGHT) {//right side is build
                buildTrack.buildTrack(click, Track.RIGHT);
                fireHint(getBuilder().getMessage());
            } else if (getStage() > EDIT_RELEASE && getStage() <= AUTO_FINISH) {
                processPlayerTurn(click);
            }

        } else {
            fireHint(HintLabels.OUTSIDE);
        }
    }

    public void keyPressed(int position) {
        //phase of the race
        if (getStage() > FIRST_TURN && getStage() <= AUTO_FINISH && getTurnMaker().getTurns().getTurn(position).isExist()) {
            Point click = getTurnMaker().getTurns().getTurn(position).getPoint();
            processPlayerTurn(click);
            repaintScene();
        }
    }

    private void processPlayerTurn(Point click) {
        turnMaker.turn(click);
        checkWinner();
        //SINGLE mode
        if (player == 1 && turnMaker.getActID() == 2 && getStage() != FIRST_TURN && getStage() != GAME_OVER) {
            //computer turn
            click = computer.selectComputerTurn(2);
            turnMaker.turn(click);
            if (turnMaker.getActID() == 2) {
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

            click.toGridUnits(getPaper().getGridSize());
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
     * @param click je bod na hlavnim panelu, kde uzivatel uvolnil mys (coz znamena misto presunuti
     * bodu trati)
     */
    public void windowMouseReleased(Point click) {
        if (getStage() == EDIT_RELEASE) {//urci se nove souradnice premistovaneho bodu. Kontrola kolize s ostatni trati
            click.toGridUnits(getPaper().getGridSize());

            setStage(EDIT_PRESS);
            if (!buildTrack.isNewPointValid(click)) {
                fireHint(HintLabels.CROSSING);
            }
        }
    }

    /**
     * It prepares game for start - track is correctly drawn. When it is single mode, track is
     * analysed for computer turns.
     */
    public void prepareGame() {
        FormulaType computerLevel = turnMaker.getComputerType();
        if (computerLevel != FormulaType.Player) { //single mode
            if (computerLevel == FormulaType.ComputerEasy) {
                computer = new ComputerEasy(this);
            } else if (computerLevel == FormulaType.ComputerMedium) {
                computer = new ComputerModerate(this);
            }
            computer.startAgain();
            player = 1;
            getAnalyzer().analyzeTrack(getBuilder().getTrack());
        } else {
            player = 2;
        }

        resetPlayers();
        getBuilder().setPoints(turnMaker.startPosition(buildTrack.getStart()));
        buildTrack.setLeftWidth(3);
        buildTrack.setRightWidth(3);

        setStage(FIRST_TURN);
        turnMaker.setID(1, 2);   //it says which formula is on turn and which is second
        fireHint(HintLabels.START_POSITION);
        firePropertyChange("startGame", 0, turnMaker.getActID()); // cought by Draw and TrackMenu
    }

    /**
     * Method for switching start and finish. It doesn't draw new start positions. It deletes and
     * restarts variables so game will be ready to start.
     */
    public void switchStart() {
        setStage(BUILD_LEFT);
        getBuilder().switchStart();
        getAnalyzer().analyzeTrack(getBuilder().getTrack());
        resetPlayers();
    }

    // ---------------- METHOD FROM TRACK MENU --------------------
    public void startBuild(int side) {
        if (getBuilder().getOppLine(side).getLength() != 1) {
            repaintScene();
            getBuilder().generateEndPoints(side);
            if (side == Track.LEFT) {
                setStage(GameModel.BUILD_LEFT);
            } else {
                setStage(GameModel.BUILD_RIGHT);
            }
        } else {
            if (side == Track.LEFT) {
                fireHint(HintLabels.RIGHT_SIDE_FIRST);
                //caught by TrackMenu:
                firePropertyChange("rightSide", false, true);
            } else {
                fireHint(HintLabels.LEFT_SIDE_FIRST);
                //caught by TrackMenu:
                firePropertyChange("leftSide", false, true);
            }
        }
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

    public void endGame() {
        setStage(BUILD_LEFT);
        firePropertyChange("buildTrack", false, true); // cought by TrackMenu
        firePropertyChange("startDraw", false, true); // cought by TrackMenu and Draw
        getAnalyzer().clearLines();
        resetPlayers();
    }

    public void loadTrackActions() {
        endGame();
        getPaper().setWidth(getBuilder().getMaxWidth() + 10);
        getPaper().setHeight(getBuilder().getMaxHeight() + 10);

        fireTrackReady(true);
        repaintScene();
    }

    /**
     * It prepares track for editing mode, so all points in track will be visibly marked.
     */
    public void clearTrackInside() {
        getAnalyzer().clearLines();
        getBuilder().getPoints().clear();
        setStage(EDIT_PRESS);
    }

    /**
     * Method for clearing whole scene: track, formulas and points.
     */
    public void resetGame() {
        setStage(BUILD_LEFT);
        getBuilder().reset();
        getAnalyzer().clearLines();
        resetPlayers();
    }

    /**
     * Method for clearing formulas and points.
     */
    public void resetPlayers() {
        turnMaker.getFormula(1).reset();
        turnMaker.getFormula(2).reset();
        getBuilder().getPoints().clear();
        if (computer != null) {
            computer.reset();
        }
        repaintScene();
    }

    public void checkWinner() {
        if (turnMaker.getFormula(2).getWin() == true) {
            winnerAnnouncement();
        } else if (turnMaker.getFormula(1).getWin() == true && turnMaker.getFinishType() != TurnMaker.WIN_LAST_TURN) {
            winnerAnnouncement();
        } else if (turnMaker.getFormula(1).getWin() == true && turnMaker.getActID() == 1) {
            winnerAnnouncement();
        }
    }

    /**
     * It generates the message about winner and game is finished.
     */
    public void winnerAnnouncement() {
        getTurnMaker().resetTurns();
        setStage(GAME_OVER);

        //cought by StatisticComponent
        firePropertyChange("winner", "", getTurnMaker().createWinnerMessage());
        repaintScene();
    }

    //============================ FIRE CHANGES TO GUI =========================
    public void fireCrash(int count) {
        //nastaveni informace o narazu do mantinelu
        String text = hintLabels.getValue(HintLabels.OUCH) + " " + turnMaker.getFormula(turnMaker.getActID()).getName() + " "
                + hintLabels.getValue(HintLabels.CRASH) + " " + count + "!!!";
        //cought by StatisticComponent
        firePropertyChange("crash", "", text);
    }

    public void repaintScene() {
        //cought by Draw
        firePropertyChange("repaint", false, true);
    }

    public void fireTrackReady(boolean ready) {
        // cought by StartMenu, TrackMenu
        firePropertyChange("startVisible", !ready, ready);
        if (ready) {
            fireHint(HintLabels.TRACK_READY);
            repaintScene();
        } else {
            fireHint(HintLabels.EMPTY);
        }
    }

    public void fireHint(String hintLabelProperty) {
        //cought by StatisticComponent
        firePropertyChange("hint", "", hintLabels.getValue(hintLabelProperty));
    }

    public void fireLoadTrack() {
        //cought by TracksComponent
        firePropertyChange("loadTrack", false, true);
    }

    //============================ SETTERS AND GETTERS =========================
    public int getStage() {
        return stage;
    }

    /**
     * Metoda nastavi fazi hry, ktera je udana celym cislem.
     *
     * @param stage 1-2 = staveni leve, resp. prave krajnice; 3-4 = editace bodu trate; 5 =
     * zahajovaci tah; 6 = normalni tah hrace; 7-8 = automaticke tahy (naraz resp. projeti cilem)
     */
    public void setStage(int stage) {
        this.stage = stage;
        if (stage == EDIT_PRESS) {
            fireHint(HintLabels.MOVE_POINTS);
            repaintScene();
        }
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
        hintLabels = new HintLabels(language);
        prop.firePropertyChange("language", null, language);
    }

    public String getHintLabel(String hintLabelProperty) {
        return hintLabels.getValue(hintLabelProperty);
    }

    public TurnMaker getTurnMaker() {
        return turnMaker;
    }

    public TrackBuilder getBuilder() {
        return buildTrack;
    }

    public Paper getPaper() {
        return paper;
    }

    public TrackAnalyzer getAnalyzer() {
        return analyzer;
    }

    public int getCheckLinesIndex() {
        if (computer != null) {
            return computer.getCheckLinesIndex();
        } else {
            return 0;
        }
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
