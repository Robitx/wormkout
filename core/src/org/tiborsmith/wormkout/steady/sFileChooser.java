package org.tiborsmith.wormkout.steady;

/**
 * Created by tibor on 10.8.14.
 */

/** Copyright 2014 Robin Stumm (serverkorken@gmail.com, http://dermetfan.net)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. */

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

import net.dermetfan.utils.Function;
import net.dermetfan.utils.libgdx.scene2d.ui.FileChooser;

import java.io.File;
import java.io.FileFilter;

public class sFileChooser extends FileChooser {

    /** @see #fileNode(com.badlogic.gdx.files.FileHandle, com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle, net.dermetfan.utils.Function) */
    public static Tree.Node fileNode(FileHandle file, Label.LabelStyle labelStyle) {
        return fileNode(file, labelStyle, null);
    }

    /** @see #fileNode(FileHandle, java.io.FileFilter, com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle, net.dermetfan.utils.Function) */
    public static Tree.Node fileNode(FileHandle file, Label.LabelStyle labelStyle, Function<Void, Tree.Node> nodeConsumer) {
        return fileNode(file, null, labelStyle, nodeConsumer);
    }

    /** @see #fileNode(FileHandle, java.io.FileFilter, com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle, net.dermetfan.utils.Function) */
    public static Tree.Node fileNode(FileHandle file, FileFilter filter, final Label.LabelStyle labelStyle) {
        return fileNode(file, filter, labelStyle, null);
    }

    /** passes an Accessor that creates labels representing the file name (with slash if it's a folder) using the given label style to {@link #fileNode(FileHandle, FileFilter, net.dermetfan.utils.Function, net.dermetfan.utils.Function)} (labelSupplier)
     *  @param labelStyle the {@link com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle} to use for created labels
     *  @see #fileNode(FileHandle, FileFilter, net.dermetfan.utils.Function, net.dermetfan.utils.Function) */
    public static Tree.Node fileNode(FileHandle file, FileFilter filter, final Label.LabelStyle labelStyle, Function<Void, Tree.Node> nodeConsumer) {
        return fileNode(file, filter, new Function<Label, FileHandle>() {
            @Override
            public Label apply(FileHandle file) {
                String name = file.name();
                if(file.isDirectory())
                    name += File.separator;
                return new sLabel(name, labelStyle);
            }
        }, nodeConsumer);
    }

    /** @see #fileNode(FileHandle, FileFilter, net.dermetfan.utils.Function, net.dermetfan.utils.Function) */
    public static Tree.Node fileNode(FileHandle file, FileFilter filter, Function<Label, FileHandle> labelSupplier) {
        return fileNode(file, filter, labelSupplier, null);
    }

    /** creates an anonymous subclass of {@link com.badlogic.gdx.scenes.scene2d.ui.Tree.Node} that recursively adds the children of the given file to it when being {@link com.badlogic.gdx.scenes.scene2d.ui.Tree.Node#setExpanded(boolean) expanded} for the first time
     *  @param file the file to put in {@link com.badlogic.gdx.scenes.scene2d.ui.Tree.Node#setObject(Object)}
     *  @param filter Filters children from being added. May be null to accept all files.
     *  @param labelSupplier supplies labels to use
     *  @param nodeConsumer Does something with nodes after they were created. May be null.
     *  @return the created Node */
    public static Tree.Node fileNode(final FileHandle file, final FileFilter filter, final Function<Label, FileHandle> labelSupplier, final Function<Void, Tree.Node> nodeConsumer) {
        Label label = labelSupplier.apply(file);

        Tree.Node node;
        if(file.isDirectory()) {
            final Tree.Node dummy = new Tree.Node(new Actor());

            node = new Tree.Node(label) {
                private boolean childrenAdded;

                @Override
                public void setExpanded(boolean expanded) {
                    if(expanded == isExpanded())
                        return;

                    if(expanded && !childrenAdded) {
                        if(filter != null)
                            for(File child : file.file().listFiles(filter))
                                add(fileNode(file.child(child.getName()), filter, labelSupplier, nodeConsumer));
                        else
                            for(FileHandle child : file.list())
                                add(fileNode(child, filter, labelSupplier, nodeConsumer));
                        childrenAdded = true;
                        remove(dummy);
                    }

                    super.setExpanded(expanded);
                }
            };
            node.add(dummy);

            if(nodeConsumer != null)
                nodeConsumer.apply(dummy);
        } else
            node = new Tree.Node(label);
        node.setObject(file);

        if(nodeConsumer != null)
            nodeConsumer.apply(node);

        return node;
    }

