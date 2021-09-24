package com.animoto.api.enums;

public enum Pacing {
    /*
     * AUTO instructs Animoto to choose pacing based
     * on artistic considerations.  The pacing chosen
     * generally will be moderate, but, unlike the
     * MODERATE pacing value below, AUTO allows Animoto
     * to deviate from a moderate pacing based on artistic
     * considerations.
     *
     * AUTO is the default and recommended pacing choice.
     */
    AUTO,

    VERY_FAST,
    FAST,
    MODERATE,
    SLOW,
    VERY_SLOW,

    /*
     * The @Deprecated enum values will be removed very soon, so do *NOT* use
     * them.
     */
    @Deprecated
    DEFAULT,

    @Deprecated
    HALF,

    @Deprecated
    DOUBLE;
}
