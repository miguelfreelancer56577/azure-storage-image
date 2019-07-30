package github.com.miguelfreelancer56577.azure_storage_image.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

import github.com.miguelfreelancer56577.azure_storage_image.blob.StorageResource;
import github.com.miguelfreelancer56577.azure_storage_image.exception.BlobException;
import github.com.miguelfreelancer56577.azure_storage_image.util.BlobUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Class used to handler each petition from the router
 *
 * @author mangelt
 *
 */
@Slf4j
@Component
public class BlobHandler
{
	@Autowired
	protected CloudBlobContainer blobContainer;

	protected ObjectMapper mapper = new ObjectMapper();

	/**
	 * Upload a file to the blob storage
	 *
	 * Validations:
	 *
	 * Validate the length of the file
	 * Validate the name of the file
	 * validate the extension of the file
	 *
	 * @param rq
	 * @return
	 */
	public Mono<ServerResponse> uploadFile(ServerRequest rq)
	{
		String fileName = rq.pathVariable("filename");

		log.info("FILE NAME {}", fileName);

		StorageResource sr = new StorageResource(this.blobContainer, fileName);

		BlobException bte = new BlobException();

		return rq.body(BodyExtractors.toMultipartData())
				.flatMap(BlobUtil::getFileFromMultipartData)
				.onErrorResume(e ->
				{
					BlobException returnedExcpetion = (BlobException)e;

					bte.setMessage(returnedExcpetion.getMessage());
					bte.setStatus(returnedExcpetion.getStatus());

					return Mono.just(new File(""));
				})
				.flatMap(file ->
				{
					try
					{
						if (bte.getStatus() != null)
						{
							return BlobUtil.onErrorResponse(bte);
						}

						BlobUtil.isValidFile(fileName, file);

						sr.uploadFromFile(file);
						log.info("FILE UPLOADED SUCCESSFULLY {}", file);

						return ServerResponse.ok()
								.build();

					}
					catch (BlobException e)
					{
						return BlobUtil.onErrorResponse(e);
					}
					catch (StorageException | IOException e)
					{
						return BlobUtil.onErrorResponse(new BlobException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
					}
				});
	}

	/**
	 * Download file from the blob storage
	 *
	 * @param rq
	 * @return cane be
	 *  ServerResponse.ok()
	 *  ServerResponse.notFound()
	 *  SHttpStatus.INTERNAL_SERVER_ERROR
	 */
	public Mono<ServerResponse> downloadFile(ServerRequest rq)
	{
		String fileName = rq.pathVariable("filename");

		log.info("FILE NAME {}", fileName);

		return Mono.just(fileName)
				.flatMap(fileToDownload ->
				{
					try
					{
						StorageResource sr = new StorageResource(this.blobContainer, fileToDownload);
						if (sr.existsBlob())
						{
							log.info("{} FILE EXITS", fileName);
							InputStream in = sr.getInputStream();
							byte[] stream = IOUtils.toByteArray(in);
							in.close();
							return ServerResponse.ok()
									.syncBody(stream);
						}
						else
						{
							return ServerResponse.notFound().build();
						}
					}
					catch (IOException e)
					{
						return BlobUtil.onErrorResponse(new BlobException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
					}
				});
	}

}