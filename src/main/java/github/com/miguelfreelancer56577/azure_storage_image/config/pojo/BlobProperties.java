package github.com.miguelfreelancer56577.azure_storage_image.config.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlobProperties {

	protected String connectionString;
	protected String containerName;
	
}
