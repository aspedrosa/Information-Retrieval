package data_containers.indexer.structures;

/**
 * Base interface of all document to store on the posting lists.
 * Documents should also extend the Block class
 */
public interface BaseDocument {

    /**
     * Getter for the document Id
     *
     * @return document id
     */
    int getDocId();

}
