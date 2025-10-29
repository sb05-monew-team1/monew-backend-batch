package com.codeit.batch.article.writer;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.codeit.batch.article.storage.S3BackupStorage;

import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class ArticleBackupWriter implements ItemStreamWriter<String> {
	private final S3BackupStorage storage;

	private ByteArrayOutputStream out;
	private BufferedWriter writer;

	@Value("#{jobParameters['backupDate']}")
	private String backupDate;

	@Override
	public void open(ExecutionContext executionContext) {
		out = new ByteArrayOutputStream(1024 * 1024);
		writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
	}

	@Override
	public void write(Chunk<? extends String> chunk) throws Exception {
		for (String jsonLine : chunk) {
			writer.write(jsonLine);
			writer.newLine();
		}
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		executionContext.putLong("articleBackupWriter.size", out.size());
	}

	@Override
	public void close() throws ItemStreamException {
		if (writer == null) {
			return;
		}
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		byte[] payload = out.toByteArray();
		storage.backup(backupDate, payload);
	}
}
