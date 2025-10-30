package com.codeit.batch.article.storage;

/**
 * Signals that backing up article data to the configured storage has failed.
 */
public class BackupStorageException extends RuntimeException {

	public BackupStorageException(String message) {
		super(message);
	}

	public BackupStorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
