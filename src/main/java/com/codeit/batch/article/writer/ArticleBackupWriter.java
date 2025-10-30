package com.codeit.batch.article.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.codeit.batch.article.storage.S3BackupStorage;

import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class ArticleBackupWriter implements ItemStreamWriter<String> {
	private static final String TEMP_FILE_KEY = "articleBackupWriter.tempFile";
	private static final String SIZE_KEY = "articleBackupWriter.size";

	private final S3BackupStorage storage;

	private BufferedWriter writer;
	private Path tempFile;

	@Value("#{jobParameters['backupDate']}")
	private String backupDate;

	@Override
	public void open(@NonNull ExecutionContext context) throws ItemStreamException {
		boolean hasSavedPath = context.containsKey(TEMP_FILE_KEY);
		try {
			if (hasSavedPath) {
				String savedPath = context.getString(TEMP_FILE_KEY);
				tempFile = Path.of(savedPath);
				if (Files.exists(tempFile)) {
					writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
				} else {
					tempFile = Files.createTempFile("article-backup-", ".jsonl");
					writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8);
				}
			} else {
				tempFile = Files.createTempFile("article-backup-", ".jsonl");
				writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8);
			}

		} catch (IOException e) {
			throw new ItemStreamException("임시 파일을 열 수 없습니다", e);
		}
	}

	@Override
	public void write(Chunk<? extends String> chunk) throws Exception {
		for (String jsonLine : chunk) {
			writer.write(jsonLine);
			writer.newLine();
		}
		writer.flush();
	}

	@Override
	public void update(@NonNull ExecutionContext context) throws ItemStreamException {
		if (tempFile == null) {
			return;
		}
		context.putString(TEMP_FILE_KEY, tempFile.toString());
		try {
			context.putLong(SIZE_KEY, Files.size(tempFile));
		} catch (IOException e) {
			throw new ItemStreamException("임시 파일 크기를 확인할 수 없습니다", e);
		}
	}

	@Override
	public void close() throws ItemStreamException {
		if (writer == null) {
			return;
		}
		try {
			writer.close();
			byte[] payload = Files.readAllBytes(tempFile);
			storage.backup(backupDate, payload);
			Files.deleteIfExists(tempFile);
			tempFile = null;
		} catch (IOException e) {
			throw new ItemStreamException("백업 파일 처리 중 오류", e);
		}
	}
}
