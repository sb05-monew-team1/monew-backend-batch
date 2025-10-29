package com.codeit.batch.article.storage;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import com.codeit.batch.article.config.AwsProperties;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Component
public class S3BackupStorage {
	private static final String PREFIX = "backup/";
	private static final String EXTENSION = ".jsonl";

	private final S3Client s3Client;
	private final String bucket;

	private String keyOf(String date) {
		return PREFIX + date + "-articles" + EXTENSION;
	}

	public S3BackupStorage(AwsProperties props) {
		AwsCredentialsProvider awsCredentialsProvider = StaticCredentialsProvider.create(
			AwsBasicCredentials.create(props.accessKey(), props.secretKey()));
		this.s3Client = S3Client.builder()
			.credentialsProvider(awsCredentialsProvider)
			.region(Region.of(props.region()))
			.build();
		this.bucket = props.bucket();
	}

	public void backup(String date, byte[] file) {
		String key = keyOf(date);
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.contentType("application/x-ndjson")
			.contentEncoding(StandardCharsets.UTF_8.name())
			.contentLength((long)file.length)
			.build();

		try {
			s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file));
		} catch (Exception e) {
			throw new RuntimeException(e); // TODO: 커스텀 예외 추가
		}

	}
}
