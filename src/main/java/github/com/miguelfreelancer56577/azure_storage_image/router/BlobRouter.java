package github.com.miguelfreelancer56577.azure_storage_image.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import github.com.miguelfreelancer56577.azure_storage_image.handler.BlobHandler;

/**
 * class used to route each petition, working with the blob storage
 *
 * @author mangelt
 *
 */
@Configuration
public class BlobRouter
{

	public static String API = "/blob-management";

	@Bean
	public RouterFunction<ServerResponse> blobItem(BlobHandler BlobHandler)
	{
		return RouterFunctions
				.route(RequestPredicates.POST(API.concat("/upload/{filename}")).and(RequestPredicates.accept(MediaType.MULTIPART_FORM_DATA)),
						BlobHandler::uploadFile)
				.andRoute(RequestPredicates.GET(API.concat("/download/{filename}")),
						BlobHandler::downloadFile);

	}

}