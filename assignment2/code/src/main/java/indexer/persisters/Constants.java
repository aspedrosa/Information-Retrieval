package indexer.persisters;

/**
 * Holds bytes constants that are used for different classes.
 * Avoids byte[] initialization overhead.
 */
public final class Constants {

    /**
     * Since it just holds constants the constructor is private
     */
    private Constants(){}

    /**
     * Comma character
     */
    public final static byte[] COMMA = {','};

    /**
     * New line character
     */
    public final static byte[] NEWLINE = {'\n'};

}
