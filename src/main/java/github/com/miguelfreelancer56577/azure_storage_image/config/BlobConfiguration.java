package github.com.miguelfreelancer56577.azure_storage_image.config;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

/**
 * Class used to configure the azure blob storage account.
 *
 * @author mangelt
 *
 */
@Configuration
public class BlobConfiguration
{

	@Value("${azure.storage.connection-string}")
	protected String StorageConnectionString;

	@Value("${container.name}")
	protected String containerName;

	/**
	 * Get a CloudBlobClient to manage any blob file
	 *
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 */
	@Bean
	protected CloudBlobClient getCloudBlobClientInstance() throws InvalidKeyException, URISyntaxException
	{
		return CloudStorageAccount.parse(this.StorageConnectionString).createCloudBlobClient();
	}

	/**
	 * Get a CloudBlobContainer to manage any operation against the blob storage file
	 *
	 * @param blobClient
	 * @return
	 */
	@Bean
	@NotNull
	@ConditionalOnBean(CloudBlobClient.class)
	protected CloudBlobContainer getCloudBlobContainerInstance(CloudBlobClient blobClient)
	{
		CloudBlobContainer container = null;
		try
		{
			container = blobClient.getContainerReference(this.containerName);
			container.createIfNotExists();
		}
		catch (URISyntaxException | StorageException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return container;
	}

}