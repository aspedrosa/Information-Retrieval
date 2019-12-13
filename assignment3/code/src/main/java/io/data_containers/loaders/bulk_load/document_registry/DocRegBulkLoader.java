package io.data_containers.loaders.bulk_load.document_registry;

import java.io.IOException;

/**
 * Base class for all document registry bulk loaders.
 * It is assumed that the document translations are written
 *  sequential in some output format (line by line, binary, ...)
 */
public abstract class DocRegBulkLoader {

    protected String folder;

    public DocRegBulkLoader(String folder) {
        this.folder = folder;
    }

    /**
     * Creates a document registry class with the associations
     *  between our internal id to the document original identifier
     *
     * @param firstDocId to which document id the first identifier is associated to
     */
    public abstract DocumentRegistry load(String filename, int firstDocId) throws IOException;

    /**
     * Stores the translations between a set of document ids to their original
     *  identifier
     */
    public static class DocumentRegistry {

        /**
         * To which document id the first identifier is associated to
         */
        private int firstDocId;

        /**
         * The original identifier of the documents
         */
        private Integer[] translations;

        public DocumentRegistry(int firstDocId, Integer[] translations) {
            this.firstDocId = firstDocId;
            this.translations = translations;
        }

        /**
         * Translates an internal id to the document's original document.
         * If this document registry doesn't have the translations return null
         */
        public Integer translate(int docId) {
            if (docId < firstDocId || docId > firstDocId + translations.length) {
                return null;
            }

            return translations[docId - firstDocId];
        }
    }
}
