package org.tiborsmith.wormkout.ui;

/**
 * Created by tibor on 22.8.14.
 */

public interface Function<R, A> {

    /** @param arg the argument
     *  @return the return value */
    public R apply(A arg);

}
