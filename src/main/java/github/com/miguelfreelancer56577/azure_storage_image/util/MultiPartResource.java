package github.com.miguelfreelancer56577.azure_storage_image.util;


import org.springframework.core.io.ByteArrayResource;
import org.springframework.lang.Nullable;

/**
 * Used by the BlobTodoRouterTest to wrap a file then send it to the service
 * @author mangelt
 *
 */
public class MultiPartResource extends ByteArrayResource
{

	private String file;

	public MultiPartResource(byte[] byteArray)
	{
		super(byteArray);
	}

	public MultiPartResource(byte[] byteArray, String file)
	{
		super(byteArray);
		this.file = file;
	}

	@Nullable
	@Override
	public String getFilename()
	{
		return this.file;
	}

	public void setFilename(String file)
	{
		this.file = file;
	}
}