package github.com.miguelfreelancer56577.azure_storage_image.util;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerResponse;

import github.com.miguelfreelancer56577.azure_storage_image.exception.BlobException;
import reactor.core.publisher.Mono;

/**
 * Utility class used by blob functionality
 * @author mangelt
 *
 */
public class BlobUtil
{

	/**
	 * Transform from MultiValueMap<String, Part> to Mono<File>
	 *
	 * @param parts : MultiValueMap<String, Part>
	 * @return Mono<File>
	 */
	public static Mono<File> getFileFromMultipartData(MultiValueMap<String, Part> parts)
	{
		Map<String, Part> map = parts.toSingleValueMap();
		FilePart filePart = (FilePart)map.get("file");
		File tmpFile;
		try
		{
			tmpFile = File.createTempFile(FilenameUtils.getBaseName(filePart.filename()),
					".".concat(FilenameUtils.getExtension(filePart.filename())));
		}
		catch (Exception e)
		{
			if (e instanceof NullPointerException)
			{
				return Mono.error(new BlobException(HttpStatus.INTERNAL_SERVER_ERROR, "You must provide a file to use this service."));
			}

			return Mono.error(new BlobException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
		}
		filePart.transferTo(tmpFile);
		return Mono.just(tmpFile);
	}

	/**
	 * Valid file name and file to be used.
	 *
	 * @param fileName: String
	 * @param file: File
	 * @return boolean
	 */
	public static boolean isValidFile(String fileName, File file)
	{

		String fileExtension = FilenameUtils.getExtension(file.getName());
		String fileNameExtension = FilenameUtils.getExtension(fileName);

		if (!fileNameExtension.equals(fileExtension))
		{
			throw new BlobException(HttpStatus.BAD_REQUEST, "The extesion of your file it's not the same.");
		}

		return true;
	}

	/**
	 * Get custom exception then wrap into a Mono<ServerResponse>
	 *
	 * @param e: BlobTodoException
	 * @return Mono<ServerResponse>
	 */
	public static Mono<ServerResponse> onErrorResponse(BlobException e)
	{
		return ServerResponse.status(e.getStatus())
				.body(Mono.just(e.getMessage()), String.class);
	}

}