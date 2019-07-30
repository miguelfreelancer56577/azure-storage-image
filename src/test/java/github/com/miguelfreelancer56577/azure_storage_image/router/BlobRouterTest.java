package github.com.miguelfreelancer56577.azure_storage_image.router;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

import github.com.miguelfreelancer56577.azure_storage_image.blob.StorageResource;
import github.com.miguelfreelancer56577.azure_storage_image.util.MultiPartResource;
import lombok.extern.slf4j.Slf4j;

/**
 * Suit of Test Cases for BlobRouter functionality
 *
 * Upload Service
 *
 * uploadFilesTest: Upload files to azure storage, trigger an error if file wasn't created
 * uploadFilesTest1: Call the service without file, must return an 500 status code
 * uploadFilesTest3: Call the service with a file longer than 26 characters, must return an 400 status code
 * uploadFilesTest4: Call the service with a different extension file, must return an 400 status code
 *
 * Download Service
 *
 * downloadFileTest: download the specific file from blob storage, must return an 200 status code
 * downloadFileTest1: Call the service to get a file that doesn't exist, must return an 404 status code
 *
 * Others
 *
 * setUp: Set up necessary objects to be used in each test
 * cleanUp: clean up components used in each test
 *
 *
 * @author mangelt
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext
@Slf4j
public class BlobRouterTest
{

	@Autowired
	WebTestClient webTestClient;

	LocalTime date = LocalTime.now();

	@Value("classpath:router_files/")
	String recourceDir;

	static ObjectMapper mapper = new ObjectMapper();

	@Autowired
	ResourceLoader resourceLoader;

	static Map<String, Object> cache = new HashMap<>();

	@Autowired
	protected CloudBlobContainer blobContainer;

	/**
	 * Get files from dir to be used in tests
	 */
	@Before
	public void setUp()
	{

		if (cache.get("files") == null)
		{

			StorageResource sr = new StorageResource(this.blobContainer);
			cache.put("StorageResource", sr);

			List<File> files = new ArrayList();
			cache.put("files", files);

			try
			{
				Files.list(Paths.get(this.resourceLoader.getResource(this.recourceDir).getFile().getPath()))
				.forEach(path -> files.add(path.toFile()));

			}
			catch (Exception e)
			{
				log.error(BlobRouterTest.class.getSimpleName(), e.getMessage());
				assertTrue(false);
			}

		}
	}

	/**
	 * Upload files to azure storage, trigger an error if file wasn't created
	 */
	@Test
	public void uploadFilesTest()
	{
		List<File> files = (List<File>)cache.get("files");

		//upload each file to azure storage
		files.forEach(file ->
		{

			MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

			try
			{
				ByteArrayResource resource =
						new MultiPartResource(Files.readAllBytes(file.toPath()), file.getName());

				map.set("file", resource);
			}
			catch (IOException e)
			{
				log.error("ERROR TO READ THE FILES OF THE FILE: {}", e.getMessage());
				assertTrue(false);
			}

			log.info("SAVING THE {} FILE INTO BLOB STORAGE", file.getName());
			this.webTestClient.post().uri(BlobRouter.API.concat("/upload/{filename}"), file.getName())
			.contentType(MediaType.MULTIPART_FORM_DATA)
			.body(BodyInserters.fromMultipartData(map))
			.exchange()
			.expectStatus()
			.is2xxSuccessful()
			.expectBody()
			.isEmpty();

		});

		//check if the file was created, return an error if it wasn't

		files.forEach(file ->
		{
			log.info("CHECKING IF {} WAS ADDED", file.getName());
			StorageResource storageResource = (StorageResource)cache.get("StorageResource");
			storageResource.setCloudBlockBlob(file.getName());
			assertTrue(storageResource.existsBlob());
		});

	}

	/**
	 * Call the service without file, must return an 500 status code
	 */
	@Test
	public void uploadFilesTest1()
	{
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

		log.info("Assert the response status code is in the 5xx range.");
		this.webTestClient.post().uri(BlobRouter.API.concat("/upload/{filename}"), "test.txt")
		.contentType(MediaType.MULTIPART_FORM_DATA)
		.syncBody(map)
		.exchange()
		.expectStatus()
		.is5xxServerError();
	}

	/**
	 * Call the service with a file longer than 26 characters, must return an 400 status code
	 */
	@Test
	public void uploadFilesTest3()
	{

		List<File> files = (List<File>)cache.get("files");
		File file = files.get(1);

		MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

		try
		{
			ByteArrayResource resource =
					new MultiPartResource(Files.readAllBytes(file.toPath()), file.getName());

			map.set("file", resource);
		}
		catch (IOException e)
		{
			log.error("ERROR TO READ THE FILES OF THE FILE: {}", e.getMessage());
			assertTrue(false);
		}

		log.info("Assert the response status code is 400.");
		this.webTestClient.post().uri(BlobRouter.API.concat("/upload/{filename}"), "testxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.txt")
		.contentType(MediaType.MULTIPART_FORM_DATA)
		.syncBody(map)
		.exchange()
		.expectStatus()
		.isBadRequest();
	}

	/**
	 * Call the service with a different extension file, must return an 400 status code
	 */
	@Test
	public void uploadFilesTest4()
	{
		List<File> files = (List<File>)cache.get("files");
		File file = files.get(1);

		String fileName = FilenameUtils.getBaseName(file.getName());

		MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

		try
		{
			ByteArrayResource resource =
					new MultiPartResource(Files.readAllBytes(file.toPath()), file.getName());

			map.set("file", resource);
		}
		catch (IOException e)
		{
			log.error("ERROR TO READ THE FILES OF THE FILE: {}", e.getMessage());
			assertTrue(false);
		}

		log.info("Assert the response status code is 400.");
		this.webTestClient.post().uri(BlobRouter.API.concat("/upload/{filename}"), fileName.concat(".other"))
		.contentType(MediaType.MULTIPART_FORM_DATA)
		.body(BodyInserters.fromMultipartData(map))
		.exchange()
		.expectStatus()
		.isBadRequest();
	}

	/**
	 * download the specific file from blob storage, must return an 200 status code
	 */
	@Test
	public void downloadFileTest()
	{

		List<File> files = (List<File>)cache.get("files");
		File file = files.get(1);

		try
		{
			log.info("CREATE THE {} FILE IN THE BLOB CONTAINER", file.getName());
			StorageResource sr = new StorageResource(this.blobContainer, file.getName());
			sr.uploadFromFile(file);
		}
		catch (StorageException | IOException e)
		{
			assertTrue(false);
		}

		log.info("DOWNLOAD THE {} FILE AS AN ARRAY OF BYTE", file.getName());
		byte[] byteFIle = this.webTestClient.get().uri(BlobRouter.API.concat("/download/{filename}"), file.getName())
				.exchange()
				.expectStatus()
				.isOk()
				.expectBody(byte[].class)
				.returnResult()
				.getResponseBody();

		try
		{
			byte[] byteFIleOriginal = Files.readAllBytes(file.toPath());

			if (Arrays.equals(byteFIle, byteFIleOriginal))
			{
				assertTrue(true);
			}
			else
			{
				assertTrue(false);
			}

		}
		catch (IOException e)
		{
			assertTrue(false);
		}

	}

	/**
	 * Call the service to get a file that doesn't exist, must return an 404 status code
	 */
	@Test
	public void downloadFileTest1()
	{

		log.info("Assert the response status code is 404, because file doesn't exit.");
		this.webTestClient.get().uri(BlobRouter.API.concat("/download/{filename}"), "test.txt")
		.exchange()
		.expectStatus()
		.isNotFound();

	}


	/**
	 * Clean up, delete files used by the tests
	 */
	@AfterClass
	public static void cleanUp()
	{
		List<File> files = (List<File>)cache.get("files");

		files.forEach(file ->
		{
			log.info("DELETING FILE FROM THE BLOB STORAGE: {}", file.getName());
			StorageResource storageResource = (StorageResource)cache.get("StorageResource");
			storageResource.setCloudBlockBlob(file.getName());
			storageResource.deleteBlob();

		});

	}

}