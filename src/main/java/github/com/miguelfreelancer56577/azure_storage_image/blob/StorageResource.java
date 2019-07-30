package github.com.miguelfreelancer56577.azure_storage_image.blob;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.time.LocalTime;

import javax.validation.constraints.NotNull;

import org.springframework.util.StreamUtils;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

import lombok.extern.slf4j.Slf4j;

/**
 * Class Used to manage operations against blob file in azure , such as upload, delete, read, write etc.
 *
 * @author mangelt
 *
 */
@Slf4j
public class StorageResource
{

	protected CloudBlobContainer blobContainer;

	protected CloudBlockBlob blockBlob;

	/**
	 * Receives an instance of the Bean which has the container reference
	 *
	 * @param blobContainer: CloudBlobContainer
	 */
	public StorageResource(@NotNull CloudBlobContainer blobContainer)
	{
		this.blobContainer = blobContainer;
	}

	/**
	 * Constructor to set the container and the blob file to be used in each operation.
	 *
	 * @param blobContainer: CloudBlobContainer
	 * @param blockBlobReference: String
	 */
	public StorageResource(@NotNull CloudBlobContainer blobContainer, @NotNull String blockBlobReference)
	{
		this.blobContainer = blobContainer;
		this.setCloudBlockBlob(blockBlobReference);
	}

	/**
	 * Set a different blob file to used.
	 *
	 * @param blockBlobReference: String
	 */
	public void setCloudBlockBlob(String blockBlobReference)
	{
		try
		{
			this.blockBlob = this.blobContainer.getBlockBlobReference(blockBlobReference);
		}
		catch (URISyntaxException | StorageException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Get a reference to the blob container
	 *
	 * @return CloudBlobContainer
	 */
	public CloudBlobContainer getCloudBlobContainer()
	{
		return this.blobContainer;
	}

	/**
	 * Get a reference to blob file
	 *
	 * @return CloudBlockBlob
	 */
	public CloudBlockBlob getCloudBlockBlob()
	{
		return this.blockBlob;
	}

	/**
	 * Upload the text into the blob file from a string
	 *
	 * @param content: String
	 * @throws StorageException
	 * @throws IOException
	 */
	public void uploadText(String content) throws StorageException, IOException
	{
		this.blockBlob.uploadText(content);
	}

	/**
	 * Upload the blob file from a File
	 *
	 * @param sourceFile: File
	 * @throws StorageException
	 * @throws IOException
	 */
	public void uploadFromFile(File sourceFile) throws StorageException, IOException
	{
		this.blockBlob.uploadFromFile(sourceFile.getAbsolutePath());
	}

	/**
	 * Upload the blob file from a File object through a String
	 *
	 * @param content: String
	 * @throws StorageException
	 * @throws IOException
	 */
	public void uploadFromFile(String content) throws StorageException, IOException
	{
		File sourceFile = this.getTmpFile(content);
		this.blockBlob.uploadFromFile(sourceFile.getAbsolutePath());
	}

	/**
	 * Write a blob file from a String
	 *
	 * @param data: String
	 * @return boolean
	 */
	public boolean writeBlobFile(String data)
	{
		try (OutputStream os = this.getOutputStream())
		{
			os.write(data.getBytes());
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Read blob file and move to a String
	 *
	 * @return String
	 */
	public String readBlobFile()
	{
		try
		{
			return StreamUtils.copyToString(
					this.getInputStream(),
					Charset.defaultCharset());
		}
		catch (IOException e)
		{
			return "";
		}
	}

	/**
	 * Check if the container already exists
	 * @return boolean
	 */
	public boolean existsContainer()
	{
		try
		{
			return this.blobContainer.exists();
		}
		catch (StorageException e)
		{
			StorageResource.log.error("EXIST ERROR: {}", e);
			throw new RuntimeException("EXIST ERROR", e);
		}
	}

	/**
	 * Check if the blob file already exist.
	 *
	 * @return boolean
	 */
	public boolean existsBlob()
	{
		try
		{
			return this.blockBlob.exists();
		}
		catch (StorageException e)
		{
			StorageResource.log.error("EXIST ERROR: {}", e);
			throw new RuntimeException("EXIST ERROR", e);
		}
	}

	/**
	 * Check the length of the blob file
	 *
	 * @return long
	 * @throws IOException
	 */
	public long contentLength() throws IOException
	{
		return this.blockBlob.getProperties().getLength();
	}

	/**
	 * Get the time of the last change
	 *
	 * @return long
	 * @throws IOException
	 */
	public long lastModified() throws IOException
	{
		return this.blockBlob.getProperties().getLastModified().getTime();
	}

	/**
	 * Get the description of the blob file
	 *
	 * @return String
	 */
	public String getDescription()
	{
		return String
				.format("Azure storage account blob resource [container='%s', blob='%s']", this.blobContainer.getName(),
						this.blockBlob.getName());
	}

	/**
	 * Delete the blob file
	 *
	 * @return boolean
	 */
	public boolean deleteBlob()
	{
		StorageResource.log.info("URI: {}", this.blockBlob.getUri());
		try
		{
			return this.blockBlob.deleteIfExists();
		}
		catch (StorageException e)
		{
			StorageResource.log.error("ERROR TO DELETE BLOB {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Return an iterator object to list all the blob files in the container
	 *
	 * @return Iterable<ListBlobItem>
	 */
	public Iterable<ListBlobItem> listBlobs()
	{
		if (this.existsContainer())
		{
			return this.blobContainer.listBlobs();
		}
		return null;
	}

	/**
	 * Get the blob file as an InputStream object
	 *
	 * @return InputStream
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException
	{
		try
		{
			return this.blockBlob.openInputStream();
		}
		catch (StorageException e)
		{
			StorageResource.log.error("INPUT STREAM ERROR {}", e);
			throw new IOException("INPUT STREAM ERROR");
		}
	}

	/**
	 * Get an OutputStream object to write in the blob file
	 *
	 * @return OutputStream
	 * @throws IOException
	 */
	public OutputStream getOutputStream() throws IOException
	{
		try
		{
			return this.blockBlob.openOutputStream();
		}
		catch (StorageException e)
		{
			StorageResource.log.error("OUTPUT STREAM ERROR: {}", e);
			throw new IOException("OUTPUT STREAM ERROR", e);
		}
	}

	/**
	 * Used to create a temporal blob file
	 *
	 * @param content
	 * @return File
	 * @throws IOException
	 */
	protected File getTmpFile(String content) throws IOException
	{
		File sourceFile = File.createTempFile(LocalTime.now().toString(), ".txt");
		StorageResource.log.info("Creating file at: {}", sourceFile.toString());
		Writer output = new BufferedWriter(new FileWriter(sourceFile));
		output.write(content);
		output.close();
		return sourceFile;
	}

}