package nz.co.senanque.jclouds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;

/**
 * Tests by writing to a GCP bucket. 
 * Prerequisites:
 * environment variable GOOGLE_APPLICATION_CREDENTIALS must contain a path to a credentials file eg ./account.json
 * also need env var jclouds.provider: google-cloud-storage
 * Requires a bucket named jclouds-test-bucket and write access to it.
 * 
 * @author Roger Parkinson
 */
@RunWith(SpringRunner.class)
@TestPropertySource
@ContextConfiguration(classes={JcloudsHelperConfig.class})
public class JcloudsHelperGCPIT {
	
	@Autowired JcloudsHelper jcloudsHelper;

	@Test
	public void test() throws IOException {
		final BlobStoreContext context = jcloudsHelper.getBlobStoreContext();
		final BlobStore blobStore = context.getBlobStore();
		boolean b = blobStore.createContainerInLocation(null, "jclouds-test-bucket");
		Assert.assertFalse("failed to find container",b);
		
		final ByteSource payload = ByteSource.wrap("blob-content".getBytes(Charsets.UTF_8));
		final Blob blob = blobStore.blobBuilder("test") // you can use folders via blobBuilder(folderName + "/sushi.jpg")
		    .payload(payload)
		    .contentLength(payload.size())
		    .build();

		// Upload the Blob
		blobStore.putBlob("billrush-test-trigger", blob);
		
		final Blob fetched = blobStore.getBlob("jclouds-test-bucket", "test");
		InputStream inputStream = fetched.getPayload().openStream();
		String text = new BufferedReader(
			      new InputStreamReader(inputStream, StandardCharsets.UTF_8))
			        .lines()
			        .collect(Collectors.joining("\n"));
		Assert.assertEquals("string is different", "blob-content", text);
		context.close();
	}

}