    /** the Tree used to show files and folders */
    private Tree tree;

    /** the ScrollPane {@link #tree} is embedded in */
    private ScrollPane treePane;

    /** basic operation buttons */
    private sTextButton chooseButton, cancelButton;

    /** Listener for {@link #tree}.
     *  {@link Button#setDisabled(boolean) Disables/enables} {@link #chooseButton} based on the {@link Tree#getSelection() selection} of {@link #tree} and {@link #isDirectoriesChoosable()} */
    public final ClickListener treeListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            Selection<Tree.Node> selection = tree.getSelection();
            if(selection.size() < 1) {
                chooseButton.setDisabled(true);
                return;
            }
            if(!isDirectoriesChoosable()) {
                Object lastObj = selection.getLastSelected().getObject();
                if(lastObj instanceof FileHandle) {
                    FileHandle file = (FileHandle) lastObj;
                    if(file.isDirectory()) {
                        chooseButton.setDisabled(true);
                        return;
                    }
                }
            }
            chooseButton.setDisabled(false);
        }
    };

    /** Listener for {@link #chooseButton}.
     *  Calls {@link Listener#choose(com.badlogic.gdx.utils.Array)} or {@link Listener#choose(FileHandle)} depending on the {@link Tree#getSelection() selection} of {@link #tree} */
    public final ClickListener chooseButtonListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            if(chooseButton.isDisabled())
                return;
            Selection<Tree.Node> selection = tree.getSelection();
            if(selection.size() < 1)
                return;
            if(selection.getMultiple()) {
                @SuppressWarnings("unchecked")
                Array<FileHandle> files = Pools.obtain(Array.class);
                for(Tree.Node node : selection) {
                    Object object = node.getObject();
                    if(object instanceof FileHandle) {
                        FileHandle file = (FileHandle) object;
                        if(isDirectoriesChoosable() || !file.isDirectory())
                            files.add(file);
                    }
                }
                getListener().choose(files);
                files.clear();
                Pools.free(files);
            } else {
                Object object = selection.getLastSelected().getObject();
                if(object instanceof FileHandle) {
                    FileHandle file = (FileHandle) object;
                    if(isDirectoriesChoosable() || !file.isDirectory())
                        getListener().choose(file);
                }
            }
        }
    };

    /** Listener for {@link #cancelButton}.
     *  Calls {@link Listener#cancel()} of the {@link #getListener() listener} */
    public final ClickListener cancelButtonListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            getListener().cancel();
        }
    };


    public sFileChooser(Skin skin, Listener listener) {
        super(listener);
        buildWidgets(skin);
        setSkin(skin);
        build();
    }



    /** @param file the {@link File} to {@link Tree#add(com.badlogic.gdx.scenes.scene2d.ui.Tree.Node) add a root} for
     *  @return the added {@link #fileNode(FileHandle, java.io.FileFilter, com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle) file node} */
    public Tree.Node add(FileHandle file, Skin skin) {
        Tree.Node node = fileNode(file, handlingFileFilter, skin.get(Label.LabelStyle.class));
        tree.add(node);
        return node;
    }

    /** builds {@link #chooseButton}, {@link #cancelButtonListener}, {@link #tree}, {@link #treePane} */
    protected void buildWidgets(Skin skin) {
        tree = new Tree(skin);
        tree.addListener(treeListener);
        treePane = new ScrollPane(tree);
        chooseButton = new sTextButton("  Add song  ",skin);
        chooseButton.addListener(chooseButtonListener);
        chooseButton.setDisabled(true);


        (cancelButton = new sTextButton("  Add song  ",skin)).addListener(cancelButtonListener);
        cancelButton.setVisible(false);
        cancelButton.setDisabled(true);
    }

    @Override
    protected void build() {
        clearChildren();
        treePane.setWidget(tree);
        add().expandX().fill().row();
        add(treePane).colspan(2).expand().fill().row();
        add(chooseButton).colspan(2).width(200).row();
        add(cancelButton).colspan(2);
    }

    /** @return the {@link #tree} */
    public Tree getTree() {
        return tree;
    }

    /** @param tree the {@link #tree} to set */
    public void setTree(Tree tree) {
        if(tree == null)
            throw new IllegalArgumentException("tree must not be null");
        this.tree = tree;
    }
}
