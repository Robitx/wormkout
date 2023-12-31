package org.tiborsmith.wormkout.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.ObjectMap;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by tibor on 17.8.14.
 */
public class sDialog extends sWindow {
    Table contentTable, buttonTable;
    private Skin skin;
    ObjectMap<Actor, Object> values = new ObjectMap();
    boolean cancelHide;
    Actor previousKeyboardFocus, previousScrollFocus;

    InputListener ignoreTouchDown = new InputListener() {
        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };

    public sDialog(String title, Skin skin) {
        super(title, skin.get(WindowStyle.class));
        this.skin = skin;
        initialize();
    }

    public sDialog(String title, Skin skin, String windowStyleName) {
        super(title, skin.get(windowStyleName, WindowStyle.class));
        setSkin(skin);
        this.skin = skin;
        initialize();
    }

    public sDialog(String title, WindowStyle windowStyle) {
        super(title, windowStyle);
        initialize();
    }

    private void initialize () {
        setModal(true);

        defaults().space(6);
        add(contentTable = new Table(skin)).expand().fill();
        row();
        add(buttonTable = new Table(skin));

        contentTable.defaults().space(6);
        buttonTable.defaults().space(6);

        buttonTable.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                if (!values.containsKey(actor)) return;
                while (actor.getParent() != buttonTable)
                    actor = actor.getParent();
                result(values.get(actor));
                if (!cancelHide) hide();
                cancelHide = false;
            }
        });

        addListener(new FocusListener() {
            public void keyboardFocusChanged (FocusEvent event, Actor actor, boolean focused) {
                if (!focused) focusChanged(event);
            }

            public void scrollFocusChanged (FocusEvent event, Actor actor, boolean focused) {
                if (!focused) focusChanged(event);
            }

            private void focusChanged (FocusEvent event) {
                Stage stage = getStage();
                if (isModal() && stage != null && stage.getRoot().getChildren().size > 0
                        && stage.getRoot().getChildren().peek() == sDialog.this) { // Dialog is top most actor.
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(sDialog.this)) event.cancel();
                }
            }
        });
    }

    public Table getContentTable () {
        return contentTable;
    }

    public Table getButtonTable () {
        return buttonTable;
    }

    /** Adds a label to the content table. The dialog must have been constructed with a skin to use this method. */
    public sDialog text (String text) {
        if (skin == null)
            throw new IllegalStateException("This method may only be used if the dialog was constructed with a Skin.");
        return text(text, skin.get(Label.LabelStyle.class));
    }

    /** Adds a label to the content table. */
    public sDialog text (String text, Label.LabelStyle labelStyle) {
        return text(new sLabel(text, labelStyle));
    }

    /** Adds the given Label to the content table */
    public sDialog text (sLabel label) {
        label.setWrap(true);
        label.setAlignment(Align.center);
        contentTable.add(label).width(Gdx.graphics.getWidth()/2);
        return this;
    }

    /** Adds a text button to the button table. Null will be passed to {@link #result(Object)} if this button is clicked. The dialog
     * must have been constructed with a skin to use this method. */
    public sDialog button (String text) {
        return button(text, null);
    }

    /** Adds a text button to the button table. The dialog must have been constructed with a skin to use this method.
     * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null. */
    public sDialog button (String text, Object object) {
        if (skin == null)
            throw new IllegalStateException("This method may only be used if the dialog was constructed with a Skin.");
        return button(text, object, skin.get(sTextButton.TextButtonStyle.class));
    }

    /** Adds a text button to the button table.
     * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null. */
    public sDialog button (String text, Object object, sTextButton.TextButtonStyle buttonStyle) {
        return button(new sTextButton(text, buttonStyle), object);
    }

    /** Adds the given button to the button table. */
    public sDialog button (Button button) {
        return button(button, null);
    }

    /** Adds the given button to the button table.
     * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null. */
    public sDialog button (Button button, Object object) {
        buttonTable.add(button).width(100);
        setObject(button, object);
        return this;
    }

    /** {@link #pack() Packs} the dialog and adds it to the stage with custom action which can be null for instant show */
    public sDialog show (Stage stage, Action action) {
        clearActions();
        removeCaptureListener(ignoreTouchDown);

        previousKeyboardFocus = null;
        Actor actor = stage.getKeyboardFocus();
        if (actor != null && !actor.isDescendantOf(this)) previousKeyboardFocus = actor;

        previousScrollFocus = null;
        actor = stage.getScrollFocus();
        if (actor != null && !actor.isDescendantOf(this)) previousScrollFocus = actor;

        pack();
        stage.addActor(this);
        stage.setKeyboardFocus(this);
        stage.setScrollFocus(this);
        if (action != null)
            addAction(action);

        return this;
    }

    /** {@link #pack() Packs} the dialog and adds it to the stage, centered with default fadeIn action */
    public sDialog show (Stage stage) {
        show(stage, sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade)));
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
        return this;
    }

    /** Hides the dialog with the given action and then removes it from the stage. */
    public void hide (Action action) {
        Stage stage = getStage();
        if (stage != null) {
            if (previousKeyboardFocus != null && previousKeyboardFocus.getStage() == null) previousKeyboardFocus = null;
            Actor actor = stage.getKeyboardFocus();
            if (actor == null || actor.isDescendantOf(this)) stage.setKeyboardFocus(previousKeyboardFocus);

            if (previousScrollFocus != null && previousScrollFocus.getStage() == null) previousScrollFocus = null;
            actor = stage.getScrollFocus();
            if (actor == null || actor.isDescendantOf(this)) stage.setScrollFocus(previousScrollFocus);
        }
        if (action != null) {
            addCaptureListener(ignoreTouchDown);
            addAction(sequence(action, Actions.removeListener(ignoreTouchDown, true), Actions.removeActor()));
        } else
            remove();
    }

    /** Hides the dialog. Called automatically when a button is clicked. The default implementation fades out the dialog over 400
     * milliseconds and then removes it from the stage. */
    public void hide () {
        hide(sequence(fadeOut(0.4f, Interpolation.fade), Actions.removeListener(ignoreTouchDown, true), Actions.removeActor()));
    }

    public void setObject (Actor actor, Object object) {
        values.put(actor, object);
    }

    /** If this key is pressed, {@link #result(Object)} is called with the specified object.
     * @see com.badlogic.gdx.Input.Keys */
    public sDialog key (final int keycode, final Object object) {
        addListener(new InputListener() {
            public boolean keyDown (InputEvent event, int keycode2) {
                if (keycode == keycode2) {
                    result(object);
                    if (!cancelHide) hide();
                    cancelHide = false;
                }
                return false;
            }
        });
        return this;
    }

    /** Called when a button is clicked. The dialog will be hidden after this method returns unless {@link #cancel()} is called.
     * @param object The object specified when the button was added. */
    protected void result (Object object) {
    }

    public void cancel () {
        cancelHide = true;
    }
}
